package com.railops.resource;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResourceRepository extends JpaRepository<RailwayResource, Long> {

    List<RailwayResource> findBySimulationId(Long simulationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RailwayResource r where r.id = :id")
    Optional<RailwayResource> findByIdForUpdate(@Param("id") Long id);

    Optional<RailwayResource> findBySimulationIdAndTrackId(Long simulationId, Long trackId);
}
