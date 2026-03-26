package com.omnicharge.rechargeprocessing.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PlanResponseDTO implements Serializable{
	private final long serialIzableID = 1L;
	private Double amount;
	private String validity;
	private String description;

}