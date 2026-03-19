package com.oprationPlanManagement.operatorPlanService.dto.responseDTO;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class OperatorResponseDTO implements Serializable {

	private final long serialIzableID = 1L;
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	
}
