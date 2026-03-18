package com.oprationPlanManagement.operatorPlanService.service;

import java.util.List;

import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.PlanRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.PlanResponseDTO;

public interface IPlanService {

	public PlanResponseDTO addNewPlan(PlanRequestDTO dto);
	public List<PlanResponseDTO> getPlanList();
	public PlanResponseDTO getPlan(long id);
	public PlanResponseDTO updatePlan(long id, PlanRequestDTO dto);
	public void deletePlan(long id, PlanRequestDTO dto);
}
