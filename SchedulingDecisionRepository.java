package com.railops.scheduling;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchedulingDecisionRepository extends JpaRepository<SchedulingDecision, Long> {

    List<SchedulingDecision> findBySimulationIdOrderByDecisionOrderAsc(Long simulationId);

    List<SchedulingDecision> findByResourceIdOrderByDecisionOrderAsc(Long resourceId);
}
