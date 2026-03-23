package com.omnicharge.paymentservice.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanResponseDTO implements Serializable{
	private final long serialIzableID = 1L;
    private Double amount;
    private String validity;
    private String description;
}