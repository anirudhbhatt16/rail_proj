package com.railops.simulation;

import com.railops.scheduling.SchedulingPolicy;

import java.time.Instant;

public class SimulationResponse {

    private final Long id;
    private final Long networkId;
    private final String name;
    private final String code;
    private final SimulationStatus status;
    private final SchedulingPolicy schedulingPolicy;
    private final Integer baseStepSeconds;
    private final Long virtualTimeSeconds;
    private final Integer speed;
    private final Integer agingIntervalTicks;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant initializedAt;
    private final Instant startedAt;
    private final Instant completedAt;

    public SimulationResponse(Simulation simulation) {
        this.id = simulation.getId();
        this.networkId = simulation.getNetwork() != null ? simulation.getNetwork().getId() : null;
        this.name = simulation.getName();
        this.code = simulation.getCode();
        this.status = simulation.getStatus();
        this.schedulingPolicy = simulation.getSchedulingPolicy();
        this.baseStepSeconds = simulation.getBaseStepSeconds();
        this.virtualTimeSeconds = simulation.getVirtualTimeSeconds();
        this.speed = simulation.getSpeed();
        this.agingIntervalTicks = simulation.getAgingIntervalTicks();
        this.createdAt = simulation.getCreatedAt();
        this.updatedAt = simulation.getUpdatedAt();
        this.initializedAt = simulation.getInitializedAt();
        this.startedAt = simulation.getStartedAt();
        this.completedAt = simulation.getCompletedAt();
    }

    public Long getId() { return id; }
    public Long getNetworkId() { return networkId; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public SimulationStatus getStatus() { return status; }
    public SchedulingPolicy getSchedulingPolicy() { return schedulingPolicy; }
    public Integer getBaseStepSeconds() { return baseStepSeconds; }
    public Long getVirtualTimeSeconds() { return virtualTimeSeconds; }
    public Integer getSpeed() { return speed; }
    public Integer getAgingIntervalTicks() { return agingIntervalTicks; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getInitializedAt() { return initializedAt; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
}
