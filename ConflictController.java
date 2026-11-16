package com.railops.resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ConflictController {

    private final ResourceService resourceService;

    public ConflictController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/conflicts")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public List<Object> getConflicts() {
        return resourceService.getConflicts();
    }

    @GetMapping("/simulations/{simulationId}/conflicts")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public List<Object> getConflictsForSimulation(@PathVariable("simulationId") Long simulationId) {
        return resourceService.getConflictsBySimulationId(simulationId);
    }
}
