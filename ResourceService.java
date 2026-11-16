package com.railops.resource;

import com.railops.common.ResourceNotFoundException;
import com.railops.event.SimulationEventDto;
import com.railops.event.SimulationEventRepository;
import com.railops.event.SimulationEventService;
import com.railops.scheduling.SchedulingDecisionRepository;
import com.railops.simulation.SimulationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceRequestRepository requestRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final SchedulingDecisionRepository schedulingDecisionRepository;
    private final ConflictRepository conflictRepository;
    private final SimulationEventRepository simulationEventRepository;
    private final SimulationEventService simulationEventService;
    private final SimulationRepository simulationRepository;

    public ResourceService(ResourceRepository resourceRepository,
                           ResourceRequestRepository requestRepository,
                           ResourceAllocationRepository allocationRepository,
                           SchedulingDecisionRepository schedulingDecisionRepository,
                           ConflictRepository conflictRepository,
                           SimulationEventRepository simulationEventRepository,
                           SimulationEventService simulationEventService,
                           SimulationRepository simulationRepository) {
        this.resourceRepository = resourceRepository;
        this.requestRepository = requestRepository;
        this.allocationRepository = allocationRepository;
        this.schedulingDecisionRepository = schedulingDecisionRepository;
        this.conflictRepository = conflictRepository;
        this.simulationEventRepository = simulationEventRepository;
        this.simulationEventService = simulationEventService;
        this.simulationRepository = simulationRepository;
    }

    @Transactional(readOnly = true)
    public List<ResourceDto> getAllResources() {
        return resourceRepository.findAll().stream()
                .map(ResourceDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResourceDto> getResourcesBySimulationId(Long simulationId) {
        return resourceRepository.findBySimulationId(simulationId).stream()
                .map(ResourceDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResourceRequestDto> getAllResourceRequests() {
        return requestRepository.findAll().stream()
                .map(ResourceRequestDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResourceRequestDto> getResourceRequestsBySimulationId(Long simulationId) {
        return requestRepository.findBySimulationIdOrderByRequestSequenceAsc(simulationId).stream()
                .map(ResourceRequestDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Object> getSchedulingDecisions() {
        List<Object> decisions = new ArrayList<>();
        decisions.addAll(schedulingDecisionRepository.findAll());
        return decisions;
    }

    @Transactional(readOnly = true)
    public List<Object> getSchedulingDecisionsBySimulationId(Long simulationId) {
        List<Object> decisions = new ArrayList<>();
        decisions.addAll(schedulingDecisionRepository.findBySimulationIdOrderByDecisionOrderAsc(simulationId));
        return decisions;
    }

    @Transactional(readOnly = true)
    public List<Object> getConflicts() {
        List<Object> conflicts = new ArrayList<>();
        conflicts.addAll(conflictRepository.findAll());
        return conflicts;
    }

    @Transactional(readOnly = true)
    public List<Object> getConflictsBySimulationId(Long simulationId) {
        List<Object> conflicts = new ArrayList<>();
        conflicts.addAll(conflictRepository.findBySimulationIdOrderByDetectedAtAsc(simulationId));
        return conflicts;
    }

    @Transactional(readOnly = true)
    public List<SimulationEventDto> getEvents(Long simulationId) {
        if (simulationId != null) {
            return simulationEventRepository.findBySimulationIdOrderByCreatedAtAsc(simulationId).stream()
                    .map(SimulationEventDto::from)
                    .toList();
        }
        return simulationEventRepository.findAll().stream()
                .map(SimulationEventDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResourceDto getResourceById(Long id) {
        return resourceRepository.findById(id)
                .map(ResourceDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + id));
    }
}
