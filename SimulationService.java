package com.railops.simulation;

import com.railops.common.InvalidStateException;
import com.railops.common.ResourceNotFoundException;
import com.railops.event.SimulationEventDto;
import com.railops.event.SimulationEventService;
import com.railops.event.SimulationEventType;
import com.railops.network.Network;
import com.railops.network.NetworkRepository;
import com.railops.resource.*;
import com.railops.scheduling.SchedulingPolicy;
import com.railops.scheduling.SchedulingDecision;
import com.railops.scheduling.SchedulingService;
import com.railops.track.Track;
import com.railops.track.TrackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SimulationService {

    private final SimulationRepository simulationRepository;
    private final NetworkRepository networkRepository;
    private final TrackRepository trackRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceRequestRepository resourceRequestRepository;
    private final ResourceAllocationRepository resourceAllocationRepository;
    private final ConflictRepository conflictRepository;
    private final SimulationEventService simulationEventService;
    private final SchedulingService schedulingService;
    private final ConflictDetectionService conflictDetectionService;
    private final Map<Long, ReentrantLock> simulationLocks = new ConcurrentHashMap<>();

    public SimulationService(SimulationRepository simulationRepository,
                            NetworkRepository networkRepository,
                            TrackRepository trackRepository,
                            ResourceRepository resourceRepository,
                            ResourceRequestRepository resourceRequestRepository,
                            ResourceAllocationRepository resourceAllocationRepository,
                            ConflictRepository conflictRepository,
                            SimulationEventService simulationEventService,
                            SchedulingService schedulingService,
                            ConflictDetectionService conflictDetectionService) {
        this.simulationRepository = simulationRepository;
        this.networkRepository = networkRepository;
        this.trackRepository = trackRepository;
        this.resourceRepository = resourceRepository;
        this.resourceRequestRepository = resourceRequestRepository;
        this.resourceAllocationRepository = resourceAllocationRepository;
        this.conflictRepository = conflictRepository;
        this.simulationEventService = simulationEventService;
        this.schedulingService = schedulingService;
        this.conflictDetectionService = conflictDetectionService;
    }

    @Transactional(readOnly = true)
    public List<SimulationResponse> listSimulations() {
        return simulationRepository.findAll().stream()
                .sorted(Comparator.comparing(Simulation::getId))
                .map(SimulationResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public SimulationResponse getSimulation(Long simulationId) {
        Simulation simulation = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new ResourceNotFoundException("Simulation not found: " + simulationId));
        return new SimulationResponse(simulation);
    }

    @Transactional
    public SimulationResponse createSimulation(CreateSimulationRequest request) {
        Network network = networkRepository.findById(request.getNetworkId())
                .orElseThrow(() -> new ResourceNotFoundException("Network not found: " + request.getNetworkId()));

        Simulation simulation = new Simulation(
                network,
                request.getName(),
                request.getCode(),
                request.getSchedulingPolicy() == null ? SchedulingPolicy.FCFS : request.getSchedulingPolicy(),
                request.getBaseStepSeconds() == null ? 60 : request.getBaseStepSeconds(),
                request.getSpeed() == null ? 1 : request.getSpeed(),
                request.getAgingIntervalTicks() == null ? 5 : request.getAgingIntervalTicks()
        );

        simulation = simulationRepository.save(simulation);
        simulationEventService.recordEvent(simulation, SimulationEventType.SIMULATION_CREATED,
                "Simulation created", "Simulation " + simulation.getCode() + " was created for network " + network.getCode());
        return new SimulationResponse(simulation);
    }

    @Transactional
    public SimulationResponse initializeSimulation(Long simulationId) {
        Simulation simulation = simulationRepository.findByIdForUpdate(simulationId)
                .orElseThrow(() -> new ResourceNotFoundException("Simulation not found: " + simulationId));

        if (simulation.getStatus() != SimulationStatus.CREATED) {
            throw new InvalidStateException("Simulation can only be initialized from CREATED state");
        }

        simulation.setStatus(SimulationStatus.INITIALIZED);
        simulation.setInitializedAt(Instant.now());
        createResourceInventory(simulation);
        simulationEventService.recordEvent(simulation, SimulationEventType.SIMULATION_INITIALIZED,
                "Simulation initialized", "Simulation became initialized and resource inventory was created");
        return new SimulationResponse(simulationRepository.save(simulation));
    }

    @Transactional
    public SimulationResponse startSimulation(Long simulationId) {
        Simulation simulation = simulationRepository.findByIdForUpdate(simulationId)
                .orElseThrow(() -> new ResourceNotFoundException("Simulation not found: " + simulationId));

        if (simulation.getStatus() == SimulationStatus.RUNNING) {
            throw new InvalidStateException("Simulation is already running");
        }
        if (simulation.getStatus() != SimulationStatus.INITIALIZED && simulation.getStatus() != SimulationStatus.PAUSED) {
            throw new InvalidStateException("Simulation must be initialized before it can start");
        }

        simulation.setStatus(SimulationStatus.RUNNING);
        simulation.setStartedAt(Instant.now());
        simulationEventService.recordEvent(simulation, SimulationEventType.SIMULATION_STARTED,
                "Simulation started", "Simulation started and tick processing began");
        return new SimulationResponse(simulationRepository.save(simulation));
    }

    @Transactional
    public SimulationResponse pauseSimulation(Long simulationId) {
        Simulation simulation = simulationRepository.findByIdForUpdate(simulationId)
                .orElseThrow(() -> new ResourceNotFoundException("Simulation not found: " + simulationId));

        if (simulation.getStatus() != SimulationStatus.RUNNING) {
            throw new InvalidStateException("Only a running simulation can be paused");
        }

        simulation.setStatus(SimulationStatus.PAUSED);
        simulationEventService.recordEvent(simulation, SimulationEventType.SIMULATION_PAUSED,
                "Simulation paused", "Simulation was paused by operator request");
        return new SimulationResponse(simulationRepository.save(simulation));
    }

    @Transactional
    public SimulationResponse resumeSimulation(Long simulationId) {
        Simulation simulation = simulationRepository.findByIdForUpdate(simulationId)
                .orElseThrow(() -> new ResourceNotFoundException("Simulation not found: " + simulationId));

        if (simulation.getStatus() != SimulationStatus.PAUSED) {
            throw new InvalidStateException("Only a paused simulation can be resumed");
        }

        simulation.setStatus(SimulationStatus.RUNNING);
        simulationEventService.recordEvent(simulation, SimulationEventType.SIMULATION_RESUMED,
                "Simulation resumed", "Simulation resumed from paused state");
        return new SimulationResponse(simulationRepository.save(simulation));
    }

    @Transactional
    public SimulationResponse stopSimulation(Long simulationId) {
        Simulation simulation = simulationRepository.findByIdForUpdate(simulationId)
                .orElseThrow(() -> new ResourceNotFoundException("Simulation not found: " + simulationId));

        if (simulation.getStatus() == SimulationStatus.STOPPED || simulation.getStatus() == SimulationStatus.COMPLETED) {
            throw new InvalidStateException("Simulation is already stopped or completed");
        }

        simulation.setStatus(SimulationStatus.STOPPED);
        simulation.setCompletedAt(Instant.now());
        releaseActiveAllocations(simulation);
        cancelPendingRequests(simulation);
        simulationEventService.recordEvent(simulation, SimulationEventType.SIMULATION_STOPPED,
                "Simulation stopped", "Simulation was stopped and active allocations were released");
        return new SimulationResponse(simulationRepository.save(simulation));
    }

    @Transactional(readOnly = true)
    public SimulationStateResponse getSimulationState(Long simulationId) {
        Simulation simulation = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new ResourceNotFoundException("Simulation not found: " + simulationId));

        List<ResourceDto> resources = resourceRepository.findBySimulationId(simulationId).stream()
                .map(ResourceDto::from)
                .toList();
        List<ResourceRequestDto> requests = resourceRequestRepository.findBySimulationIdOrderByRequestSequenceAsc(simulationId)
                .stream()
                .map(ResourceRequestDto::from)
                .toList();
        List<SimulationEventDto> events = simulationEventService.getEvents(simulationId).stream()
                .map(SimulationEventDto::from)
                .toList();

        return new SimulationStateResponse(new SimulationResponse(simulation), resources, requests, events);
    }

    @Transactional
    public void tickAllRunningSimulations() {
        List<Simulation> runningSimulations = simulationRepository.findByStatus(SimulationStatus.RUNNING);
        for (Simulation simulation : runningSimulations) {
            tickSimulation(simulation.getId());
        }
    }

    @Transactional
    public void tickSimulation(Long simulationId) {
        Simulation simulation = simulationRepository.findByIdForUpdate(simulationId)
                .orElseThrow(() -> new ResourceNotFoundException("Simulation not found: " + simulationId));

        if (simulation.getStatus() != SimulationStatus.RUNNING) {
            return;
        }

        long step = simulation.getBaseStepSeconds() * simulation.getSpeed();
        simulation.setVirtualTimeSeconds(simulation.getVirtualTimeSeconds() + step);
        simulationRepository.save(simulation);

        List<RailwayResource> resources = resourceRepository.findBySimulationId(simulationId);
        for (RailwayResource resource : resources) {
            List<ResourceRequest> requests = resourceRequestRepository.findByResourceIdOrderByRequestSequenceAsc(resource.getId());
            if (requests.isEmpty()) {
                continue;
            }
            ResourceRequest winner = schedulingService.selectWinner(simulation, resource, requests);
            if (winner != null && resource.getAvailableSlots() > 0) {
                grantResource(resource, winner, simulation);
            }
        }

        conflictDetectionService.detectConflicts(simulationId);
        simulationEventService.recordEvent(simulation, SimulationEventType.TICK_ADVANCED,
                "Tick advanced", "Simulation virtual time advanced by " + step + " seconds");
    }

    @Transactional
    public ResourceRequest requestResource(Long simulationId, Long resourceId, Long trainId, Integer trainPriority) {
        Simulation simulation = simulationRepository.findByIdForUpdate(simulationId)
                .orElseThrow(() -> new ResourceNotFoundException("Simulation not found: " + simulationId));
        RailwayResource resource = resourceRepository.findByIdForUpdate(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + resourceId));

        if (!resource.getSimulation().getId().equals(simulationId)) {
            throw new InvalidStateException("Resource does not belong to the simulation");
        }

        Long requestSequence = simulation.getNextRequestSequence() + 1L;
        simulation.setNextRequestSequence(requestSequence);
        ResourceRequest request = new ResourceRequest(simulation, resource, trainId, trainPriority, requestSequence, simulation.getVirtualTimeSeconds());
        request.setEffectivePriority(computeEffectivePriority(simulation, request));
        ResourceRequest saved = resourceRequestRepository.save(request);
        simulationEventService.recordEvent(simulation, SimulationEventType.RESOURCE_REQUESTED,
                "Resource request queued", "Train " + trainId + " requested resource " + resource.getCode());
        return saved;
    }

    @Transactional
    public void grantResource(RailwayResource resource, ResourceRequest request, Simulation simulation) {
        if (resource.getAvailableSlots() <= 0) {
            return;
        }

        resource.setAvailableSlots(resource.getAvailableSlots() - 1);
        request.setStatus(ResourceRequestStatus.GRANTED);
        request.setGrantedAt(Instant.now());
        resourceRequestRepository.save(request);

        ResourceAllocation allocation = new ResourceAllocation(simulation, resource, request.getTrainId());
        resourceAllocationRepository.save(allocation);
        resourceRepository.save(resource);

        schedulingService.recordDecision(simulation, resource, request, true);
        simulationEventService.recordEvent(simulation, SimulationEventType.RESOURCE_GRANTED,
                "Resource granted", "Train " + request.getTrainId() + " received access to resource " + resource.getCode());
    }

    private void createResourceInventory(Simulation simulation) {
        List<Track> tracks = trackRepository.findAllByNetworkId(simulation.getNetwork().getId());
        for (Track track : tracks) {
            RailwayResource resource = new RailwayResource(simulation, track, track.getCode(), track.getCapacity());
            if (resourceRepository.findBySimulationIdAndTrackId(simulation.getId(), track.getId()).isEmpty()) {
                resourceRepository.save(resource);
            }
        }
    }

    private void releaseActiveAllocations(Simulation simulation) {
        List<ResourceAllocation> active = resourceAllocationRepository.findBySimulationId(simulation.getId()).stream()
                .filter(allocation -> allocation.getStatus() == ResourceAllocationStatus.ACTIVE)
                .toList();

        for (ResourceAllocation allocation : active) {
            RailwayResource resource = allocation.getResource();
            resource.setAvailableSlots(Math.min(resource.getCapacity(), resource.getAvailableSlots() + 1));
            allocation.setStatus(ResourceAllocationStatus.RELEASED);
            allocation.setReleasedAt(Instant.now());
            resourceRepository.save(resource);
            resourceAllocationRepository.save(allocation);
            simulationEventService.recordEvent(simulation, SimulationEventType.RESOURCE_RELEASED,
                    "Resource released", "Train " + allocation.getTrainId() + " released resource " + resource.getCode());
        }
    }

    private void cancelPendingRequests(Simulation simulation) {
        List<ResourceRequest> pending = resourceRequestRepository.findBySimulationIdOrderByRequestSequenceAsc(simulation.getId()).stream()
                .filter(request -> request.getStatus() == ResourceRequestStatus.PENDING)
                .toList();

        for (ResourceRequest request : pending) {
            request.setStatus(ResourceRequestStatus.CANCELLED);
            resourceRequestRepository.save(request);
            simulationEventService.recordEvent(simulation, SimulationEventType.RESOURCE_DENIED,
                    "Request cancelled", "Resource request for train " + request.getTrainId() + " was cancelled when simulation stopped");
        }
    }

    private int computeEffectivePriority(Simulation simulation, ResourceRequest request) {
        long waitingTicks = Math.max(0, simulation.getVirtualTimeSeconds() - request.getVirtualTick());
        long agingIntervalTicks = Math.max(1, simulation.getAgingIntervalTicks());
        long agedPriority = request.getTrainPriority() + (waitingTicks / agingIntervalTicks);
        request.setEffectivePriority((int) agedPriority);
        return (int) agedPriority;
    }
}
