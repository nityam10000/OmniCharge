package com.oprationPlanManagement.operatorPlanService.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oprationPlanManagement.operatorPlanService.entity.PlanEntity;

public interface IPlanRepository extends JpaRepository<PlanEntity, Long> {

}
