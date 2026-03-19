package com.omnicharge.rechargeprocessing.dto;


import java.time.LocalDateTime;

import com.omnicharge.rechargeprocessing.enums.PaymentMethod;
import com.omnicharge.rechargeprocessing.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter

public class TransactionResponseDTO {
    private Double amount;
    private TransactionStatus transactionStatus;
    private PaymentMethod paymentMethod;
    private LocalDateTime timestamp;
	
}

