package com.omnicharge.rechargeprocessing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PlanResponseDTO {
	
	private Double amount;
	private String validity;
	private String description;
	
}