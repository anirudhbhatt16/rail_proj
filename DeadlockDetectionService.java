package com.railops.deadlock;

import com.railops.event.SimulationEventService;
import com.railops.event.SimulationEventType;
import com.railops.resource.Conflict;
import com.railops.resource.ConflictRepository;
import com.railops.resource.ConflictStatus;
import com.railops.resource.ResourceAllocation;
import com.railops.resource.ResourceAllocationRepository;
import com.railops.resource.ResourceRequest;
import com.railops.resource.ResourceRequestRepository;
import com.railops.resource.ResourceRequestStatus;
import com.railops.simulation.Simulation;
import com.railops.simulation.SimulationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DeadlockDetectionService {

    private final SimulationRepository simulationRepository;
    private final ResourceRequestRepository requestRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final ConflictRepository conflictRepository;
    private final SimulationEventService simulationEventService;
    private final CycleDetectionService cycleDetectionService;

    public DeadlockDetectionService(SimulationRepository simulationRepository,
                                   ResourceRequestRepository requestRepository,
                                   ResourceAllocationRepository allocationRepository,
                                   ConflictRepository conflictRepository,
                                   SimulationEventService simulationEventService,
                                   CycleDetectionService cycleDetectionService) {
        this.simulationRepository = simulationRepository;
        this.requestRepository = requestRepository;
        this.allocationRepository = allocationRepository;
        this.conflictRepository = conflictRepository;
        this.simulationEventService = simulationEventService;
        this.cycleDetectionService = cycleDetectionService;
    }

    @Transactional
    public List<DeadlockReport> detectDeadlocks(Long simulationId) {
        Simulation simulation = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new IllegalArgumentException("Simulation not found: " + simulationId));

        List<ResourceRequest> pending = requestRepository.findBySimulationIdOrderByRequestSequenceAsc(simulationId)
                .stream()
                .filter(request -> request.getStatus() == ResourceRequestStatus.PENDING)
                .toList();

        List<ResourceAllocation> activeAllocations = allocationRepository.findBySimulationId(simulationId)
                .stream()
                .filter(allocation -> allocation.getStatus() == com.railops.resource.ResourceAllocationStatus.ACTIVE)
                .toList();

        WaitForGraph waitForGraph = new WaitForGraph();
        for (ResourceRequest request : pending) {
            List<ResourceAllocation> holders = activeAllocations.stream()
                    .filter(allocation -> allocation.getResource().getId().equals(request.getResource().getId()))
                    .filter(allocation -> !allocation.getTrainId().equals(request.getTrainId()))
                    .toList();
            for (ResourceAllocation holder : holders) {
                waitForGraph.addEdge(request.getTrainId(), holder.getTrainId());
            }
        }

        List<DeadlockReport> reports = new ArrayList<>();
        for (List<Long> cycle : cycleDetectionService.detectCycles(waitForGraph.asMap())) {
            if (cycle.size() <= 1) {
                continue;
            }

            List<Long> involvedTrains = cycle.stream().distinct().sorted().toList();
            List<Long> involvedResources = pending.stream()
                    .filter(request -> involvedTrains.contains(request.getTrainId()))
                    .map(request -> request.getResource().getId())
                    .distinct()
                    .sorted()
                    .toList();

            String cycleText = cycle.stream().map(String::valueOf).collect(Collectors.joining(" -> ")) + " -> " + cycle.get(0);

            String summary = "Deadlock detected: " + cycleText;
            if (conflictRepository.findBySimulationIdOrderByDetectedAtAsc(simulationId).stream()
                    .noneMatch(conflict -> conflict.getStatus() == ConflictStatus.OPEN
                            && conflict.getConflictType().equals("DEADLOCK")
                            && conflict.getSummary().equals(summary))) {

                Conflict deadlock = new Conflict(simulation, "DEADLOCK", summary,
                        "Wait-for graph cycle detected: " + cycleText + ". The simulation must inspect active allocations and pending requests for this cycle.");
                deadlock.setStatus(ConflictStatus.OPEN);
                deadlock.setResourceId(involvedResources.isEmpty() ? null : involvedResources.get(0));
                deadlock.setAffectedTrainIds(involvedTrains.stream().map(String::valueOf).collect(Collectors.joining(",")));
                conflictRepository.save(deadlock);
                simulationEventService.recordEvent(simulation, SimulationEventType.CONFLICT_DETECTED,
                        "Deadlock detected", deadlock.getDetails());
            }

            reports.add(new DeadlockReport(involvedTrains, involvedResources, cycleText));
        }

        return reports;
    }
}
