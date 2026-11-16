package com.railops.ai;

import com.railops.simulation.Simulation;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "ai_predictions",
        indexes = {
                @Index(name = "idx_ai_predictions_status", columnList = "status"),
                @Index(name = "idx_ai_predictions_created_at", columnList = "created_at"),
                @Index(name = "idx_ai_predictions_simulation", columnList = "simulation_id"),
                @Index(name = "idx_ai_predictions_train", columnList = "train_id")
        })
public class AiPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id")
    private Simulation simulation;

    @Column(name = "train_id")
    private Long trainId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AiPredictionStatus status = AiPredictionStatus.SUCCESS;

    @Column(name = "model_version", length = 64)
    private String modelVersion;

    @Column(name = "prediction_result")
    private Double predictionResult;

    @Column(name = "feature_snapshot", columnDefinition = "TEXT")
    private String featureSnapshot;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AiPrediction() {
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    public Long getTrainId() {
        return trainId;
    }

    public void setTrainId(Long trainId) {
        this.trainId = trainId;
    }

    public AiPredictionStatus getStatus() {
        return status;
    }

    public void setStatus(AiPredictionStatus status) {
        this.status = status;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Double getPredictionResult() {
        return predictionResult;
    }

    public void setPredictionResult(Double predictionResult) {
        this.predictionResult = predictionResult;
    }

    public String getFeatureSnapshot() {
        return featureSnapshot;
    }

    public void setFeatureSnapshot(String featureSnapshot) {
        this.featureSnapshot = featureSnapshot;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
