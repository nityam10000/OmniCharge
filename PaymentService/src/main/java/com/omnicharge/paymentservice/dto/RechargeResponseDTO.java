
package com.omnicharge.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RechargeResponseDTO {
    private Long rechargeId;
    private String status;
    private Double amount;
    private Long planId;
    private String transactionStatus;
    private Long userId;  // used for ownership validation in PaymentService

}