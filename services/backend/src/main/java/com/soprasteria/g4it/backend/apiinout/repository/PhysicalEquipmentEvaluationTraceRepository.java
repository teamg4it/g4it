// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apiinout.repository;

import com.soprasteria.g4it.backend.apiinout.modeldb.PhysicalEquipmentEvaluationTrace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

public interface PhysicalEquipmentEvaluationTraceRepository extends JpaRepository<PhysicalEquipmentEvaluationTrace, Long> {
    Page<PhysicalEquipmentEvaluationTrace> findByTaskId(Long taskId, Pageable pageable);

    Optional<PhysicalEquipmentEvaluationTrace> findByTaskIdAndSourceEquipmentId(Long taskId, Long sourceEquipmentId);

    List<PhysicalEquipmentEvaluationTrace> findByTaskIdAndSourceEquipmentIdIn(Long taskId, List<Long> sourceEquipmentIds);

    @Transactional
    @Modifying
    @Query("delete from PhysicalEquipmentEvaluationTrace trace where trace.taskId = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);
}
