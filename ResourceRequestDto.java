package com.railops.resource;

import java.time.Instant;

public record ResourceRequestDto(
        Long id,
        Long simulationId,
        Long resourceId,
        Long trainId,
        Integer trainPriority,
        Long requestSequence,
        Instant requestedAt,
        Long virtualTick,
        Integer effectivePriority,
        String status,
        Instant grantedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static ResourceRequestDto from(ResourceRequest request) {
        return new ResourceRequestDto(
                request.getId(),
                request.getSimulation() != null ? request.getSimulation().getId() : null,
                request.getResource() != null ? request.getResource().getId() : null,
                request.getTrainId(),
                request.getTrainPriority(),
                request.getRequestSequence(),
                request.getRequestedAt(),
                request.getVirtualTick(),
                request.getEffectivePriority(),
                request.getStatus() != null ? request.getStatus().name() : null,
                request.getGrantedAt(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}
