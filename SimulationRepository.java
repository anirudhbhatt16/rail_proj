package com.railops.simulation;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SimulationRepository extends JpaRepository<Simulation, Long> {

    List<Simulation> findByNetworkId(Long networkId);

    long countByStatus(SimulationStatus status);

    @Query("select s from Simulation s where s.status = :status")
    List<Simulation> findByStatus(@Param("status") SimulationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Simulation s where s.id = :id")
    Optional<Simulation> findByIdForUpdate(@Param("id") Long id);
}
