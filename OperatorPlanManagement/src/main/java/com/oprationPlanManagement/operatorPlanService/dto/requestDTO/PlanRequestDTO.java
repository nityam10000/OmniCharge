package com.oprationPlanManagement.operatorPlanService.dto.requestDTO;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PlanRequestDTO {

	@NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be greater than 0")
    private Double amount;

    @NotBlank(message = "Validity is required")
    private String validity;

    @NotBlank(message = "Data field is required")
    private String description;

    @Positive(message = "Operator ID must be a positive number")
    private Long operatorId;

	
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public String getValidity() {
		return validity;
	}
	public void setValidity(String validity) {
		this.validity = validity;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public long getOperatorId() {
		return operatorId;
	}
	public void setOperatorId(long operatorId) {
		this.operatorId = operatorId;
	}
	public PlanRequestDTO(Double amount, String validity, String description, Long operatorId) {
		super();
		this.amount = amount;
		this.validity = validity;
		this.description = description;
		this.operatorId = operatorId;
	}
	public PlanRequestDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
