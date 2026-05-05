package com.omnicharge.paymentservice.integration;

import com.omnicharge.paymentservice.dto.PlanResponseDTO;
import com.omnicharge.paymentservice.dto.RechargeResponseDTO;
import com.omnicharge.paymentservice.dto.RazorpayOrderRequestDTO;
import com.omnicharge.paymentservice.dto.UserResponseDTO;
import com.omnicharge.paymentservice.feignClient.IOperatorPlanClient;
import com.omnicharge.paymentservice.feignClient.IRechargeClient;
import com.omnicharge.paymentservice.feignClient.IUserClient;
import com.omnicharge.paymentservice.repository.ITransactionRepository;
import com.omnicharge.paymentservice.service.implementation.TransactionService;
import com.omnicharge.paymentservice.support.AuthenticatedUserContext;
import com.omnicharge.paymentservice.support.RazorpayGateway;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ITransactionRepository transactionRepository;

    @MockBean
    private IRechargeClient rechargeClient;

    @MockBean
    private IOperatorPlanClient operatorPlanClient;

    @MockBean
    private IUserClient userClient;

    @MockBean
    private RazorpayGateway razorpayGateway;

    @MockBean
    private AuthenticatedUserContext authenticatedUserContext;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration: Should fetch user, plan, and recharge from all services and create order")
    void createOrder_ShouldIntegrate_WithAllServices() throws Exception {
        when(authenticatedUserContext.getEmail()).thenReturn("rahul@example.com");
        when(authenticatedUserContext.getRole()).thenReturn("ROLE_USER");
        when(authenticatedUserContext.getUserIdHeader()).thenReturn("10");

        RechargeResponseDTO recharge = new RechargeResponseDTO(
                5L, "PENDING", 299.0, 7L, "PENDING", 10L
        );

        PlanResponseDTO plan = new PlanResponseDTO(7L, 299.0, "28 days", "Unlimited + 2GB/day");

        UserResponseDTO user = UserResponseDTO.builder()
                .userId(10L)
                .name("Rahul Sharma")
                .email("rahul@example.com")
                .contactNo("9876543210")
                .build();

        when(rechargeClient.getRechargeById("ROLE_USER", "rahul@example.com", 5L))
                .thenReturn(recharge);
        when(operatorPlanClient.getPlanById("ROLE_USER", "rahul@example.com", 7L))
                .thenReturn(plan);
        when(userClient.getUserByEmail("ROLE_USER", "rahul@example.com", "rahul@example.com"))
                .thenReturn(user);
        when(razorpayGateway.createOrderId(299.0, "recharge_5", "test_key", "test_secret"))
                .thenReturn("order_XYZ123");

        RazorpayOrderRequestDTO orderRequest = new RazorpayOrderRequestDTO(5L, "upi");
        var response = transactionService.createOrder(orderRequest);

        assertNotNull(response);
        assertEquals(299.0, response.getAmount());
    }

    @Test
    @DisplayName("Integration: Should handle recharge with zero amount from plan service")
    void createOrder_ShouldHandle_ZeroAmountPlan() throws Exception {
        when(authenticatedUserContext.getEmail()).thenReturn("user@example.com");
        when(authenticatedUserContext.getRole()).thenReturn("ROLE_USER");
        when(authenticatedUserContext.getUserIdHeader()).thenReturn("20");

        RechargeResponseDTO recharge = new RechargeResponseDTO(
                10L, null, 0.0, 50L, null, 20L
        );

        PlanResponseDTO plan = new PlanResponseDTO(50L, 0.0, "free", "Free Trial");

        when(rechargeClient.getRechargeById(anyString(), anyString(), anyLong()))
                .thenReturn(recharge);
        when(operatorPlanClient.getPlanById(anyString(), anyString(), anyLong()))
                .thenReturn(plan);

        RazorpayOrderRequestDTO orderRequest = new RazorpayOrderRequestDTO(10L, "upi");
        var response = transactionService.createOrder(orderRequest);

        assertNotNull(response);
    }

    @Test
    @DisplayName("Integration: Should handle pagination when retrieving user transactions")
    void getMyTransactions_ShouldIntegrate_WithPagination() throws Exception {
        when(authenticatedUserContext.getEmail()).thenReturn("puja@example.com");
        when(authenticatedUserContext.getRole()).thenReturn("ROLE_USER");
        when(authenticatedUserContext.getUserIdHeader()).thenReturn("15");

        UserResponseDTO user = UserResponseDTO.builder()
                .userId(15L)
                .name("Puja Verma")
                .email("puja@example.com")
                .contactNo("7777777777")
                .build();

        when(userClient.getUserByEmail(anyString(), anyString(), anyString()))
                .thenReturn(user);

        Pageable pageable = PageRequest.of(0, 10);
        var transactions = transactionService.getMyTransactions(pageable);
        assertNotNull(transactions);
    }

    @Test
    @DisplayName("Integration Exception: Should fail gracefully when UserManagement service is down")
    void createOrder_ShouldFail_WhenUserServiceDown() throws Exception {
        when(authenticatedUserContext.getEmail()).thenReturn("absent@example.com");
        when(authenticatedUserContext.getRole()).thenReturn("ROLE_USER");
        when(authenticatedUserContext.getUserIdHeader()).thenReturn("30");

        RechargeResponseDTO recharge = new RechargeResponseDTO(
                8L, null, null, 40L, null, 30L
        );

        when(rechargeClient.getRechargeById(anyString(), anyString(), anyLong()))
                .thenReturn(recharge);
        when(userClient.getUserByEmail(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service Down"));

        RazorpayOrderRequestDTO orderRequest = new RazorpayOrderRequestDTO(8L, "upi");
        assertThrows(Exception.class, () -> transactionService.createOrder(orderRequest));
    }

    @Test
    @DisplayName("Integration Exception: Should throw when recharge does not belong to authenticated user")
    void createOrder_ShouldFail_WhenRechargeNotOwnedByUser() throws Exception {
        when(authenticatedUserContext.getEmail()).thenReturn("user1@example.com");
        when(authenticatedUserContext.getRole()).thenReturn("ROLE_USER");
        when(authenticatedUserContext.getUserIdHeader()).thenReturn("100");

        RechargeResponseDTO recharge = new RechargeResponseDTO(
                12L, null, 599.0, 60L, null, 200L
        );

        when(rechargeClient.getRechargeById(anyString(), anyString(), anyLong()))
                .thenReturn(recharge);

        RazorpayOrderRequestDTO orderRequest = new RazorpayOrderRequestDTO(12L, "upi");
        assertThrows(Exception.class, () -> transactionService.createOrder(orderRequest));
    }

    @Test
    @DisplayName("Integration Exception: Should handle operator plan service returning null")
    void createOrder_ShouldFail_WhenPlanServiceReturnsNull() throws Exception {
        when(authenticatedUserContext.getEmail()).thenReturn("test@example.com");
        when(authenticatedUserContext.getRole()).thenReturn("ROLE_USER");
        when(authenticatedUserContext.getUserIdHeader()).thenReturn("50");

        RechargeResponseDTO recharge = new RechargeResponseDTO(
                15L, null, null, 70L, null, 50L
        );

        when(rechargeClient.getRechargeById(anyString(), anyString(), anyLong()))
                .thenReturn(recharge);
        when(operatorPlanClient.getPlanById(anyString(), anyString(), anyLong()))
                .thenReturn(null);

        RazorpayOrderRequestDTO orderRequest = new RazorpayOrderRequestDTO(15L, "upi");
        assertThrows(Exception.class, () -> transactionService.createOrder(orderRequest));
    }
}
