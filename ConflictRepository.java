package com.railops.resource;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConflictRepository extends JpaRepository<Conflict, Long> {

    List<Conflict> findBySimulationIdOrderByDetectedAtAsc(Long simulationId);

    List<Conflict> findBySimulationIdAndStatusOrderByDetectedAtAsc(Long simulationId, ConflictStatus status);

    long countByStatus(ConflictStatus status);

    long countByConflictType(String conflictType);
}
