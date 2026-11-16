package com.railops.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiPredictionResponse {

    @JsonProperty("predictedDelayMinutes")
    private Double predictedDelayMinutes;

    @JsonProperty("modelVersion")
    private String modelVersion;

    @JsonProperty("features")
    private Map<String, Object> features = new LinkedHashMap<>();

    @JsonProperty("status")
    private String status;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("failureReason")
    private String failureReason;

    public AiPredictionResponse() {
    }

    public Double getPredictedDelayMinutes() {
        return predictedDelayMinutes;
    }

    public void setPredictedDelayMinutes(Double predictedDelayMinutes) {
        this.predictedDelayMinutes = predictedDelayMinutes;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Map<String, Object> getFeatures() {
        return features;
    }

    public void setFeatures(Map<String, Object> features) {
        this.features = features == null ? new LinkedHashMap<>() : features;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
