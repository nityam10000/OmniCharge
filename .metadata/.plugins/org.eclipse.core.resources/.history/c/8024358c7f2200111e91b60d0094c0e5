package com.oprationPlanManagement.operatorPlanService.mapper;

import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.OperatorRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.PlanRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.OperatorResponseDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.PlanResponseDTO;
import com.oprationPlanManagement.operatorPlanService.entity.OperatorEntity;
import com.oprationPlanManagement.operatorPlanService.entity.PlanEntity;

public class Mapper {

	public OperatorResponseDTO operatotToDTO(OperatorEntity entity) {
		return new OperatorResponseDTO(entity.getName());
	}
	
	public PlanResponseDTO planToDTO(PlanEntity entity) {
		return new PlanResponseDTO(
				entity.getAmount(),
				entity.getValidity(),
				entity.getData()
				);
	}
	
	public OperatorEntity dtoToOperatorEntity(OperatorRequestDTO dto ){
		return new OperatorEntity(
				dto.getName()
				);
	}
	
	public PlanEntity dtoToPlanEntity(PlanRequestDTO dto ){
		return new PlanEntity(
				dto.getAmount(),
				dto.getValidity(),
				dto.getData(),
				dto.getOperatorId()
				);
	}
}
