package com.omnicharge.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomPaymentRequestDTO {

    @NotNull(message = "rechargeId must not be null")
    private Long rechargeId;

    @NotBlank(message = "paymentMethod must not be blank")
    private String paymentMethod;

    /**
     * Simulated payment gateway response.
     * Accepted values: "pass" (payment succeeds) or "fail" (payment fails).
     */
    @NotBlank(message = "paymentResponse must not be blank")
    @Pattern(regexp = "(?i)pass|fail", message = "paymentResponse must be 'pass' or 'fail'")
    private String paymentResponse;
}
