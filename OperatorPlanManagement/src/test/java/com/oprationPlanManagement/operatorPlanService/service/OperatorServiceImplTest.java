package com.oprationPlanManagement.operatorPlanService.service;

import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.OperatorRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.OperatorResponseDTO;
import com.oprationPlanManagement.operatorPlanService.entity.OperatorEntity;
import com.oprationPlanManagement.operatorPlanService.mapper.Mapper;
import com.oprationPlanManagement.operatorPlanService.repository.IOperatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperatorServiceImplTest {

    @Mock
    private IOperatorRepository operatorRepo;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private OperatorServiceImpl operatorService;

    private OperatorEntity operatorEntity;
    private OperatorRequestDTO requestDTO;
    private OperatorResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        operatorEntity = new OperatorEntity(1L, "Airtel");
        requestDTO = new OperatorRequestDTO("Airtel");
        responseDTO = new OperatorResponseDTO("Airtel");
    }

    // ══════════════════════════════════════════════════════
    // saveOper()
    // ══════════════════════════════════════════════════════

    @Test
    @DisplayName("saveOper: should save and return OperatorResponseDTO")
    void saveOper_ShouldSaveAndReturnResponse() {
        when(mapper.dtoToOperatorEntity(requestDTO)).thenReturn(operatorEntity);
        when(operatorRepo.save(operatorEntity)).thenReturn(operatorEntity);

        OperatorResponseDTO result = operatorService.saveOper(requestDTO);

        assertNotNull(result);
        assertEquals("Airtel", result.getName());
        verify(operatorRepo).save(operatorEntity);
    }

    // ══════════════════════════════════════════════════════
    // getOper()
    // ══════════════════════════════════════════════════════

    @Test
    @DisplayName("getOper: should return operator when found by ID")
    void getOper_ShouldReturnOperator_WhenFound() {
        when(operatorRepo.findById(1L)).thenReturn(Optional.of(operatorEntity));

        OperatorResponseDTO result = operatorService.getOper(1L);

        assertNotNull(result);
        assertEquals("Airtel", result.getName());
    }

    @Test
    @DisplayName("getOper: should throw RuntimeException when operator not found")
    void getOper_ShouldThrowException_WhenNotFound() {
        when(operatorRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> operatorService.getOper(99L));
    }

    // ══════════════════════════════════════════════════════
    // getOperList()
    // ══════════════════════════════════════════════════════

    @Test
    @DisplayName("getOperList: should return list of all operators")
    void getOperList_ShouldReturnAllOperators() {
        OperatorEntity entity2 = new OperatorEntity(2L, "Jio");
        when(operatorRepo.findAll()).thenReturn(List.of(operatorEntity, entity2));

        List<OperatorResponseDTO> result = operatorService.getOperList();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Airtel", result.get(0).getName());
        assertEquals("Jio", result.get(1).getName());
    }

    @Test
    @DisplayName("getOperList: should return empty list when no operators exist")
    void getOperList_ShouldReturnEmptyList_WhenNoOperators() {
        when(operatorRepo.findAll()).thenReturn(List.of());

        List<OperatorResponseDTO> result = operatorService.getOperList();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ══════════════════════════════════════════════════════
    // updateResponse()
    // ══════════════════════════════════════════════════════

    @Test
    @DisplayName("updateResponse: should update operator name and return updated DTO")
    void updateResponse_ShouldUpdateAndReturn_WhenFound() {
        OperatorRequestDTO updateDTO = new OperatorRequestDTO("Jio");
        when(operatorRepo.findById(1L)).thenReturn(Optional.of(operatorEntity));
        when(operatorRepo.save(operatorEntity)).thenReturn(operatorEntity);

        OperatorResponseDTO result = operatorService.updateResponse(1L, updateDTO);

        assertNotNull(result);
        assertEquals("Jio", operatorEntity.getName()); // name was updated on the entity
        verify(operatorRepo).save(operatorEntity);
    }

    @Test
    @DisplayName("updateResponse: should throw RuntimeException when operator not found")
    void updateResponse_ShouldThrowException_WhenNotFound() {
        when(operatorRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> operatorService.updateResponse(99L, requestDTO));
        verify(operatorRepo, never()).save(any());
    }

    // ══════════════════════════════════════════════════════
    // deleteOper()
    // ══════════════════════════════════════════════════════

    @Test
    @DisplayName("deleteOper: should delete operator when it exists")
    void deleteOper_ShouldDelete_WhenExists() {
        when(operatorRepo.existsById(1L)).thenReturn(true);
        doNothing().when(operatorRepo).deleteById(1L);

        assertDoesNotThrow(() -> operatorService.deleteOper(1L));
        verify(operatorRepo).deleteById(1L);
    }

    @Test
    @DisplayName("deleteOper: should throw RuntimeException when operator not found")
    void deleteOper_ShouldThrowException_WhenNotFound() {
        when(operatorRepo.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> operatorService.deleteOper(99L));
        verify(operatorRepo, never()).deleteById(any());
    }
}