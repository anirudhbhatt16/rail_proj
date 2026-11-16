package com.railops.event;

import com.railops.simulation.Simulation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SimulationEventService {

    private final SimulationEventRepository simulationEventRepository;

    public SimulationEventService(SimulationEventRepository simulationEventRepository) {
        this.simulationEventRepository = simulationEventRepository;
    }

    @Transactional
    public SimulationEvent recordEvent(Simulation simulation, SimulationEventType eventType,
                                      String message, String details) {
        SimulationEvent event = new SimulationEvent(simulation, eventType, message, details);
        return simulationEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<SimulationEvent> getEvents(Long simulationId) {
        return simulationEventRepository.findBySimulationIdOrderByCreatedAtAsc(simulationId);
    }
}
