package com.railops.resource;

import com.railops.simulation.Simulation;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "resource_requests",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_requests_resource_sequence", columnNames = {"resource_id", "request_sequence"})
        },
        indexes = {
                @Index(name = "idx_requests_simulation", columnList = "simulation_id"),
                @Index(name = "idx_requests_resource", columnList = "resource_id"),
                @Index(name = "idx_requests_train", columnList = "train_id"),
                @Index(name = "idx_requests_status", columnList = "status")
        })
public class ResourceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id", nullable = false)
    private Simulation simulation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private RailwayResource resource;

    @Column(name = "train_id", nullable = false)
    private Long trainId;

    @Column(name = "train_priority", nullable = false)
    private Integer trainPriority;

    @Column(name = "request_sequence", nullable = false)
    private Long requestSequence;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "virtual_tick", nullable = false)
    private Long virtualTick;

    @Column(name = "effective_priority", nullable = false)
    private Integer effectivePriority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ResourceRequestStatus status = ResourceRequestStatus.PENDING;

    @Column(name = "granted_at")
    private Instant grantedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ResourceRequest() {
    }

    public ResourceRequest(Simulation simulation, RailwayResource resource, Long trainId,
                          Integer trainPriority, Long requestSequence, Long virtualTick) {
        this.simulation = simulation;
        this.resource = resource;
        this.trainId = trainId;
        this.trainPriority = trainPriority;
        this.requestSequence = requestSequence;
        this.requestedAt = Instant.now();
        this.virtualTick = virtualTick;
        this.effectivePriority = trainPriority;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.requestedAt == null) {
            this.requestedAt = now;
        }
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

    public RailwayResource getResource() {
        return resource;
    }

    public void setResource(RailwayResource resource) {
        this.resource = resource;
    }

    public Long getTrainId() {
        return trainId;
    }

    public Integer getTrainPriority() {
        return trainPriority;
    }

    public void setTrainPriority(Integer trainPriority) {
        this.trainPriority = trainPriority;
        this.effectivePriority = trainPriority;
    }

    public Long getRequestSequence() {
        return requestSequence;
    }

    public void setRequestSequence(Long requestSequence) {
        this.requestSequence = requestSequence;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Long getVirtualTick() {
        return virtualTick;
    }

    public void setVirtualTick(Long virtualTick) {
        this.virtualTick = virtualTick;
    }

    public Integer getEffectivePriority() {
        return effectivePriority;
    }

    public void setEffectivePriority(Integer effectivePriority) {
        this.effectivePriority = effectivePriority;
    }

    public ResourceRequestStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceRequestStatus status) {
        this.status = status;
    }

    public Instant getGrantedAt() {
        return grantedAt;
    }

    public void setGrantedAt(Instant grantedAt) {
        this.grantedAt = grantedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
