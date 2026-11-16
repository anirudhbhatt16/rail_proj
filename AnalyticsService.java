package com.railops.analytics;

import com.railops.ai.AiPredictionRepository;
import com.railops.ai.AiPredictionStatus;
import com.railops.resource.*;
import com.railops.simulation.SimulationRepository;
import com.railops.simulation.SimulationStatus;
import com.railops.train.TrainRepository;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private final SimulationRepository simulationRepository;
    private final ResourceRequestRepository resourceRequestRepository;
    private final ResourceAllocationRepository resourceAllocationRepository;
    private final ConflictRepository conflictRepository;
    private final TrainRepository trainRepository;
    private final AiPredictionRepository aiPredictionRepository;

    public AnalyticsService(SimulationRepository simulationRepository,
                            ResourceRequestRepository resourceRequestRepository,
                            ResourceAllocationRepository resourceAllocationRepository,
                            ConflictRepository conflictRepository,
                            TrainRepository trainRepository,
                            AiPredictionRepository aiPredictionRepository) {
        this.simulationRepository = simulationRepository;
        this.resourceRequestRepository = resourceRequestRepository;
        this.resourceAllocationRepository = resourceAllocationRepository;
        this.conflictRepository = conflictRepository;
        this.trainRepository = trainRepository;
        this.aiPredictionRepository = aiPredictionRepository;
    }

    public AnalyticsOverviewResponse getOverview() {
        long runningSimulations = simulationRepository.countByStatus(SimulationStatus.RUNNING);
        long totalTrains = trainRepository.count();
        long waitingTrains = resourceRequestRepository.findAll().stream()
                .filter(request -> request.getStatus() == ResourceRequestStatus.PENDING)
                .map(ResourceRequest::getTrainId)
                .distinct()
                .count();
        long activeResourceAllocations = resourceAllocationRepository.countByStatus(ResourceAllocationStatus.ACTIVE);
        long openConflicts = conflictRepository.countByStatus(ConflictStatus.OPEN);
        long deadlockConflicts = conflictRepository.countByConflictType("DEADLOCK");
        long aiPredictionCount = aiPredictionRepository.count();
        Double averagePredictedDelayMinutes = aiPredictionRepository.findAveragePredictionResultByStatus(AiPredictionStatus.SUCCESS);

        AnalyticsOverviewResponse response = new AnalyticsOverviewResponse();
        response.setRunningSimulations(runningSimulations);
        response.setTotalTrains(totalTrains);
        response.setWaitingTrains(waitingTrains);
        response.setActiveResourceAllocations(activeResourceAllocations);
        response.setOpenConflicts(openConflicts);
        response.setDeadlockConflicts(deadlockConflicts);
        response.setAiPredictionCount(aiPredictionCount);
        response.setAveragePredictedDelayMinutes(averagePredictedDelayMinutes);
        response.setAverageActualDelayMinutes(null);
        return response;
    }
}
