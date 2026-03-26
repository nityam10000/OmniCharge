package com.omnicharge.rechargeprocessing.dto;

import com.omnicharge.rechargeprocessing.enums.RechargeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RechargeResponseDTO {
	private Long rechargeId;
	private RechargeStatus status;
	private Double amount;
	private Long planId;
	private String transactionStatus;
	private Long userId;  // exposed so PaymentService can validate ownership
}
