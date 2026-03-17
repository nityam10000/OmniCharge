package com.omnicharge.rechargeprocessing.dto;

import com.omnicharge.rechargeprocessing.enums.RechargeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RechargeRequestDTO {
    private RechargeStatus status;

    @NotNull(message = "userId required!!!")
    private Long userId;

    @NotNull(message = "planId required!!!")
    private Long planId;
}
