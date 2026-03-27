package com.oprationPlanManagement.operatorPlanService.service;

import java.util.List;

import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.OperatorRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.OperatorResponseDTO;

public interface IOperatorService {
	public OperatorResponseDTO saveOper(OperatorRequestDTO dto);
	public OperatorResponseDTO getOper(long id);
	public List<OperatorResponseDTO> getOperList();
	public OperatorResponseDTO updateResponse(long id,OperatorRequestDTO dto);
	public void deleteOper(long id);
}