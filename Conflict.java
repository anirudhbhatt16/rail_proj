package com.railops.resource;

import com.railops.simulation.Simulation;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "conflicts",
        indexes = {
                @Index(name = "idx_conflicts_simulation", columnList = "simulation_id"),
                @Index(name = "idx_conflicts_status", columnList = "status"),
                @Index(name = "idx_conflicts_resource", columnList = "resource_id")
        })
public class Conflict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id", nullable = false)
    private Simulation simulation;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "track_id")
    private Long trackId;

    @Column(name = "conflict_type", nullable = false, length = 64)
    private String conflictType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ConflictStatus status = ConflictStatus.OPEN;

    @Column(nullable = false, length = 255)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "affected_train_ids", length = 255)
    private String affectedTrainIds;

    @Column(name = "detected_at", nullable = false, updatable = false)
    private Instant detectedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected Conflict() {
    }

    public Conflict(Simulation simulation, String conflictType, String summary, String details) {
        this.simulation = simulation;
        this.conflictType = conflictType;
        this.summary = summary;
        this.details = details;
    }

    @PrePersist
    protected void onCreate() {
        this.detectedAt = Instant.now();
        if (this.status == null) {
            this.status = ConflictStatus.OPEN;
        }
    }

    public Long getId() {
        return id;
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public String getConflictType() {
        return conflictType;
    }

    public String getSummary() {
        return summary;
    }

    public String getDetails() {
        return details;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public ConflictStatus getStatus() {
        return status;
    }

    public void setStatus(ConflictStatus status) {
        this.status = status;
    }

    public String getAffectedTrainIds() {
        return affectedTrainIds;
    }

    public void setAffectedTrainIds(String affectedTrainIds) {
        this.affectedTrainIds = affectedTrainIds;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
