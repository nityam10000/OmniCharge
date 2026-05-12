package com.oprationPlanManagement.operatorPlanService.mapper;

import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.OperatorRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.PlanRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.OperatorResponseDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.PlanResponseDTO;
import com.oprationPlanManagement.operatorPlanService.entity.OperatorEntity;
import com.oprationPlanManagement.operatorPlanService.entity.PlanEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    private final Mapper mapper = new Mapper();

    @Test
    void dtoToOperatorEntity_MapsNameCorrectly() {
        OperatorRequestDTO dto = new OperatorRequestDTO("Airtel");
        OperatorEntity entity = mapper.dtoToOperatorEntity(dto);
        assertEquals("Airtel", entity.getName());
    }

    @Test
    void operatorToDTO_MapsNameCorrectly() {
        OperatorEntity entity = new OperatorEntity(1L, "Jio");
        OperatorResponseDTO dto = mapper.operatotToDTO(entity);
        assertEquals("Jio", dto.getName());
    }

    @Test
    void dtoToPlanEntity_MapsAllFields() {
        PlanRequestDTO dto = new PlanRequestDTO(299.0, "28 days", "Unlimited", 2L, null);
        PlanEntity entity = mapper.dtoToPlanEntity(dto);
        assertEquals(299.0, entity.getAmount());
        assertEquals("28 days", entity.getValidity());
        assertEquals("Unlimited", entity.getDescription());
        assertEquals(2L, entity.getOperatorId());
    }

    @Test
    void planToDTO_MapsAllFields() {
        PlanEntity entity = new PlanEntity(1L, null, 199.0, "14 days", "Basic", 1L);
        PlanResponseDTO dto = mapper.planToDTO(entity);
        assertEquals(199.0, dto.getAmount());
        assertEquals("14 days", dto.getValidity());
        assertEquals("Basic", dto.getDescription());
    }
}