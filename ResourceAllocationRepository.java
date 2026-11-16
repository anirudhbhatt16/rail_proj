package com.railops.resource;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, Long> {

    List<ResourceAllocation> findByResourceIdAndStatus(Long resourceId, ResourceAllocationStatus status);

    List<ResourceAllocation> findBySimulationId(Long simulationId);

    long countByStatus(ResourceAllocationStatus status);
}
