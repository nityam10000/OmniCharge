package com.oprationPlanManagement.operatorPlanService.dto.responseDTO;

import java.io.Serializable;

public class OperatorResponseDTO implements Serializable {

	private final long serialIzableID = 1L;
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public OperatorResponseDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public OperatorResponseDTO(String name) {
		super();
		this.name = name;
	}
	
}
