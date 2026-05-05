package com.oprationPlanManagement.operatorPlanService.service;

import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.PlanRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.PlanResponseDTO;
import com.oprationPlanManagement.operatorPlanService.entity.PlanEntity;
import com.oprationPlanManagement.operatorPlanService.mapper.Mapper;
import com.oprationPlanManagement.operatorPlanService.repository.IPlanRepository;
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
class PlanServiceImplTest {

    @Mock
    private IPlanRepository planRepo;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private PlanServiceImpl planService;

    private PlanEntity planEntity;
    private PlanRequestDTO requestDTO;
    private PlanResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        planEntity = new PlanEntity(1L, 299.0, "28 days", "Unlimited calls + 2GB/day", 1L);
        requestDTO = new PlanRequestDTO(299.0, "28 days", "Unlimited calls + 2GB/day", 1L);
        responseDTO = new PlanResponseDTO(1L, 1L, 299.0, "28 days", "Unlimited calls + 2GB/day");
    }

    // ══════════════════════════════════════════════════════
    // addNewPlan()
    // ══════════════════════════════════════════════════════

    @Test
    @DisplayName("addNewPlan: should save and return PlanResponseDTO")
    void addNewPlan_ShouldSaveAndReturn() {
        when(mapper.dtoToPlanEntity(requestDTO)).thenReturn(planEntity);
        when(planRepo.save(planEntity)).thenReturn(planEntity);
        when(mapper.planToDTO(planEntity)).thenReturn(responseDTO);

        PlanResponseDTO result = planService.addNewPlan(requestDTO);

        assertNotNull(result);
        assertEquals(299.0, result.getAmount());
        assertEquals("28 days", result.getValidity());
        verify(planRepo).save(planEntity);
    }

    // ══════════════════════════════════════════════════════
    // getPlanList()
    // ══════════════════════════════════════════════════════

    @Test
    @DisplayName("getPlanList: should return list of all plans")
    void getPlanList_ShouldReturnAllPlans() {
        PlanEntity plan2 = new PlanEntity(2L, 599.0, "84 days", "Unlimited + 3GB/day", 1L);
        PlanResponseDTO resp2 = new PlanResponseDTO(2L, 1L, 599.0, "84 days", "Unlimited + 3GB/day");
        when(planRepo.findAll()).thenReturn(List.of(planEntity, plan2));
        when(mapper.planToDTO(planEntity)).thenReturn(responseDTO);
        when(mapper.planToDTO(plan2)).thenReturn(resp2);

        List<PlanResponseDTO> result = planService.getPlanList();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(299.0, result.get(0).getAmount());
        assertEquals(599.0, result.get(1).getAmount());
    }

    @Test
    @DisplayName("getPlanList: should return empty list when no plans exist")
    void getPlanList_ShouldReturnEmpty_WhenNoPlans() {
        when(planRepo.findAll()).thenReturn(List.of());

        List<PlanResponseDTO> result = planService.getPlanList();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ══════════════════════════════════════════════════════
    // getPlan()
    // ══════════════════════════════════════════════════════

    @Test
    @DisplayName("getPlan: should return plan when found by ID")
    void getPlan_ShouldReturnPlan_WhenFound() {
        when(planRepo.findById(1L)).thenReturn(Optional.of(planEntity));
        when(mapper.planToDTO(planEntity)).thenReturn(responseDTO);

        PlanResponseDTO result = planService.getPlan(1L);

        assertNotNull(result);
        assertEquals(299.0, result.getAmount());
        assertEquals("28 days", result.getValidity());
    }

    @Test
    @DisplayName("getPlan: should throw RuntimeException when plan not found")
    void getPlan_ShouldThrowException_WhenNotFound() {
        when(planRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> planService.getPlan(99L));
    }

    // ══════════════════════════════════════════════════════
    // updatePlan()
    // ══════════════════════════════════════════════════════

    @Test
    @DisplayName("updatePlan: should update all fields and return updated DTO")
    void updatePlan_ShouldUpdateAndReturn_WhenFound() {
        PlanRequestDTO updateDTO = new PlanRequestDTO(399.0, "56 days", "Unlimited + 2.5GB/day", 1L);
        when(planRepo.findById(1L)).thenReturn(Optional.of(planEntity));
        when(planRepo.save(planEntity)).thenReturn(planEntity);
        when(mapper.planToDTO(planEntity)).thenReturn(new PlanResponseDTO(1L, 1L, 399.0, "56 days", "Unlimited + 2.5GB/day"));

        PlanResponseDTO result = planService.updatePlan(1L, updateDTO);

        assertNotNull(result);
        // verify fields were actually updated on the entity
        assertEquals(399.0, planEntity.getAmount());
        assertEquals("56 days", planEntity.getValidity());
        assertEquals("Unlimited + 2.5GB/day", planEntity.getDescription());
        verify(planRepo).save(planEntity);
    }

    @Test
    @DisplayName("updatePlan: should throw RuntimeException when plan not found")
    void updatePlan_ShouldThrowException_WhenNotFound() {
        when(planRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> planService.updatePlan(99L, requestDTO));
        verify(planRepo, never()).save(any());
    }

    // ══════════════════════════════════════════════════════
    // deletePlan()
    // ══════════════════════════════════════════════════════

    @Test
    @DisplayName("deletePlan: should delete plan when it exists")
    void deletePlan_ShouldDelete_WhenExists() {
        when(planRepo.existsById(1L)).thenReturn(true);
        doNothing().when(planRepo).deleteById(1L);

        assertDoesNotThrow(() -> planService.deletePlan(1L, requestDTO));
        verify(planRepo).deleteById(1L);
    }

    @Test
    @DisplayName("deletePlan: should throw RuntimeException when plan not found")
    void deletePlan_ShouldThrowException_WhenNotFound() {
        when(planRepo.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> planService.deletePlan(99L, requestDTO));
        verify(planRepo, never()).deleteById(any());
    }
}