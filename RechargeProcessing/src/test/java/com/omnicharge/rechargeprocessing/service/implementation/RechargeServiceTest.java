package com.omnicharge.rechargeprocessing.service.implementation;

import com.omnicharge.rechargeprocessing.dto.PlanResponseDTO;
import com.omnicharge.rechargeprocessing.dto.RechargeRequestDTO;
import com.omnicharge.rechargeprocessing.dto.RechargeResponseDTO;
import com.omnicharge.rechargeprocessing.entity.Recharge;
import com.omnicharge.rechargeprocessing.enums.PaymentMethod;
import com.omnicharge.rechargeprocessing.enums.RechargeStatus;
import com.omnicharge.rechargeprocessing.exception.RechargeNotFoundException;
import com.omnicharge.rechargeprocessing.feignClient.IOperatorPlanClient;
import com.omnicharge.rechargeprocessing.mapper.RechargeMapper;
import com.omnicharge.rechargeprocessing.repository.IRechargeRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RechargeServiceTest {

    // ── Mocks ──────────────────────────────────────────────────────────────────
    @Mock private IRechargeRepository rechargeRepository;
    @Mock private RechargeMapper rechargeMapper;
    @Mock private IOperatorPlanClient operatorPlanClient; // Feign — no real HTTP call

    // ── System Under Test ──────────────────────────────────────────────────────
    @InjectMocks
    private RechargeService rechargeService;

    // ── Shared test data ───────────────────────────────────────────────────────
    private Recharge recharge;
    private RechargeRequestDTO requestDTO;
    private RechargeResponseDTO responseDTO;
    private PlanResponseDTO planResponseDTO;

    @BeforeEach
    void setUp() {
        recharge = new Recharge();
        recharge.setId(1L);
        recharge.setUserId(10L);
        recharge.setPlanId(5L);
        recharge.setStatus(RechargeStatus.PENDING);

        requestDTO = RechargeRequestDTO.builder()
                .planId(5L)
                .paymentMethod(PaymentMethod.UPI)
                .build();

        responseDTO = RechargeResponseDTO.builder()
                .rechargeId(1L)
                .status(RechargeStatus.PENDING)
                .planId(5L)
                .userId(10L)
                .build();

        planResponseDTO = new PlanResponseDTO(299.0, "28 days", "Unlimited calls + 2GB/day");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // addRecharge()
    // Reads X-User-Id header AND calls Feign client for plan details.
    // KEY FIX: create mock attributes BEFORE opening MockedStatic block
    // to avoid UnfinishedStubbingException.
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("addRecharge: should save recharge with PENDING status and attach plan amount")
    void addRecharge_ShouldSaveAndReturn_WithPlanAmount() {
        // Create mock BEFORE opening static block — avoids UnfinishedStubbingException
        ServletRequestAttributes attrs = mockServletAttributes("10");

        try (MockedStatic<RequestContextHolder> ctxHolder =
                     mockStatic(RequestContextHolder.class)) {

            ctxHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(attrs);

            when(operatorPlanClient.getPlanById(5L)).thenReturn(planResponseDTO);
            when(rechargeRepository.save(any(Recharge.class))).thenReturn(recharge);
            when(rechargeMapper.toRechargeResponseDTO(recharge)).thenReturn(responseDTO);

            RechargeResponseDTO result = rechargeService.addRecharge(requestDTO);

            assertNotNull(result);
            assertEquals(1L, result.getRechargeId());
            assertEquals(RechargeStatus.PENDING, result.getStatus());
            assertEquals(299.0, result.getAmount());                // set from plan
            assertEquals("PENDING", result.getTransactionStatus()); // set by service
            verify(operatorPlanClient).getPlanById(5L);             // Feign was called
            verify(rechargeRepository).save(any(Recharge.class));   // entity was saved
        }
    }

    @Test
    @DisplayName("addRechargeFallback: should throw RuntimeException when all retries exhausted")
    void addRechargeFallback_ShouldThrowRuntimeException() {
        Exception cause = new RuntimeException("OperatorPlanManagement is down");

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> rechargeService.addRechargeFallback(requestDTO, cause));

        assertTrue(thrown.getMessage().contains("Recharge creation failed after retries"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getRechargeById()
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getRechargeById: should return recharge when found by ID")
    void getRechargeById_ShouldReturnRecharge_WhenFound() {
        when(rechargeRepository.findById(1L)).thenReturn(Optional.of(recharge));
        when(rechargeMapper.toRechargeResponseDTO(recharge)).thenReturn(responseDTO);

        RechargeResponseDTO result = rechargeService.getRechargeById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getRechargeId());
        assertEquals(RechargeStatus.PENDING, result.getStatus());
    }

    @Test
    @DisplayName("getRechargeById: should throw RechargeNotFoundException when ID not found")
    void getRechargeById_ShouldThrowException_WhenNotFound() {
        when(rechargeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RechargeNotFoundException.class,
                () -> rechargeService.getRechargeById(99L));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getAllRechargs()
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getAllRechargs: should return paginated list of recharges")
    void getAllRechargs_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Recharge> page = new PageImpl<>(List.of(recharge));

        when(rechargeRepository.findAll(pageable)).thenReturn(page);
        when(rechargeMapper.toRechargeResponseDTO(recharge)).thenReturn(responseDTO);

        Page<RechargeResponseDTO> result = rechargeService.getAllRechargs(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getRechargeId());
    }

    @Test
    @DisplayName("getAllRechargs: should return empty page when no recharges exist")
    void getAllRechargs_ShouldReturnEmptyPage_WhenNone() {
        Pageable pageable = PageRequest.of(0, 10);
        when(rechargeRepository.findAll(pageable)).thenReturn(Page.empty());

        Page<RechargeResponseDTO> result = rechargeService.getAllRechargs(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // deleteRecharge()
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("deleteRecharge: should delete recharge and return success message")
    void deleteRecharge_ShouldDeleteAndReturnMessage() {
        when(rechargeRepository.findById(1L)).thenReturn(Optional.of(recharge));
        doNothing().when(rechargeRepository).delete(recharge);

        String result = rechargeService.deleteRecharge(1L);

        assertEquals("Recharge deleted!!", result);
        verify(rechargeRepository).delete(recharge);
    }

    @Test
    @DisplayName("deleteRecharge: should throw RechargeNotFoundException when ID not found")
    void deleteRecharge_ShouldThrowException_WhenNotFound() {
        when(rechargeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RechargeNotFoundException.class,
                () -> rechargeService.deleteRecharge(99L));
        verify(rechargeRepository, never()).delete(any());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // findAllRechargeByUserId()
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findAllRechargeByUserId: should return all recharges for a user")
    void findAllRechargeByUserId_ShouldReturnList() {
        when(rechargeRepository.findByUserId(10L)).thenReturn(List.of(recharge));
        when(rechargeMapper.toRechargeResponseDTO(recharge)).thenReturn(responseDTO);

        List<RechargeResponseDTO> result = rechargeService.findAllRechargeByUserId(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getUserId());
    }

    @Test
    @DisplayName("findAllRechargeByUserId: should return empty list when user has no recharges")
    void findAllRechargeByUserId_ShouldReturnEmpty_WhenNone() {
        when(rechargeRepository.findByUserId(99L)).thenReturn(List.of());

        List<RechargeResponseDTO> result = rechargeService.findAllRechargeByUserId(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // findAllRechargeByPlanId()
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findAllRechargeByPlanId: should return all recharges for a plan")
    void findAllRechargeByPlanId_ShouldReturnList() {
        when(rechargeRepository.findByPlanId(5L)).thenReturn(List.of(recharge));
        when(rechargeMapper.toRechargeResponseDTO(recharge)).thenReturn(responseDTO);

        List<RechargeResponseDTO> result = rechargeService.findAllRechargeByPlanId(5L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getPlanId());
    }

    @Test
    @DisplayName("findAllRechargeByPlanId: should return empty list when no recharges for plan")
    void findAllRechargeByPlanId_ShouldReturnEmpty_WhenNone() {
        when(rechargeRepository.findByPlanId(99L)).thenReturn(List.of());

        List<RechargeResponseDTO> result = rechargeService.findAllRechargeByPlanId(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getCurrentUserRecharges()
    // Also reads X-User-Id — same fix: create attrs BEFORE MockedStatic block
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getCurrentUserRecharges: should return recharges for the logged-in user")
    void getCurrentUserRecharges_ShouldReturnList() {
        ServletRequestAttributes attrs = mockServletAttributes("10");

        try (MockedStatic<RequestContextHolder> ctxHolder =
                     mockStatic(RequestContextHolder.class)) {

            ctxHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(attrs);

            when(rechargeRepository.findByUserId(10L)).thenReturn(List.of(recharge));
            when(rechargeMapper.toRechargeResponseDTO(recharge)).thenReturn(responseDTO);

            List<RechargeResponseDTO> result = rechargeService.getCurrentUserRecharges();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getRechargeId());
        }
    }

    @Test
    @DisplayName("getCurrentUserRecharges: should return empty list when user has no recharges")
    void getCurrentUserRecharges_ShouldReturnEmpty_WhenNone() {
        ServletRequestAttributes attrs = mockServletAttributes("10");

        try (MockedStatic<RequestContextHolder> ctxHolder =
                     mockStatic(RequestContextHolder.class)) {

            ctxHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(attrs);

            when(rechargeRepository.findByUserId(10L)).thenReturn(List.of());

            List<RechargeResponseDTO> result = rechargeService.getCurrentUserRecharges();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // updateRechargeStatus()
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updateRechargeStatus: should update status on entity and save")
    void updateRechargeStatus_ShouldUpdateAndSave_WhenFound() {
        when(rechargeRepository.findById(1L)).thenReturn(Optional.of(recharge));

        rechargeService.updateRechargeStatus(1L, RechargeStatus.SUCCESS);

        assertEquals(RechargeStatus.SUCCESS, recharge.getStatus()); // status was changed
        verify(rechargeRepository).save(recharge);                   // save was called
    }

    @Test
    @DisplayName("updateRechargeStatus: should throw RechargeNotFoundException when not found")
    void updateRechargeStatus_ShouldThrowException_WhenNotFound() {
        when(rechargeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RechargeNotFoundException.class,
                () -> rechargeService.updateRechargeStatus(99L, RechargeStatus.SUCCESS));
        verify(rechargeRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Helper
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Fakes the incoming HTTP request so getLoggedInUserId() can read X-User-Id.
     * IMPORTANT: always call this BEFORE opening a MockedStatic block —
     * creating mocks inside a static-mock block causes UnfinishedStubbingException.
     */
    private ServletRequestAttributes mockServletAttributes(String userId) {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        lenient().when(mockRequest.getHeader("X-User-Id")).thenReturn(userId);

        ServletRequestAttributes mockAttrs = mock(ServletRequestAttributes.class);
        lenient().when(mockAttrs.getRequest()).thenReturn(mockRequest);

        return mockAttrs;
    }
}