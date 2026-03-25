
package com.omnicharge.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RazorpayOrderRequestDTO {

    @NotNull(message = "rechargeId must not be null")
    private Long rechargeId;

    @NotBlank(message = "paymentMethod must not be blank")
    private String paymentMethod;

}