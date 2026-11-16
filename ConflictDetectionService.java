package com.railops.resource;

import com.railops.deadlock.DeadlockDetectionService;
import com.railops.event.SimulationEventService;
import com.railops.event.SimulationEventType;
import com.railops.simulation.Simulation;
import com.railops.simulation.SimulationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConflictDetectionService {

    private final SimulationRepository simulationRepository;
    private final ResourceRequestRepository requestRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final ConflictRepository conflictRepository;
    private final SimulationEventService simulationEventService;
    private final DeadlockDetectionService deadlockDetectionService;

    public ConflictDetectionService(SimulationRepository simulationRepository,
                                   ResourceRequestRepository requestRepository,
                                   ResourceAllocationRepository allocationRepository,
                                   ConflictRepository conflictRepository,
                                   SimulationEventService simulationEventService,
                                   DeadlockDetectionService deadlockDetectionService) {
        this.simulationRepository = simulationRepository;
        this.requestRepository = requestRepository;
        this.allocationRepository = allocationRepository;
        this.conflictRepository = conflictRepository;
        this.simulationEventService = simulationEventService;
        this.deadlockDetectionService = deadlockDetectionService;
    }

    @Transactional
    public List<Conflict> detectConflicts(Long simulationId) {
        Simulation simulation = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new IllegalArgumentException("Simulation not found: " + simulationId));

        List<ResourceRequest> pendingRequests = requestRepository.findBySimulationIdOrderByRequestSequenceAsc(simulationId)
                .stream()
                .filter(request -> request.getStatus() == ResourceRequestStatus.PENDING)
                .toList();

        List<ResourceAllocation> activeAllocations = allocationRepository.findBySimulationId(simulationId)
                .stream()
                .filter(allocation -> allocation.getStatus() == ResourceAllocationStatus.ACTIVE)
                .toList();

        Set<String> existingSummaries = conflictRepository.findBySimulationIdOrderByDetectedAtAsc(simulationId)
                .stream()
                .filter(conflict -> conflict.getStatus() == ConflictStatus.OPEN)
                .map(Conflict::getSummary)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Conflict> detected = new ArrayList<>();

        for (ResourceRequest request : pendingRequests) {
            List<ResourceAllocation> holders = activeAllocations.stream()
                    .filter(allocation -> allocation.getResource().getId().equals(request.getResource().getId()))
                    .filter(allocation -> !allocation.getTrainId().equals(request.getTrainId()))
                    .toList();

            if (holders.isEmpty()) {
                continue;
            }

            for (ResourceAllocation holder : holders) {
                String summary = "Train " + request.getTrainId() + " waiting for resource "
                        + request.getResource().getCode() + " held by train " + holder.getTrainId();
                if (!existingSummaries.contains(summary)) {
                    Conflict conflict = new Conflict(simulation, "WAITING", summary,
                            "Train " + request.getTrainId() + " requested resource " + request.getResource().getCode()
                                    + " while train " + holder.getTrainId() + " still held that resource.");
                    conflict.setStatus(ConflictStatus.OPEN);
                    conflict.setResourceId(request.getResource().getId());
                    conflict.setAffectedTrainIds(request.getTrainId() + "," + holder.getTrainId());
                    conflictRepository.save(conflict);
                    simulationEventService.recordEvent(simulation, SimulationEventType.CONFLICT_DETECTED,
                            "Resource waiting conflict", conflict.getDetails());
                    existingSummaries.add(summary);
                    detected.add(conflict);
                }
            }
        }

        deadlockDetectionService.detectDeadlocks(simulationId);

        List<Conflict> persistedDeadlocks = conflictRepository.findBySimulationIdOrderByDetectedAtAsc(simulationId)
                .stream()
                .filter(conflict -> conflict.getStatus() == ConflictStatus.OPEN)
                .filter(conflict -> "DEADLOCK".equals(conflict.getConflictType()))
                .toList();

        detected.addAll(persistedDeadlocks);
        return detected;
    }
}
