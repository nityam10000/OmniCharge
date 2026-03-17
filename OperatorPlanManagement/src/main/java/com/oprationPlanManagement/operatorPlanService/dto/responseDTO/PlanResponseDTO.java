package com.oprationPlanManagement.operatorPlanService.dto.responseDTO;

import java.math.BigDecimal;

public class PlanResponseDTO {
	
	private BigDecimal amount;
	private String validity;
	private String data;
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getValidity() {
		return validity;
	}
	public void setValidity(String validity) {
		this.validity = validity;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public PlanResponseDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public PlanResponseDTO(BigDecimal amount, String validity, String data) {
		super();
		this.amount = amount;
		this.validity = validity;
		this.data = data;
	}
	
	
}
