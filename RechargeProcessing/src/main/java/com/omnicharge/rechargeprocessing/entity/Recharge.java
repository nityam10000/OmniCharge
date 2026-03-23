package com.omnicharge.rechargeprocessing.entity;


import com.omnicharge.rechargeprocessing.enums.RechargeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Recharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RechargeStatus status;

    private Long userId;

    private Long planId;

	public Long getId() {
		return id;
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
