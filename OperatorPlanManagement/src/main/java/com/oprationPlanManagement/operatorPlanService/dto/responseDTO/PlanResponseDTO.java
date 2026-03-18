package com.oprationPlanManagement.operatorPlanService.dto.responseDTO;

<<<<<<< HEAD

=======
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
>>>>>>> origin/bhavik
public class PlanResponseDTO {
	
	private Double amount;
	private String validity;
	private String description;
	
<<<<<<< HEAD
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
	public PlanResponseDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public PlanResponseDTO(Double amount, String validity, String description) {
		super();
		this.amount = amount;
		this.validity = validity;
		this.description = description;
	}
	
	
=======


>>>>>>> origin/bhavik
}
