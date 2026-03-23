package com.oprationPlanManagement.operatorPlanService.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oprationPlanManagement.operatorPlanService.entity.OperatorEntity;

public interface IOperatorRepository extends JpaRepository<OperatorEntity, Long> {
	
	
	
}
