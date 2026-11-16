package com.railops.resource;

import com.railops.simulation.Simulation;
import com.railops.track.Track;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "resources",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_resources_simulation_track", columnNames = {"simulation_id", "track_id"}),
                @UniqueConstraint(name = "uk_resources_simulation_code", columnNames = {"simulation_id", "code"})
        },
        indexes = {
                @Index(name = "idx_resources_simulation", columnList = "simulation_id"),
                @Index(name = "idx_resources_track", columnList = "track_id"),
                @Index(name = "idx_resources_status", columnList = "status")
        })
public class RailwayResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id", nullable = false)
    private Simulation simulation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "available_slots", nullable = false)
    private Integer availableSlots;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ResourceStatus status = ResourceStatus.AVAILABLE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RailwayResource() {
    }

    public RailwayResource(Simulation simulation, Track track, String code, Integer capacity) {
        this.simulation = simulation;
        this.track = track;
        this.code = code;
        this.capacity = capacity;
        this.availableSlots = capacity;
        this.status = ResourceStatus.AVAILABLE;
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

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(Integer availableSlots) {
        this.availableSlots = availableSlots;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
