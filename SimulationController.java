package com.railops.simulation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/simulations")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public List<SimulationResponse> listSimulations() {
        return simulationService.listSimulations();
    }

    @GetMapping("/simulations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public ResponseEntity<SimulationResponse> getSimulation(@PathVariable("id") Long id) {
        return ResponseEntity.ok(simulationService.getSimulation(id));
    }

    @PostMapping("/simulations")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<SimulationResponse> createSimulation(@Valid @RequestBody CreateSimulationRequest request) {
        return ResponseEntity.ok(simulationService.createSimulation(request));
    }

    @PostMapping("/simulations/{id}/initialize")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<SimulationResponse> initializeSimulation(@PathVariable("id") Long id) {
        return ResponseEntity.ok(simulationService.initializeSimulation(id));
    }

    @PostMapping("/simulations/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<SimulationResponse> startSimulation(@PathVariable("id") Long id) {
        return ResponseEntity.ok(simulationService.startSimulation(id));
    }

    @PostMapping("/simulations/{id}/pause")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<SimulationResponse> pauseSimulation(@PathVariable("id") Long id) {
        return ResponseEntity.ok(simulationService.pauseSimulation(id));
    }

    @PostMapping("/simulations/{id}/resume")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<SimulationResponse> resumeSimulation(@PathVariable("id") Long id) {
        return ResponseEntity.ok(simulationService.resumeSimulation(id));
    }

    @PostMapping("/simulations/{id}/stop")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<SimulationResponse> stopSimulation(@PathVariable("id") Long id) {
        return ResponseEntity.ok(simulationService.stopSimulation(id));
    }

    @GetMapping("/simulations/{id}/state")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public ResponseEntity<SimulationStateResponse> getSimulationState(@PathVariable("id") Long id) {
        return ResponseEntity.ok(simulationService.getSimulationState(id));
    }
}
