package com.oprationPlanManagement.operatorPlanService.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@RequiredArgsConstructor
public class OperatorRequestDTO {
	
	@NotBlank(message = "Operator name is required")
    @Size(min = 2, max = 50, message = "Operator name must be between 2 and 50 characters")
    private String name;


}
