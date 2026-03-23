package com.oprationPlanManagement.operatorPlanService.dto.responseDTO;

import java.io.Serializable;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PlanResponseDTO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private Double amount;
	private String validity;
	private String description;
	
}