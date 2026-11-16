package com.railops.ai;

import com.railops.common.DependencyUnavailableException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiIntegrationService {

    private final AiClient aiClient;
    private final AiPredictionRepository aiPredictionRepository;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public AiIntegrationService(AiClient aiClient,
                               AiPredictionRepository aiPredictionRepository,
                               ObjectMapper objectMapper,
                               PlatformTransactionManager transactionManager) {
        this.aiClient = aiClient;
        this.aiPredictionRepository = aiPredictionRepository;
        this.objectMapper = objectMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public List<AiPredictionResponse> listPredictions() {
        return aiPredictionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AiPredictionResponse predictDelay(AiPredictionRequest request) {
        try {
            AiPredictionResponse response = aiClient.predictDelay(request);
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(TransactionStatus status) {
                    persistSuccess(request, response);
                }
            });
            return response;
        } catch (DependencyUnavailableException | IllegalArgumentException ex) {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(TransactionStatus status) {
                    persistFailure(request, ex.getMessage());
                }
            });
            throw new DependencyUnavailableException("AI prediction service is unavailable");
        }
    }

    public void persistFailure(AiPredictionRequest request, String failureReason) {
        AiPrediction prediction = new AiPrediction();
        prediction.setStatus(AiPredictionStatus.FAILED);
        prediction.setFailureReason(failureReason);
        prediction.setFeatureSnapshot(toJson(request.toFeatureMap()));
        aiPredictionRepository.save(prediction);
    }

    public void persistSuccess(AiPredictionRequest request, AiPredictionResponse response) {
        AiPrediction prediction = new AiPrediction();
        prediction.setStatus(AiPredictionStatus.SUCCESS);
        prediction.setModelVersion(response.getModelVersion());
        prediction.setPredictionResult(response.getPredictedDelayMinutes());
        prediction.setFeatureSnapshot(toJson(request.toFeatureMap()));
        aiPredictionRepository.save(prediction);
    }

    private AiPredictionResponse toResponse(AiPrediction prediction) {
        AiPredictionResponse response = new AiPredictionResponse();
        response.setStatus(prediction.getStatus().name());
        response.setModelVersion(prediction.getModelVersion());
        response.setPredictedDelayMinutes(prediction.getPredictionResult());
        response.setFailureReason(prediction.getFailureReason());
        response.setCreatedAt(prediction.getCreatedAt());

        if (prediction.getFeatureSnapshot() != null) {
            try {
                response.setFeatures(objectMapper.readValue(prediction.getFeatureSnapshot(), java.util.Map.class));
            } catch (JsonProcessingException ex) {
                response.setFeatures(new java.util.LinkedHashMap<>());
            }
        }

        return response;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }
}
