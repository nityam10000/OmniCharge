package com.oprationPlanManagement.operatorPlanService.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oprationPlanManagement.operatorPlanService.entity.PlanEntity;

import java.util.List;

public interface IPlanRepository extends JpaRepository<PlanEntity, Long> {
    List<PlanEntity> findByOperatorId(Long OperatorId);
}
