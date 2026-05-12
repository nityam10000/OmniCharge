package com.oprationPlanManagement.operatorPlanService.dto.responseDTO;

import java.io.Serializable;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PlanResponseDTO implements Serializable{
        private Long id;
        private Long operatorId;
        private Double amount;
        private String validity;
        private String description;
        private String planName;
        
}
