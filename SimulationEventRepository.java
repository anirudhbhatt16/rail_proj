package com.railops.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SimulationEventRepository extends JpaRepository<SimulationEvent, Long> {

    List<SimulationEvent> findBySimulationIdOrderByCreatedAtAsc(Long simulationId);

    List<SimulationEvent> findBySimulationIdOrderByCreatedAtDesc(Long simulationId);
}
