package com.railops.ai;

import com.railops.common.DependencyUnavailableException;
import com.railops.config.AiServiceConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AiClient {

    private static final Logger log = LoggerFactory.getLogger(AiClient.class);

    private final RestTemplate restTemplate;
    private final AiServiceConfig aiServiceConfig;
    private final ObjectMapper objectMapper;

    public AiClient(RestTemplate restTemplate, AiServiceConfig aiServiceConfig, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.aiServiceConfig = aiServiceConfig;
        this.objectMapper = objectMapper;
    }

    public AiPredictionResponse predictDelay(AiPredictionRequest request) {
        String baseUrl = aiServiceConfig.getUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new DependencyUnavailableException("AI service URL is not configured");
        }

        String requestUrl = baseUrl.endsWith("/") ? baseUrl + "predict/delay" : baseUrl + "/predict/delay";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request.toFeatureMap(), headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(requestUrl, HttpMethod.POST, entity, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new DependencyUnavailableException("AI service returned an invalid response");
            }

            Map<String, Object> payload = response.getBody();
            Object predictedValue = payload.get("predicted_delay_minutes");
            if (predictedValue == null) {
                throw new DependencyUnavailableException("AI service response did not include a prediction value");
            }

            AiPredictionResponse aiPrediction = new AiPredictionResponse();
            aiPrediction.setPredictedDelayMinutes(parseDouble(predictedValue));
            Object modelVersion = payload.get("model_version");
            if (modelVersion != null) {
                aiPrediction.setModelVersion(String.valueOf(modelVersion));
            }

            Object rawFeatures = payload.get("features");
            if (rawFeatures instanceof Map<?, ?> rawMap) {
                Map<String, Object> featureMap = new LinkedHashMap<>();
                rawMap.forEach((key, value) -> featureMap.put(String.valueOf(key), value));
                aiPrediction.setFeatures(featureMap);
            }

            return aiPrediction;
        } catch (RestClientException ex) {
            log.warn("AI prediction request failed: {}", ex.getMessage());
            throw new DependencyUnavailableException("AI service is unavailable or timed out");
        }
    }

    private Double parseDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            throw new DependencyUnavailableException("AI service returned a malformed numeric prediction");
        }
    }
}
