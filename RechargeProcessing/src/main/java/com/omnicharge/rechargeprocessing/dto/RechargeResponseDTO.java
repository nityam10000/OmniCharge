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
    private Long id;
    private RechargeStatus status;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public RechargeStatus getStatus() {
		return status;
	}
	public void setStatus(RechargeStatus status) {
		this.status = status;
	}
    
}
