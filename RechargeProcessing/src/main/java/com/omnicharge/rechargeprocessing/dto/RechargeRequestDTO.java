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

	public RechargeRequestDTO(RechargeStatus status, @NotNull(message = "userId required!!!") Long userId,
			@NotNull(message = "planId required!!!") Long planId) {
		super();
		this.status = status;
		this.userId = userId;
		this.planId = planId;
	}

	public RechargeStatus getStatus() {
		return status;
	}

	public void setStatus(RechargeStatus status) {
		this.status = status;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getPlanId() {
		return planId;
	}

	public void setPlanId(Long planId) {
		this.planId = planId;
	}
    
    
}
