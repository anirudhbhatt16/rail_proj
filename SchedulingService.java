package com.railops.scheduling;

import com.railops.common.InvalidStateException;
import com.railops.resource.ResourceRequest;
import com.railops.resource.ResourceRequestStatus;
import com.railops.resource.RailwayResource;
import com.railops.simulation.Simulation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class SchedulingService {

    private final SchedulingDecisionRepository schedulingDecisionRepository;

    public SchedulingService(SchedulingDecisionRepository schedulingDecisionRepository) {
        this.schedulingDecisionRepository = schedulingDecisionRepository;
    }

    @Transactional(readOnly = true)
    public ResourceRequest selectWinner(Simulation simulation, RailwayResource resource, List<ResourceRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return null;
        }

        List<ResourceRequest> eligible = requests.stream()
                .filter(request -> request.getStatus() == ResourceRequestStatus.PENDING)
                .toList();

        if (eligible.isEmpty()) {
            return null;
        }

        Comparator<ResourceRequest> comparator;
        if (simulation.getSchedulingPolicy() == SchedulingPolicy.PRIORITY) {
            comparator = Comparator
                    .comparingInt(ResourceRequest::getEffectivePriority).reversed()
                    .thenComparingLong(ResourceRequest::getRequestSequence);
        } else {
            comparator = Comparator.comparingLong(ResourceRequest::getRequestSequence);
        }

        return eligible.stream().sorted(comparator).findFirst().orElseThrow(
                () -> new InvalidStateException("No eligible request could be selected for resource " + resource.getId())
        );
    }

    @Transactional
    public SchedulingDecision recordDecision(Simulation simulation, RailwayResource resource,
                                            ResourceRequest request, boolean granted) {
        Long decisionOrder = simulation.getNextRequestSequence() + 1L;
        SchedulingDecision decision = new SchedulingDecision(
                simulation,
                resource,
                request,
                simulation.getSchedulingPolicy(),
                granted,
                decisionOrder
        );
        schedulingDecisionRepository.save(decision);
        return decision;
    }

    @Transactional(readOnly = true)
    public List<SchedulingDecision> getDecisionHistory(Long simulationId) {
        return schedulingDecisionRepository.findBySimulationIdOrderByDecisionOrderAsc(simulationId);
    }
}
