package com.omnicharge.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentVerifyRequestDTO {

    @NotBlank(message = "razorpayOrderId must not be blank")
    private String razorpayOrderId;

    @NotBlank(message = "razorpayPaymentId must not be blank")
    private String razorpayPaymentId;

    @NotBlank(message = "razorpaySignature must not be blank")
    private String razorpaySignature;
}