package com.railops.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "railops.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationEngine {

    private static final Logger log = LoggerFactory.getLogger(SimulationEngine.class);

    private final SimulationService simulationService;

    public SimulationEngine(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @Scheduled(fixedRateString = "${simulation.tick-interval-ms:5000}")
    public void advanceRunningSimulations() {
        simulationService.tickAllRunningSimulations();
        log.debug("Simulation tick sweep completed");
    }
}
