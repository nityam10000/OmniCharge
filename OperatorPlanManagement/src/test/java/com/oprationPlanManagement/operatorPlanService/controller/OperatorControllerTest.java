package com.oprationPlanManagement.operatorPlanService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.OperatorRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.OperatorResponseDTO;
import com.oprationPlanManagement.operatorPlanService.service.IOperatorService;
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
class OperatorControllerTest {

    @Mock private IOperatorService operatorService;
    @InjectMocks private OperatorController operatorController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(operatorController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createOperator_Returns200() throws Exception {
        OperatorRequestDTO req = new OperatorRequestDTO("Airtel");
        OperatorResponseDTO res = new OperatorResponseDTO(1L, "Airtel");
        when(operatorService.saveOper(any())).thenReturn(res);

        mockMvc.perform(post("/operators/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Airtel"));
    }

    @Test
    void getOperator_Returns200() throws Exception {
        when(operatorService.getOper(1L)).thenReturn(new OperatorResponseDTO(1L, "Airtel"));

        mockMvc.perform(get("/operators/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Airtel"));
    }

    @Test
    void getAllOperators_Returns200WithList() throws Exception {
        when(operatorService.getOperList()).thenReturn(
                List.of(new OperatorResponseDTO(2L, "Airtel"), new OperatorResponseDTO(1L, "Jio")));

        mockMvc.perform(get("/operators/getList"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateOperator_Returns200() throws Exception {
        OperatorRequestDTO req = new OperatorRequestDTO("Jio");
        when(operatorService.updateResponse(eq(1L), any())).thenReturn(new OperatorResponseDTO(1L, "Jio"));

        mockMvc.perform(put("/operators/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteOperator_Returns204() throws Exception {
        doNothing().when(operatorService).deleteOper(1L);

        mockMvc.perform(delete("/operators/delete/1"))
                .andExpect(status().isNoContent());
    }
}