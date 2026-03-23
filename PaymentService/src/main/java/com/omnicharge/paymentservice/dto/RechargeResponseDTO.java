package com.omnicharge.paymentservice.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RechargeResponseDTO implements Serializable{
	private final long serialIzableID = 1L;
    private Long rechargeId;
    private String status;
    private Double amount;
    private Long planId;
    private String transactionStatus;
}