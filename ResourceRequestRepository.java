package com.railops.resource;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResourceRequestRepository extends JpaRepository<ResourceRequest, Long> {

    List<ResourceRequest> findBySimulationIdOrderByRequestSequenceAsc(Long simulationId);

    List<ResourceRequest> findByResourceIdOrderByRequestSequenceAsc(Long resourceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ResourceRequest r where r.resource.id = :resourceId and r.status = :status order by r.requestSequence asc")
    List<ResourceRequest> findPendingForUpdate(@Param("resourceId") Long resourceId,
                                              @Param("status") ResourceRequestStatus status);

    Optional<ResourceRequest> findFirstByResourceIdAndStatusOrderByRequestSequenceAsc(Long resourceId,
                                                                                   ResourceRequestStatus status);
}
