package com.oprationPlanManagement.operatorPlanService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.PlanRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.PlanResponseDTO;
import com.oprationPlanManagement.operatorPlanService.service.IPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PlanControllerTest {

    @Mock private IPlanService planService;
    @InjectMocks private PlanController planController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private PlanRequestDTO req;
    private PlanResponseDTO res;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(planController).build();
        objectMapper = new ObjectMapper();
        req = new PlanRequestDTO(299.0, "28 days", "Unlimited", 1L);
        res = new PlanResponseDTO(1L, 1L, 299.0, "28 days", "Unlimited");
    }

    @Test
    void createPlan_Returns200() throws Exception {
        when(planService.addNewPlan(any())).thenReturn(res);

        mockMvc.perform(post("/plans/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(299.0));
    }

    @Test
    void getAllPlans_Returns200() throws Exception {
        when(planService.getPlanList()).thenReturn(List.of(res));

        mockMvc.perform(get("/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getPlan_Returns200() throws Exception {
        when(planService.getPlan(1L)).thenReturn(res);

        mockMvc.perform(get("/plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validity").value("28 days"));
    }

    @Test
    void updatePlan_Returns200() throws Exception {
        when(planService.updatePlan(eq(1L), any())).thenReturn(res);

        mockMvc.perform(put("/plans/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void deletePlan_Returns204() throws Exception {
        doNothing().when(planService).deletePlan(eq(1L), any());

        mockMvc.perform(delete("/plans/delete/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }
}