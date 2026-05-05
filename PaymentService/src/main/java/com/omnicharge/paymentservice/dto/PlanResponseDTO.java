package com.omnicharge.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanResponseDTO {
    private Long operatorId;
    private Double amount;
    private String validity;
    private String description;
}