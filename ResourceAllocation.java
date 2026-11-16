package com.railops.resource;

import com.railops.simulation.Simulation;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "resource_allocations",
        indexes = {
                @Index(name = "idx_allocations_simulation", columnList = "simulation_id"),
                @Index(name = "idx_allocations_resource", columnList = "resource_id"),
                @Index(name = "idx_allocations_train", columnList = "train_id")
        })
public class ResourceAllocation {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ResourceAllocationStatus status = ResourceAllocationStatus.ACTIVE;

    @Column(name = "allocated_at", nullable = false, updatable = false)
    private Instant allocatedAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    protected ResourceAllocation() {
    }

    public ResourceAllocation(Simulation simulation, RailwayResource resource, Long trainId) {
        this.simulation = simulation;
        this.resource = resource;
        this.trainId = trainId;
    }

    @PrePersist
    protected void onCreate() {
        this.allocatedAt = Instant.now();
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

    public Long getTrainId() {
        return trainId;
    }

    public ResourceAllocationStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceAllocationStatus status) {
        this.status = status;
    }

    public Instant getAllocatedAt() {
        return allocatedAt;
    }

    public Instant getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(Instant releasedAt) {
        this.releasedAt = releasedAt;
    }
}
