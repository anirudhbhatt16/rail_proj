package com.railops.event;

import java.time.Instant;

public record SimulationEventDto(
        Long id,
        Long simulationId,
        String eventType,
        String message,
        String details,
        Instant createdAt
) {
    public static SimulationEventDto from(SimulationEvent event) {
        return new SimulationEventDto(
                event.getId(),
                event.getSimulation() != null ? event.getSimulation().getId() : null,
                event.getEventType() != null ? event.getEventType().name() : null,
                event.getMessage(),
                event.getDetails(),
                event.getCreatedAt()
        );
    }
}
