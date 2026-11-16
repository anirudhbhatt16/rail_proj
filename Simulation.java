package com.railops.simulation;

import com.railops.network.Network;
import com.railops.scheduling.SchedulingPolicy;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "simulations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_simulations_network_code", columnNames = {"network_id", "code"})
        },
        indexes = {
                @Index(name = "idx_simulations_network", columnList = "network_id"),
                @Index(name = "idx_simulations_status", columnList = "status")
        })
public class Simulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "network_id", nullable = false)
    private Network network;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SimulationStatus status = SimulationStatus.CREATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "scheduling_policy", nullable = false, length = 16)
    private SchedulingPolicy schedulingPolicy = SchedulingPolicy.FCFS;

    @Column(name = "base_step_seconds", nullable = false)
    private Integer baseStepSeconds = 60;

    @Column(name = "virtual_time_seconds", nullable = false)
    private Long virtualTimeSeconds = 0L;

    @Column(nullable = false)
    private Integer speed = 1;

    @Column(name = "aging_interval_ticks", nullable = false)
    private Integer agingIntervalTicks = 5;

    @Column(name = "next_request_sequence", nullable = false)
    private Long nextRequestSequence = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "initialized_at")
    private Instant initializedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected Simulation() {
    }

    public Simulation(Network network, String name, String code, SchedulingPolicy schedulingPolicy,
                     Integer baseStepSeconds, Integer speed, Integer agingIntervalTicks) {
        this.network = network;
        this.name = name;
        this.code = code;
        this.schedulingPolicy = schedulingPolicy;
        this.baseStepSeconds = baseStepSeconds;
        this.speed = speed;
        this.agingIntervalTicks = agingIntervalTicks;
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

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public SimulationStatus getStatus() {
        return status;
    }

    public void setStatus(SimulationStatus status) {
        this.status = status;
    }

    public SchedulingPolicy getSchedulingPolicy() {
        return schedulingPolicy;
    }

    public void setSchedulingPolicy(SchedulingPolicy schedulingPolicy) {
        this.schedulingPolicy = schedulingPolicy;
    }

    public Integer getBaseStepSeconds() {
        return baseStepSeconds;
    }

    public void setBaseStepSeconds(Integer baseStepSeconds) {
        this.baseStepSeconds = baseStepSeconds;
    }

    public Long getVirtualTimeSeconds() {
        return virtualTimeSeconds;
    }

    public void setVirtualTimeSeconds(Long virtualTimeSeconds) {
        this.virtualTimeSeconds = virtualTimeSeconds;
    }

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public Integer getAgingIntervalTicks() {
        return agingIntervalTicks;
    }

    public void setAgingIntervalTicks(Integer agingIntervalTicks) {
        this.agingIntervalTicks = agingIntervalTicks;
    }

    public Long getNextRequestSequence() {
        return nextRequestSequence;
    }

    public void setNextRequestSequence(Long nextRequestSequence) {
        this.nextRequestSequence = nextRequestSequence;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getInitializedAt() {
        return initializedAt;
    }

    public void setInitializedAt(Instant initializedAt) {
        this.initializedAt = initializedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
