package com.railops.resource;

import com.railops.event.SimulationEventDto;
import com.railops.simulation.SimulationService;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ResourceController {

    private final ResourceService resourceService;
    private final SimulationService simulationService;

    public ResourceController(ResourceService resourceService, SimulationService simulationService) {
        this.resourceService = resourceService;
        this.simulationService = simulationService;
    }

    @GetMapping("/resources")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public List<ResourceDto> getAllResources() {
        return resourceService.getAllResources();
    }

    @GetMapping("/resources/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public ResponseEntity<ResourceDto> getResource(@PathVariable("id") Long id) {
        return ResponseEntity.ok(resourceService.getResourceById(id));
    }

    @GetMapping("/simulations/{simulationId}/resources")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public List<ResourceDto> getResourcesForSimulation(@PathVariable("simulationId") Long simulationId) {
        return resourceService.getResourcesBySimulationId(simulationId);
    }

    @GetMapping("/resource-requests")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public List<ResourceRequestDto> getAllResourceRequests() {
        return resourceService.getAllResourceRequests();
    }

    @GetMapping("/simulations/{simulationId}/resource-requests")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public List<ResourceRequestDto> getResourceRequestsForSimulation(@PathVariable("simulationId") Long simulationId) {
        return resourceService.getResourceRequestsBySimulationId(simulationId);
    }

    @GetMapping("/scheduling/decisions")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public List<Object> getSchedulingDecisions() {
        return resourceService.getSchedulingDecisions();
    }

    @GetMapping("/simulations/{simulationId}/scheduling/decisions")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public List<Object> getSchedulingDecisionsForSimulation(@PathVariable("simulationId") Long simulationId) {
        return resourceService.getSchedulingDecisionsBySimulationId(simulationId);
    }

    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public List<SimulationEventDto> getEvents(@RequestParam(required = false) Long simulationId) {
        return resourceService.getEvents(simulationId);
    }

    @PostMapping("/simulations/{simulationId}/resources/{resourceId}/requests")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<ResourceRequestDto> enqueueRequest(@PathVariable("simulationId") Long simulationId,
                                                          @PathVariable("resourceId") Long resourceId,
                                                          @RequestParam Long trainId,
                                                          @RequestParam @Min(0) Integer priority) {
        return ResponseEntity.ok(ResourceRequestDto.from(simulationService.requestResource(simulationId, resourceId, trainId, priority)));
    }
}
