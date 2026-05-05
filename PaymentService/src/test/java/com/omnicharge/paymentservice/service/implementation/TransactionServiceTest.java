package com.omnicharge.paymentservice.service.implementation;

import com.omnicharge.paymentservice.dto.PaymentVerifyRequestDTO;
import com.omnicharge.paymentservice.dto.PlanResponseDTO;
import com.omnicharge.paymentservice.dto.RazorpayOrderRequestDTO;
import com.omnicharge.paymentservice.dto.RazorpayOrderResponseDTO;
import com.omnicharge.paymentservice.dto.RechargeResponseDTO;
import com.omnicharge.paymentservice.dto.NotificationEvent;
import com.omnicharge.paymentservice.dto.PaymentSagaEvent;
import com.omnicharge.paymentservice.dto.TransactionRequestDTO;
import com.omnicharge.paymentservice.dto.TransactionResponseDTO;
import com.omnicharge.paymentservice.dto.UserResponseDTO;
import com.omnicharge.paymentservice.entity.Transaction;
import com.omnicharge.paymentservice.enums.PaymentMethod;
import com.omnicharge.paymentservice.enums.TransactionStatus;
import com.omnicharge.paymentservice.exception.TransactionNotFoundException;
import com.omnicharge.paymentservice.feignClient.IOperatorPlanClient;
import com.omnicharge.paymentservice.feignClient.IRechargeClient;
import com.omnicharge.paymentservice.feignClient.IUserClient;
import com.omnicharge.paymentservice.mapper.Mapper;
import com.omnicharge.paymentservice.repository.ITransactionRepository;
import com.omnicharge.paymentservice.service.RazorpayRefundService;
import com.omnicharge.paymentservice.support.AuthenticatedUserContext;
import com.omnicharge.paymentservice.support.RazorpayGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private ITransactionRepository transactionRepository;
    @Mock private Mapper mapper;
    @Mock private IRechargeClient rechargeClient;
    @Mock private IOperatorPlanClient operatorPlanClient;
    @Mock private IUserClient userClient;
    @Mock private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    @Mock private RazorpayRefundService razorpayRefundService;
    @Mock private AuthenticatedUserContext authenticatedUserContext;
    @Mock private RazorpayGateway razorpayGateway;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;
    private TransactionResponseDTO responseDTO;

    @BeforeEach
    void setUp() throws Exception {
        transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setAmount(299.0);
        transaction.setUserId(10L);
        transaction.setRechargeId(5L);
        transaction.setUserEmail("rahul@example.com");
        transaction.setUserContactNo("9876543210");
        transaction.setPaymentMethod(PaymentMethod.UPI);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setRazorpayOrderId("order_ABC123");
        transaction.setTimestamp(LocalDateTime.now());

        responseDTO = TransactionResponseDTO.builder()
                .id(1L)
                .rechargeId(5L)
                .userId(10L)
                .amount(299.0)
                .paymentMethod(PaymentMethod.UPI)
                .status(TransactionStatus.PENDING)
                .razorpayOrderId("order_ABC123")
                .razorpayPaymentId(null)
                .createdAt(transaction.getTimestamp())
                .build();

        setField("razorpayKeyId", "test_key");
        setField("razorpayKeySecret", "test_secret");
    }

    @Test
    void createTransaction_ShouldThrowUnsupportedOperationException() {
        TransactionRequestDTO dto = new TransactionRequestDTO(299.0, 5L, PaymentMethod.UPI);

        assertThrows(UnsupportedOperationException.class,
                () -> transactionService.createTransaction(dto));
    }

    @Test
    void getAllTransactionsByUserId_ShouldReturnList() {
        when(transactionRepository.findByUserId(10L)).thenReturn(List.of(transaction));
        when(mapper.toTransactionResponseDTO(transaction)).thenReturn(responseDTO);

        List<TransactionResponseDTO> result = transactionService.getAllTransactionsByUserId(10L);

        assertEquals(1, result.size());
        verify(transactionRepository).findByUserId(10L);
    }

    @Test
    void getTransactionByRechargeId_ShouldReturnTransaction() {
        mockAuthenticatedUser();
        when(transactionRepository.findByRechargeId(5L)).thenReturn(Optional.of(transaction));
        when(mapper.toTransactionResponseDTO(transaction)).thenReturn(responseDTO);

        TransactionResponseDTO result = transactionService.getTransactionByRechargeId(5L);

        assertNotNull(result);
        verify(transactionRepository).findByRechargeId(5L);
    }

    @Test
    void getTransactionByRechargeId_ShouldThrow_WhenNotFound() {
        mockAuthenticatedUser();
        when(transactionRepository.findByRechargeId(99L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getTransactionByRechargeId(99L));
    }

    @Test
    void getMyTransactions_ShouldUseAuthenticatedUserId() {
        mockAuthenticatedUserId();
        Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction));
        when(transactionRepository.findByUserId(10L, PageRequest.of(0, 10))).thenReturn(transactionPage);
        when(mapper.toTransactionResponseDTO(transaction)).thenReturn(responseDTO);

        Page<TransactionResponseDTO> result = transactionService.getMyTransactions(PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        verify(authenticatedUserContext).getUserIdHeader();
        verify(transactionRepository).findByUserId(10L, PageRequest.of(0, 10));
    }

    @Test
    void createOrder_ShouldCreatePendingTransaction() throws Exception {
        mockAuthenticatedUser();
        RazorpayOrderRequestDTO dto = new RazorpayOrderRequestDTO(5L, "upi");
        RechargeResponseDTO recharge = new RechargeResponseDTO(5L, "PENDING", 299.0, 7L, "PENDING", 10L);
        PlanResponseDTO plan = new PlanResponseDTO(1L, 299.0, "28 days", "Unlimited");
        UserResponseDTO user = UserResponseDTO.builder().contactNo("9876543210").build();

        when(rechargeClient.getRechargeById("ROLE_USER", "rahul@example.com", 5L)).thenReturn(recharge);
        when(operatorPlanClient.getPlanById("ROLE_USER", "rahul@example.com", 7L)).thenReturn(plan);
        when(userClient.getUserByEmail("ROLE_USER", "rahul@example.com", "rahul@example.com")).thenReturn(user);
        when(razorpayGateway.createOrderId(299.0, "recharge_5", "test_key", "test_secret"))
                .thenReturn("order_999");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RazorpayOrderResponseDTO result = transactionService.createOrder(dto);

        assertEquals("order_999", result.getRazorpayOrderId());
        assertEquals(299.0, result.getAmount());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction saved = captor.getValue();
        assertEquals(TransactionStatus.PENDING, saved.getStatus());
        assertEquals(PaymentMethod.UPI, saved.getPaymentMethod());
        assertEquals("9876543210", saved.getUserContactNo());
        assertEquals("order_999", saved.getRazorpayOrderId());
    }

    @Test
    void createOrder_ShouldThrow_WhenRechargeNotFound() {
        mockAuthenticatedUser();
        RazorpayOrderRequestDTO dto = new RazorpayOrderRequestDTO(5L, "upi");
        when(rechargeClient.getRechargeById("ROLE_USER", "rahul@example.com", 5L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transactionService.createOrder(dto));

        assertEquals("Razorpay order creation failed: Recharge not found for id: 5", ex.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createOrder_ShouldThrow_WhenRechargeOwnedByAnotherUser() {
        mockAuthenticatedUser();
        RazorpayOrderRequestDTO dto = new RazorpayOrderRequestDTO(5L, "upi");
        RechargeResponseDTO recharge = new RechargeResponseDTO(5L, "PENDING", 299.0, 7L, "PENDING", 22L);
        when(rechargeClient.getRechargeById("ROLE_USER", "rahul@example.com", 5L)).thenReturn(recharge);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transactionService.createOrder(dto));

        assertEquals("Razorpay order creation failed: Access denied: recharge does not belong to the current user.",
                ex.getMessage());
    }

    @Test
    void createOrder_ShouldThrow_WhenPlanAmountMissing() {
        mockAuthenticatedUser();
        RazorpayOrderRequestDTO dto = new RazorpayOrderRequestDTO(5L, "upi");
        RechargeResponseDTO recharge = new RechargeResponseDTO(5L, "PENDING", 299.0, 7L, "PENDING", 10L);

        when(rechargeClient.getRechargeById("ROLE_USER", "rahul@example.com", 5L)).thenReturn(recharge);
        when(operatorPlanClient.getPlanById("ROLE_USER", "rahul@example.com", 7L))
                .thenReturn(new PlanResponseDTO(1L, null, "28 days", "Unlimited"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transactionService.createOrder(dto));

        assertEquals("Razorpay order creation failed: Plan not found or has no amount for planId: 7",
                ex.getMessage());
    }

    @Test
    void createOrder_ShouldContinue_WhenContactLookupFails() throws Exception {
        mockAuthenticatedUser();
        RazorpayOrderRequestDTO dto = new RazorpayOrderRequestDTO(5L, "card");
        RechargeResponseDTO recharge = new RechargeResponseDTO(5L, "PENDING", 299.0, 7L, "PENDING", 10L);
        PlanResponseDTO plan = new PlanResponseDTO(1L, 299.0, "28 days", "Unlimited");

        when(rechargeClient.getRechargeById("ROLE_USER", "rahul@example.com", 5L)).thenReturn(recharge);
        when(operatorPlanClient.getPlanById("ROLE_USER", "rahul@example.com", 7L)).thenReturn(plan);
        when(userClient.getUserByEmail("ROLE_USER", "rahul@example.com", "rahul@example.com"))
                .thenThrow(new RuntimeException("user service down"));
        when(razorpayGateway.createOrderId(299.0, "recharge_5", "test_key", "test_secret"))
                .thenReturn("order_contact_fail");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RazorpayOrderResponseDTO result = transactionService.createOrder(dto);

        assertEquals("order_contact_fail", result.getRazorpayOrderId());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertNull(captor.getValue().getUserContactNo());
        assertEquals(PaymentMethod.CARD, captor.getValue().getPaymentMethod());
    }

    @Test
    void createOrderFallback_ShouldSaveAuditTransaction_AndThrow() {
        mockAuthenticatedUserIdAndEmail();
        RazorpayOrderRequestDTO dto = new RazorpayOrderRequestDTO(5L, "upi");
        Transaction failed = new Transaction();
        failed.setTransactionId(UUID.randomUUID());
        failed.setRechargeId(5L);
        failed.setUserId(10L);
        failed.setUserEmail("rahul@example.com");
        failed.setStatus(TransactionStatus.FAILED);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(failed);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transactionService.createOrderFallback(dto, new RuntimeException("down")));

        assertEquals(
                "Order creation failed - RechargeProcessing is unavailable. Your recharge has been cancelled. Please try again later.",
                ex.getMessage()
        );
        verify(transactionRepository).save(any(Transaction.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(PaymentSagaEvent.class));
    }

    @Test
    void createOrderFallback_ShouldStillThrow_WhenAuditSaveFails() {
        mockAuthenticatedUserIdAndEmail();
        RazorpayOrderRequestDTO dto = new RazorpayOrderRequestDTO(5L, "upi");
        when(transactionRepository.save(any(Transaction.class))).thenThrow(new RuntimeException("db down"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transactionService.createOrderFallback(dto, new RuntimeException("down")));

        assertEquals(
                "Order creation failed - RechargeProcessing is unavailable. Your recharge has been cancelled. Please try again later.",
                ex.getMessage()
        );
    }

    @Test
    void verifyPayment_ShouldSetSuccess_WhenSignatureValid() throws Exception {
        String orderId = "order_ABC123";
        String paymentId = "pay_XYZ789";
        String signature = computeHmac("test_secret", orderId + "|" + paymentId);

        PaymentVerifyRequestDTO dto = new PaymentVerifyRequestDTO(orderId, paymentId, signature);

        when(transactionRepository.findByRazorpayOrderId(orderId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toTransactionResponseDTO(any(Transaction.class))).thenReturn(responseDTO);

        TransactionResponseDTO result = transactionService.verifyPayment(dto);

        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCESS, transaction.getStatus());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(PaymentSagaEvent.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationEvent.class));
        verify(razorpayRefundService, never()).refundAndNotify(any(), anyString());
    }

    @Test
    void verifyPayment_ShouldSetFailed_AndRefund_WhenSignatureInvalid() {
        PaymentVerifyRequestDTO dto =
                new PaymentVerifyRequestDTO("order_ABC123", "pay_XYZ789", "wrong_sig");

        when(transactionRepository.findByRazorpayOrderId("order_ABC123")).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toTransactionResponseDTO(any(Transaction.class))).thenReturn(responseDTO);

        transactionService.verifyPayment(dto);

        assertEquals(TransactionStatus.FAILED, transaction.getStatus());
        verify(razorpayRefundService).refundAndNotify(
                any(Transaction.class),
                org.mockito.ArgumentMatchers.contains("Signature mismatch")
        );
    }

    @Test
    void verifyPayment_ShouldSetFailed_WithoutRefund_WhenPaymentIdMissing() {
        PaymentVerifyRequestDTO dto =
                new PaymentVerifyRequestDTO("order_ABC123", "", "wrong_sig");

        when(transactionRepository.findByRazorpayOrderId("order_ABC123")).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toTransactionResponseDTO(any(Transaction.class))).thenReturn(responseDTO);

        transactionService.verifyPayment(dto);

        assertEquals(TransactionStatus.FAILED, transaction.getStatus());
        verify(razorpayRefundService, never()).refundAndNotify(any(), anyString());
    }

    @Test
    void verifyPayment_ShouldSkip_WhenAlreadyProcessed() {
        transaction.setStatus(TransactionStatus.SUCCESS);
        when(transactionRepository.findByRazorpayOrderId("order_ABC123")).thenReturn(Optional.of(transaction));
        when(mapper.toTransactionResponseDTO(transaction)).thenReturn(responseDTO);

        TransactionResponseDTO result = transactionService.verifyPayment(
                new PaymentVerifyRequestDTO("order_ABC123", "pay", "sig"));

        assertNotNull(result);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void verifyPayment_ShouldThrow_WhenOrderNotFound() {
        when(transactionRepository.findByRazorpayOrderId("order_X")).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.verifyPayment(new PaymentVerifyRequestDTO("order_X", "pay", "sig")));
    }

    @Test
    void getMyTransactions_ShouldThrow_WhenUserHeaderMissing() {
        when(authenticatedUserContext.getUserIdHeader()).thenReturn("");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transactionService.getMyTransactions(PageRequest.of(0, 10)));

        assertEquals("X-User-Id header is missing", ex.getMessage());
    }

    @Test
    void verifyPayment_ShouldIgnoreNotificationPublishFailure() throws Exception {
        String orderId = "order_ABC123";
        String paymentId = "pay_XYZ789";
        String signature = computeHmac("test_secret", orderId + "|" + paymentId);

        when(transactionRepository.findByRazorpayOrderId(orderId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toTransactionResponseDTO(any(Transaction.class))).thenReturn(responseDTO);
        doThrow(new RuntimeException("amqp down"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationEvent.class));

        TransactionResponseDTO result = transactionService.verifyPayment(
                new PaymentVerifyRequestDTO(orderId, paymentId, signature));

        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCESS, transaction.getStatus());
    }

    private void mockAuthenticatedUser() {
        lenient().when(authenticatedUserContext.getUserIdHeader()).thenReturn("10");
        lenient().when(authenticatedUserContext.getEmail()).thenReturn("rahul@example.com");
        lenient().when(authenticatedUserContext.getRole()).thenReturn("ROLE_USER");
    }

    private void mockAuthenticatedUserId() {
        when(authenticatedUserContext.getUserIdHeader()).thenReturn("10");
    }

    private void mockAuthenticatedUserIdAndEmail() {
        when(authenticatedUserContext.getUserIdHeader()).thenReturn("10");
        when(authenticatedUserContext.getEmail()).thenReturn("rahul@example.com");
    }

    private void setField(String fieldName, String value) throws Exception {
        Field field = transactionService.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(transactionService, value);
    }

    private String computeHmac(String secret, String payload) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes(), "HmacSHA256"));
        byte[] hash = mac.doFinal(payload.getBytes());
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) {
                hex.append('0');
            }
            hex.append(s);
        }
        return hex.toString();
    }
}
