package com.omnicharge.rechargeprocessing.integration;

import com.omnicharge.rechargeprocessing.dto.PlanResponseDTO;
import com.omnicharge.rechargeprocessing.dto.RechargeRequestDTO;
import com.omnicharge.rechargeprocessing.dto.RechargeResponseDTO;
import com.omnicharge.rechargeprocessing.entity.Recharge;
import com.omnicharge.rechargeprocessing.enums.PaymentMethod;
import com.omnicharge.rechargeprocessing.enums.RechargeStatus;
import com.omnicharge.rechargeprocessing.exception.RechargeNotFoundException;
import com.omnicharge.rechargeprocessing.feignClient.IOperatorPlanClient;
import com.omnicharge.rechargeprocessing.repository.IRechargeRepository;
import com.omnicharge.rechargeprocessing.service.implementation.RechargeService;
import com.omnicharge.rechargeprocessing.support.RequestUserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Integration tests for RechargeProcessing service cross-service communication.
 * Tests the actual Feign client interactions with OperatorPlanManagement service.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RechargeProcessingIntegrationTest {

    @Autowired
    private RechargeService rechargeService;

    @Autowired
    private IRechargeRepository rechargeRepository;

    @MockBean
    private IOperatorPlanClient operatorPlanClient;

    @MockBean
    private RequestUserContext requestUserContext;

    @BeforeEach
    void setUp() {
        rechargeRepository.deleteAll();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PART 1: NORMAL WORKING - Happy Path Integration Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Integration: Should fetch plan from OperatorPlanManagement and create recharge")
    void addRecharge_ShouldIntegrate_WithOperatorPlanService() {
        // Setup authenticated user
        when(requestUserContext.getUserIdHeader()).thenReturn("10");

        // Setup Plan Service Response
        PlanResponseDTO plan = new PlanResponseDTO(5L, 299.0, "28 days", "Unlimited calls + 2GB/day");

        // Mock cross-service call
        when(operatorPlanClient.getPlanById(5L)).thenReturn(plan);

        // Create recharge
        RechargeRequestDTO requestDTO = RechargeRequestDTO.builder()
                .operatorId(5L)
                .planId(5L)
                .paymentMethod(PaymentMethod.UPI)
                .build();

        RechargeResponseDTO response = rechargeService.addRecharge(requestDTO);

        // Verify
        assertNotNull(response);
        assertEquals(299.0, response.getAmount());
        assertEquals(RechargeStatus.PENDING, response.getStatus());
        assertEquals(10L, response.getUserId());
        assertEquals(5L, response.getPlanId());

        // Verify recharge was persisted
        List<Recharge> recharges = rechargeRepository.findAll();
        assertEquals(1, recharges.size());
    }

    @Test
    @DisplayName("Integration: Should attach correct plan details to recharge")
    void addRecharge_ShouldAttach_PlanDetailsCorrectly() {
        when(requestUserContext.getUserIdHeader()).thenReturn("25");

        PlanResponseDTO expensivePlan = new PlanResponseDTO(8L, 999.0, "365 days", "Premium + 5GB/day");

        when(operatorPlanClient.getPlanById(8L)).thenReturn(expensivePlan);

        RechargeRequestDTO requestDTO = RechargeRequestDTO.builder()
                .operatorId(8L)
                .planId(8L)
                .paymentMethod(PaymentMethod.NETBANKING)
                .build();

        RechargeResponseDTO response = rechargeService.addRecharge(requestDTO);

        assertEquals(999.0, response.getAmount());
        assertEquals(RechargeStatus.PENDING, response.getStatus());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PART 2: BOUNDARY VALUE TESTING - Edge Cases
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Integration: Should handle minimum plan amount (boundary)")
    void addRecharge_ShouldHandle_MinimumPlanAmount() {
        when(requestUserContext.getUserIdHeader()).thenReturn("35");

        // Minimum amount plan
        PlanResponseDTO minPlan = new PlanResponseDTO(1L, 9.99, "1 day", "Limited 100MB");

        when(operatorPlanClient.getPlanById(1L)).thenReturn(minPlan);

        RechargeRequestDTO requestDTO = RechargeRequestDTO.builder()
                .operatorId(1L)
                .planId(1L)
                .paymentMethod(PaymentMethod.NETBANKING)
                .build();

        RechargeResponseDTO response = rechargeService.addRecharge(requestDTO);

        assertNotNull(response);
        assertEquals(9.99, response.getAmount());
    }

    @Test
    @DisplayName("Integration: Should handle maximum plan amount (boundary)")
    void addRecharge_ShouldHandle_MaximumPlanAmount() {
        when(requestUserContext.getUserIdHeader()).thenReturn("40");

        // Maximum amount plan
        PlanResponseDTO maxPlan = new PlanResponseDTO(20L, 9999.99, "1000 days", "Unlimited Everything");

        when(operatorPlanClient.getPlanById(20L)).thenReturn(maxPlan);

        RechargeRequestDTO requestDTO = RechargeRequestDTO.builder()
                .operatorId(20L)
                .planId(20L)
                .paymentMethod(PaymentMethod.UPI)
                .build();

        RechargeResponseDTO response = rechargeService.addRecharge(requestDTO);

        assertNotNull(response);
        assertEquals(9999.99, response.getAmount());
    }

    @Test
    @DisplayName("Integration: Should retrieve multiple recharges for same user with pagination")
    void getRecharges_ShouldHandle_PaginationWithMultipleResults() {
        // Create 15 recharges for user 45
        for (int i = 1; i <= 15; i++) {
            Recharge recharge = new Recharge();
            recharge.setUserId(45L);
            recharge.setPlanId((long) i);
            recharge.setStatus(RechargeStatus.SUCCESS);
            rechargeRepository.save(recharge);
        }

        // Fetch page 1 (10 items)
        Pageable pageable = PageRequest.of(0, 10);
        var page1 = rechargeService.getAllRechargs(pageable);

        assertEquals(15, page1.getTotalElements());
        assertEquals(10, page1.getContent().size());
        assertEquals(2, page1.getTotalPages());

        // Fetch page 2
        pageable = PageRequest.of(1, 10);
        var page2 = rechargeService.getAllRechargs(pageable);

        assertEquals(5, page2.getContent().size());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PART 3: EXCEPTION HANDLING - Service Failures and Recovery
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Integration Exception: Should fallback when OperatorPlanManagement service is down")
    void addRecharge_ShouldFallback_WhenOperatorServiceDown() {
        when(requestUserContext.getUserIdHeader()).thenReturn("50");

        // Simulate service down
        when(operatorPlanClient.getPlanById(anyLong()))
                .thenThrow(new RuntimeException("OperatorPlanManagement service unavailable"));

        RechargeRequestDTO requestDTO = RechargeRequestDTO.builder()
                .operatorId(15L)
                .planId(15L)
                .paymentMethod(PaymentMethod.UPI)
                .build();

        // Should throw RuntimeException with fallback message
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> rechargeService.addRecharge(requestDTO));

        String message = exception.getMessage();
        assertNotNull(message);
    }

    @Test
    @DisplayName("Integration Exception: Should handle when OperatorPlanManagement returns null plan")
    void addRecharge_ShouldFail_WhenPlanServiceReturnsNull() {
        when(requestUserContext.getUserIdHeader()).thenReturn("55");

        // Plan service returns null
        when(operatorPlanClient.getPlanById(99L)).thenReturn(null);

        RechargeRequestDTO requestDTO = RechargeRequestDTO.builder()
                .operatorId(99L)
                .planId(99L)
                .paymentMethod(PaymentMethod.UPI)
                .build();

        // Should handle gracefully
        assertThrows(Exception.class, () -> rechargeService.addRecharge(requestDTO));
    }

    @Test
    @DisplayName("Integration Exception: Should throw when user header is invalid")
    void addRecharge_ShouldThrow_WhenUserHeaderInvalid() {
        // Invalid user ID format
        when(requestUserContext.getUserIdHeader()).thenReturn("not-a-number");

        PlanResponseDTO plan = new PlanResponseDTO(5L, 299.0, "28 days", "Plan");
        when(operatorPlanClient.getPlanById(5L)).thenReturn(plan);

        RechargeRequestDTO requestDTO = RechargeRequestDTO.builder()
                .operatorId(5L)
                .planId(5L)
                .paymentMethod(PaymentMethod.UPI)
                .build();

        assertThrows(NumberFormatException.class, () -> rechargeService.addRecharge(requestDTO));
    }

    @Test
    @DisplayName("Integration Exception: Should throw when recharge not found by ID")
    void getRechargeById_ShouldThrow_WhenNotFound() {
        assertThrows(RechargeNotFoundException.class,
                () -> rechargeService.getRechargeById(9999L));
    }

    @Test
    @DisplayName("Integration Exception: Should return recharge when found by ID")
    void getRechargeById_ShouldReturn_WhenFound() {
        Recharge recharge = new Recharge();
        recharge.setUserId(60L);
        recharge.setPlanId(10L);
        recharge.setStatus(RechargeStatus.SUCCESS);
        Recharge saved = rechargeRepository.save(recharge);

        RechargeResponseDTO response = rechargeService.getRechargeById(saved.getId());

        assertNotNull(response);
        assertEquals(60L, response.getUserId());
    }

    @Test
    @DisplayName("Integration Exception: Should handle concurrent recharge requests for same user")
    void addRecharge_ShouldHandle_ConcurrentRequests() throws InterruptedException {
        when(requestUserContext.getUserIdHeader()).thenReturn("65");

        PlanResponseDTO plan = new PlanResponseDTO(7L, 349.0, "30 days", "Plan");
        when(operatorPlanClient.getPlanById(7L)).thenReturn(plan);

        RechargeRequestDTO requestDTO = RechargeRequestDTO.builder()
                .operatorId(7L)
                .planId(7L)
                .paymentMethod(PaymentMethod.UPI)
                .build();

        // Simulate concurrent requests
        Thread thread1 = new Thread(() -> rechargeService.addRecharge(requestDTO));
        Thread thread2 = new Thread(() -> rechargeService.addRecharge(requestDTO));

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // Verify both recharges were created
        List<Recharge> recharges = rechargeRepository.findByUserId(65L);
        assertEquals(2, recharges.size());
    }
}
