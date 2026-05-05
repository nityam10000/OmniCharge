package com.oprationPlanManagement.operatorPlanService.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.PlanRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.PlanResponseDTO;

public interface IPlanService {

	public PlanResponseDTO addNewPlan(PlanRequestDTO dto);
	public List<PlanResponseDTO> getPlanList();
	public Page<PlanResponseDTO> getPlanList(Pageable pageable);
	public List<PlanResponseDTO> getPlansByOperatorId(Long operatorId);
	public PlanResponseDTO getPlan(long id);
	public PlanResponseDTO updatePlan(long id, PlanRequestDTO dto);
	public void deletePlan(long id, PlanRequestDTO dto);
}
