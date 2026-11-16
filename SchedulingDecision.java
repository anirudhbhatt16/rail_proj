package com.railops.scheduling;

import com.railops.resource.ResourceRequest;
import com.railops.resource.RailwayResource;
import com.railops.simulation.Simulation;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "scheduling_decisions",
        indexes = {
                @Index(name = "idx_decisions_simulation", columnList = "simulation_id"),
                @Index(name = "idx_decisions_resource", columnList = "resource_id"),
                @Index(name = "idx_decisions_request", columnList = "request_id")
        })
public class SchedulingDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id", nullable = false)
    private Simulation simulation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private RailwayResource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ResourceRequest request;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SchedulingPolicy policy;

    @Column(nullable = false)
    private Boolean granted = false;

    @Column(name = "decision_order", nullable = false)
    private Long decisionOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SchedulingDecision() {
    }

    public SchedulingDecision(Simulation simulation, RailwayResource resource,
                             ResourceRequest request, SchedulingPolicy policy,
                             Boolean granted, Long decisionOrder) {
        this.simulation = simulation;
        this.resource = resource;
        this.request = request;
        this.policy = policy;
        this.granted = granted;
        this.decisionOrder = decisionOrder;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
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

    public ResourceRequest getRequest() {
        return request;
    }

    public SchedulingPolicy getPolicy() {
        return policy;
    }

    public Boolean getGranted() {
        return granted;
    }

    public void setGranted(Boolean granted) {
        this.granted = granted;
    }

    public Long getDecisionOrder() {
        return decisionOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
