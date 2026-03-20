package com.omnicharge.rechargeprocessing.dto;

import com.omnicharge.rechargeprocessing.enums.RechargeStatus;
import jakarta.validation.constraints.NotNull;
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
public class RechargeRequestDTO {
    private RechargeStatus status;

    @NotNull(message = "planId required!!!")
    private Long planId;
    
    
}
