package com.oprationPlanManagement.operatorPlanService.dto.responseDTO;

import java.io.Serializable;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class OperatorResponseDTO implements Serializable {

	private final long serialIzableID = 2L;
	private String name;
	
}
