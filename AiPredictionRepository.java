package com.railops.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AiPredictionRepository extends JpaRepository<AiPrediction, Long> {

    List<AiPrediction> findAllByOrderByCreatedAtDesc();

    long countByStatus(AiPredictionStatus status);

    @Query("select avg(p.predictionResult) from AiPrediction p where p.status = :status")
    Double findAveragePredictionResultByStatus(@Param("status") AiPredictionStatus status);
}
