package com.railops.analytics;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyticsOverviewResponse {

    private long runningSimulations;
    private long totalTrains;
    private long waitingTrains;
    private long activeResourceAllocations;
    private long openConflicts;
    private long deadlockConflicts;
    private long aiPredictionCount;
    private Double averagePredictedDelayMinutes;
    private Double averageActualDelayMinutes;

    public AnalyticsOverviewResponse() {
    }

    public long getRunningSimulations() {
        return runningSimulations;
    }

    public void setRunningSimulations(long runningSimulations) {
        this.runningSimulations = runningSimulations;
    }

    public long getTotalTrains() {
        return totalTrains;
    }

    public void setTotalTrains(long totalTrains) {
        this.totalTrains = totalTrains;
    }

    public long getWaitingTrains() {
        return waitingTrains;
    }

    public void setWaitingTrains(long waitingTrains) {
        this.waitingTrains = waitingTrains;
    }

    public long getActiveResourceAllocations() {
        return activeResourceAllocations;
    }

    public void setActiveResourceAllocations(long activeResourceAllocations) {
        this.activeResourceAllocations = activeResourceAllocations;
    }

    public long getOpenConflicts() {
        return openConflicts;
    }

    public void setOpenConflicts(long openConflicts) {
        this.openConflicts = openConflicts;
    }

    public long getDeadlockConflicts() {
        return deadlockConflicts;
    }

    public void setDeadlockConflicts(long deadlockConflicts) {
        this.deadlockConflicts = deadlockConflicts;
    }

    public long getAiPredictionCount() {
        return aiPredictionCount;
    }

    public void setAiPredictionCount(long aiPredictionCount) {
        this.aiPredictionCount = aiPredictionCount;
    }

    public Double getAveragePredictedDelayMinutes() {
        return averagePredictedDelayMinutes;
    }

    public void setAveragePredictedDelayMinutes(Double averagePredictedDelayMinutes) {
        this.averagePredictedDelayMinutes = averagePredictedDelayMinutes;
    }

    public Double getAverageActualDelayMinutes() {
        return averageActualDelayMinutes;
    }

    public void setAverageActualDelayMinutes(Double averageActualDelayMinutes) {
        this.averageActualDelayMinutes = averageActualDelayMinutes;
    }
}
