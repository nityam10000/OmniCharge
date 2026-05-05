package com.oprationPlanManagement.operatorPlanService.mapper;

import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.OperatorRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.PlanRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.OperatorResponseDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.PlanResponseDTO;
import com.oprationPlanManagement.operatorPlanService.entity.OperatorEntity;
import com.oprationPlanManagement.operatorPlanService.entity.PlanEntity;
import org.springframework.stereotype.Component;

@Component
public class Mapper {

	public OperatorResponseDTO operatotToDTO(OperatorEntity entity) {
		OperatorResponseDTO dto = new OperatorResponseDTO();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		return dto;
	}
	
	public PlanResponseDTO planToDTO(PlanEntity entity) {
		PlanResponseDTO dto = new PlanResponseDTO();
		dto.setId(entity.getId());
		dto.setOperatorId(entity.getOperatorId());
		dto.setAmount(entity.getAmount());
		dto.setValidity(entity.getValidity());
		dto.setDescription(entity.getDescription());
		return dto;
	}
	
	public OperatorEntity dtoToOperatorEntity(OperatorRequestDTO dto ) {
		OperatorEntity entity = new OperatorEntity();
		entity.setName(dto.getName());
		return entity;
	}

	public PlanEntity dtoToPlanEntity(PlanRequestDTO dto ){
		PlanEntity entity = new PlanEntity();
		entity.setAmount(dto.getAmount());
		entity.setValidity(dto.getValidity());
		entity.setDescription(dto.getDescription());
		entity.setOperatorId(dto.getOperatorId());
		return entity;
	}
}
