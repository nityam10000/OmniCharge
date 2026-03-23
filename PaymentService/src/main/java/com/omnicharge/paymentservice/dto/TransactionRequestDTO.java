package com.omnicharge.paymentservice.dto;

import com.omnicharge.paymentservice.enums.PaymentMethod;
import com.omnicharge.paymentservice.enums.TransactionStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransactionRequestDTO {
    @Min(value = 1, message = "Invalid amount!!")
    private Double amount;

    private Long rechargeId;

    @NotNull
    private PaymentMethod paymentMethod;


}
