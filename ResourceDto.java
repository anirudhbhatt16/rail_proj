package com.railops.resource;

import java.time.Instant;

public record ResourceDto(
        Long id,
        Long simulationId,
        Long trackId,
        String code,
        Integer capacity,
        Integer availableSlots,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static ResourceDto from(RailwayResource resource) {
        return new ResourceDto(
                resource.getId(),
                resource.getSimulation() != null ? resource.getSimulation().getId() : null,
                resource.getTrack() != null ? resource.getTrack().getId() : null,
                resource.getCode(),
                resource.getCapacity(),
                resource.getAvailableSlots(),
                resource.getStatus() != null ? resource.getStatus().name() : null,
                resource.getCreatedAt(),
                resource.getUpdatedAt()
        );
    }
}
