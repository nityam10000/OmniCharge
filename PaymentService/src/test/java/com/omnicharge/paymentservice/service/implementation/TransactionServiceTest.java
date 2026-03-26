package com.omnicharge.paymentservice.service.implementation;

import com.omnicharge.paymentservice.dto.*;
import com.omnicharge.paymentservice.entity.Transaction;
import com.omnicharge.paymentservice.enums.PaymentMethod;
import com.omnicharge.paymentservice.enums.TransactionStatus;
import com.omnicharge.paymentservice.exception.TransactionNotFoundException;
import com.omnicharge.paymentservice.feignClient.IOperatorPlanClient;
import com.omnicharge.paymentservice.feignClient.IRechargeClient;
import com.omnicharge.paymentservice.feignClient.IUserClient;
import com.omnicharge.paymentservice.mapper.Mapper;
import com.omnicharge.paymentservice.repository.ITransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.omnicharge.paymentservice.dto.NotificationEvent;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    // ── Mocks ──────────────────────────────────────────────────────────────────
    @Mock private ITransactionRepository transactionRepository;
    @Mock private Mapper mapper;
    @Mock private IRechargeClient rechargeClient;        // Feign — no real HTTP call
    @Mock private IOperatorPlanClient operatorPlanClient; // Feign — no real HTTP call
    @Mock private IUserClient userClient;                 // Feign — no real HTTP call
    @Mock private RabbitTemplate rabbitTemplate;          // faked — no real RabbitMQ needed

    // ── System Under Test ──────────────────────────────────────────────────────
    @InjectMocks
    private TransactionService transactionService;

    // ── Shared test data ───────────────────────────────────────────────────────
    private Transaction transaction;
    private TransactionResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
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
                .amount(299.0)
                .transactionStatus(TransactionStatus.PENDING)
                .paymentMethod(PaymentMethod.UPI)
                .rechargeId(5L)
                .timestamp(transaction.getTimestamp())
                .build();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // createTransaction()
    // Deliberately disabled in the service — Razorpay flow must be used instead.
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createTransaction: should throw UnsupportedOperationException")
    void createTransaction_ShouldThrowUnsupportedOperationException() {
        TransactionRequestDTO dto = new TransactionRequestDTO(299.0, 5L, PaymentMethod.UPI);

        assertThrows(UnsupportedOperationException.class,
                () -> transactionService.createTransaction(dto));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getAllTransactionsByUserId()
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getAllTransactionsByUserId: should return all transactions for a user")
    void getAllTransactionsByUserId_ShouldReturnList() {
        Transaction tx2 = new Transaction();
        tx2.setAmount(199.0);
        tx2.setUserId(10L);

        TransactionResponseDTO resp2 = TransactionResponseDTO.builder()
                .amount(199.0).transactionStatus(TransactionStatus.SUCCESS).build();

        when(transactionRepository.findByUserId(10L)).thenReturn(List.of(transaction, tx2));
        when(mapper.toTransactionResponseDTO(transaction)).thenReturn(responseDTO);
        when(mapper.toTransactionResponseDTO(tx2)).thenReturn(resp2);

        List<TransactionResponseDTO> result = transactionService.getAllTransactionsByUserId(10L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(299.0, result.get(0).getAmount());
        assertEquals(199.0, result.get(1).getAmount());
    }

    @Test
    @DisplayName("getAllTransactionsByUserId: should return empty list when user has no transactions")
    void getAllTransactionsByUserId_ShouldReturnEmpty_WhenNone() {
        when(transactionRepository.findByUserId(99L)).thenReturn(List.of());

        List<TransactionResponseDTO> result = transactionService.getAllTransactionsByUserId(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getTransactionByRechargeId()
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getTransactionByRechargeId: should return transaction when rechargeId exists")
    void getTransactionByRechargeId_ShouldReturnTransaction_WhenFound() {
        when(transactionRepository.findByRechargeId(5L)).thenReturn(Optional.of(transaction));
        when(mapper.toTransactionResponseDTO(transaction)).thenReturn(responseDTO);

        TransactionResponseDTO result = transactionService.getTransactionByRechargeId(5L);

        assertNotNull(result);
        assertEquals(299.0, result.getAmount());
        assertEquals(PaymentMethod.UPI, result.getPaymentMethod());
    }

    @Test
    @DisplayName("getTransactionByRechargeId: should throw TransactionNotFoundException when not found")
    void getTransactionByRechargeId_ShouldThrowException_WhenNotFound() {
        when(transactionRepository.findByRechargeId(99L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getTransactionByRechargeId(99L));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getMyTransactions()
    // Reads X-User-Id from the HTTP header — we fake the request context.
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getMyTransactions: should return transactions for the logged-in user")
    void getMyTransactions_ShouldReturnList() {
        ServletRequestAttributes attrs = mockServletAttributes("10");

        try (MockedStatic<RequestContextHolder> ctxHolder =
                     mockStatic(RequestContextHolder.class)) {

            ctxHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(attrs);

            when(transactionRepository.findByUserId(10L)).thenReturn(List.of(transaction));
            when(mapper.toTransactionResponseDTO(transaction)).thenReturn(responseDTO);

            List<TransactionResponseDTO> result = transactionService.getMyTransactions();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(299.0, result.get(0).getAmount());
        }
    }

    @Test
    @DisplayName("getMyTransactions: should return empty list when user has no transactions")
    void getMyTransactions_ShouldReturnEmpty_WhenNone() {
        ServletRequestAttributes attrs = mockServletAttributes("10");

        try (MockedStatic<RequestContextHolder> ctxHolder =
                     mockStatic(RequestContextHolder.class)) {

            ctxHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(attrs);

            when(transactionRepository.findByUserId(10L)).thenReturn(List.of());

            List<TransactionResponseDTO> result = transactionService.getMyTransactions();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // createOrderFallback()
    // Called by Resilience4J when all retries for createOrder() are exhausted.
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createOrderFallback: should throw RuntimeException with retry-exhausted message")
    void createOrderFallback_ShouldThrowRuntimeException() {
        RazorpayOrderRequestDTO dto = new RazorpayOrderRequestDTO(5L, "UPI");
        Exception cause = new RuntimeException("RechargeProcessing is down");

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> transactionService.createOrderFallback(dto, cause));

        assertTrue(thrown.getMessage().contains("Order creation failed after retries"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // verifyPayment() — valid signature → SUCCESS
    //
    // verifySignature() is a private method that runs real HMAC-SHA256 crypto.
    // We compute a correct signature using the same algorithm so the test passes.
    // razorpayKeySecret is a @Value field — injected via reflection since there
    // is no Spring context running in a unit test.
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("verifyPayment: should set SUCCESS and publish success notification when signature is valid")
    void verifyPayment_ShouldSetSuccess_WhenSignatureValid() throws Exception {
        String secret    = "test_secret";
        String orderId   = "order_ABC123";
        String paymentId = "pay_XYZ789";
        String signature = computeHmac(secret, orderId + "|" + paymentId);

        setField(transactionService, "razorpayKeySecret", secret);

        transaction.setStatus(TransactionStatus.PENDING);

        PaymentVerifyRequestDTO dto = new PaymentVerifyRequestDTO(orderId, paymentId, signature);

        when(transactionRepository.findByRazorpayOrderId(orderId))
                .thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(mapper.toTransactionResponseDTO(transaction)).thenReturn(
                TransactionResponseDTO.builder()
                        .amount(299.0)
                        .transactionStatus(TransactionStatus.SUCCESS)
                        .build()
        );
        doReturn(null).when(rechargeClient)
                .updateRechargeStatus(anyString(), anyString(), anyLong(), anyString());

        TransactionResponseDTO result = transactionService.verifyPayment(dto);

        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCESS, transaction.getStatus()); // status updated
        assertEquals(paymentId, transaction.getRazorpayPaymentId());       // paymentId stored
        verify(transactionRepository).save(transaction);                    // saved to DB
        // success notification published to RabbitMQ
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("verifyPayment: should set FAILED and publish failure notification when signature is invalid")
    void verifyPayment_ShouldSetFailed_WhenSignatureInvalid() throws Exception {
        setField(transactionService, "razorpayKeySecret", "test_secret");

        transaction.setStatus(TransactionStatus.PENDING);

        PaymentVerifyRequestDTO dto = new PaymentVerifyRequestDTO(
                "order_ABC123", "pay_XYZ789", "completely_wrong_signature"
        );

        when(transactionRepository.findByRazorpayOrderId("order_ABC123"))
                .thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(mapper.toTransactionResponseDTO(transaction)).thenReturn(
                TransactionResponseDTO.builder()
                        .amount(299.0)
                        .transactionStatus(TransactionStatus.FAILED)
                        .build()
        );
        doReturn(null).when(rechargeClient)
                .updateRechargeStatus(anyString(), anyString(), anyLong(), anyString());

        TransactionResponseDTO result = transactionService.verifyPayment(dto);

        assertNotNull(result);
        assertEquals(TransactionStatus.FAILED, transaction.getStatus());    // marked FAILED
        assertEquals("Signature mismatch", transaction.getFailureReason()); // reason recorded
        verify(transactionRepository).save(transaction);
        // failure notification published to RabbitMQ
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationEvent.class));    }

    @Test
    @DisplayName("verifyPayment: should skip reprocessing and return existing result if already SUCCESS")
    void verifyPayment_ShouldReturnExisting_WhenAlreadyProcessed() throws Exception {
        setField(transactionService, "razorpayKeySecret", "test_secret");

        transaction.setStatus(TransactionStatus.SUCCESS); // already processed

        PaymentVerifyRequestDTO dto = new PaymentVerifyRequestDTO(
                "order_ABC123", "pay_XYZ789", "any_signature"
        );

        when(transactionRepository.findByRazorpayOrderId("order_ABC123"))
                .thenReturn(Optional.of(transaction));
        when(mapper.toTransactionResponseDTO(transaction)).thenReturn(responseDTO);

        TransactionResponseDTO result = transactionService.verifyPayment(dto);

        assertNotNull(result);
        // Already processed — save must never be called again
        verify(transactionRepository, never()).save(any());
        // Do NOT verify rabbitTemplate here — it causes ambiguity with RabbitTemplate's overloads
    }
    
    @Test
    @DisplayName("verifyPayment: should throw TransactionNotFoundException when orderId not found")
    void verifyPayment_ShouldThrowException_WhenOrderNotFound() throws Exception {
        setField(transactionService, "razorpayKeySecret", "test_secret");

        PaymentVerifyRequestDTO dto = new PaymentVerifyRequestDTO(
                "order_NOTEXIST", "pay_XYZ", "sig"
        );

        when(transactionRepository.findByRazorpayOrderId("order_NOTEXIST"))
                .thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.verifyPayment(dto));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // updateRechargeStatusFallback()
    // Called silently when Feign to RechargeProcessing fails — should just log,
    // never throw, so the payment flow is not disrupted.
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updateRechargeStatusFallback: should not throw any exception (logs silently)")
    void updateRechargeStatusFallback_ShouldNotThrow() {
        assertDoesNotThrow(() ->
                transactionService.updateRechargeStatusFallback(
                        5L, "SUCCESS", new RuntimeException("Feign error")
                )
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Fakes the incoming HTTP request so getLoggedInUserId() can read X-User-Id.
     * Chain faked: RequestContextHolder → ServletRequestAttributes → HttpServletRequest
     */
    private ServletRequestAttributes mockServletAttributes(String userId) {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        lenient().when(mockRequest.getHeader("X-User-Id")).thenReturn(userId);

        ServletRequestAttributes mockAttrs = mock(ServletRequestAttributes.class);
        lenient().when(mockAttrs.getRequest()).thenReturn(mockRequest);

        return mockAttrs;
    }

    /**
     * Mocks Spring SecurityContext so getLoggedInUserEmail() and
     * getLoggedInUserRole() work without a running Spring container.
     */
    private void mockSecurityContext(String email, String role) {
        Authentication auth = mock(Authentication.class);
        SecurityContext ctx  = mock(SecurityContext.class);

        lenient().when(auth.getName()).thenReturn(email);
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        lenient().doReturn(authorities).when(auth).getAuthorities();
        lenient().when(ctx.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(ctx);
    }

    /**
     * Sets a private @Value field via reflection — used to inject
     * razorpayKeySecret without starting a Spring application context.
     */
    private void setField(Object target, String fieldName, String value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Computes HMAC-SHA256 exactly the same way TransactionService.verifySignature()
     * does internally — so our valid-signature tests can supply a correct signature.
     */
    private String computeHmac(String secret, String payload) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(
                secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }
}