package com.oprationPlanManagement.operatorPlanService.dto.requestDTO;


import java.math.BigDecimal;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter

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
	
}
