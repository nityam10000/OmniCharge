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
import com.omnicharge.paymentservice.service.RazorpayRefundService;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    // ================= MOCKS =================
    @Mock private ITransactionRepository transactionRepository;
    @Mock private Mapper mapper;
    @Mock private IRechargeClient rechargeClient;
    @Mock private IOperatorPlanClient operatorPlanClient;
    @Mock private IUserClient userClient;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private RazorpayRefundService razorpayRefundService;

    @InjectMocks
    private TransactionService transactionService;

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

    // ================= createTransaction =================
    @Test
    void createTransaction_ShouldThrowUnsupportedOperationException() {
        TransactionRequestDTO dto = new TransactionRequestDTO(299.0, 5L, PaymentMethod.UPI);
        assertThrows(UnsupportedOperationException.class,
                () -> transactionService.createTransaction(dto));
    }

    // ================= getAllTransactions =================
    @Test
    void getAllTransactionsByUserId_ShouldReturnList() {
        when(transactionRepository.findByUserId(10L)).thenReturn(List.of(transaction));
        when(mapper.toTransactionResponseDTO(transaction)).thenReturn(responseDTO);

        List<TransactionResponseDTO> result =
                transactionService.getAllTransactionsByUserId(10L);

        assertEquals(1, result.size());
    }

    // ================= getTransactionByRechargeId =================
    @Test
    void getTransactionByRechargeId_ShouldReturnTransaction() {
        when(transactionRepository.findByRechargeId(5L))
                .thenReturn(Optional.of(transaction));
        when(mapper.toTransactionResponseDTO(transaction)).thenReturn(responseDTO);

        TransactionResponseDTO result =
                transactionService.getTransactionByRechargeId(5L);

        assertNotNull(result);
    }

    @Test
    void getTransactionByRechargeId_ShouldThrow_WhenNotFound() {
        when(transactionRepository.findByRechargeId(99L))
                .thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getTransactionByRechargeId(99L));
    }

    // ================= verifyPayment SUCCESS =================
    @Test
    void verifyPayment_ShouldSetSuccess_WhenSignatureValid() throws Exception {

        setField("razorpayKeySecret", "test_secret");

        String orderId = "order_ABC123";
        String paymentId = "pay_XYZ789";
        String signature = computeHmac("test_secret", orderId + "|" + paymentId);

        PaymentVerifyRequestDTO dto =
                new PaymentVerifyRequestDTO(orderId, paymentId, signature);

        when(transactionRepository.findByRazorpayOrderId(orderId))
                .thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any())).thenReturn(transaction);
        when(mapper.toTransactionResponseDTO(any())).thenReturn(responseDTO);

        TransactionResponseDTO result = transactionService.verifyPayment(dto);

        assertEquals(TransactionStatus.SUCCESS, transaction.getStatus());

        verify(rabbitTemplate, atLeastOnce())
                .convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));

        verify(razorpayRefundService, never())
                .refundAndNotify(any(), any());
    }

    // ================= verifyPayment FAILED =================
    @Test
    void verifyPayment_ShouldSetFailed_WhenSignatureInvalid() throws Exception {

        setField("razorpayKeySecret", "test_secret");

        transaction.setRazorpayPaymentId("pay_XYZ789");

        PaymentVerifyRequestDTO dto =
                new PaymentVerifyRequestDTO("order_ABC123", "pay_XYZ789", "wrong_sig");

        when(transactionRepository.findByRazorpayOrderId("order_ABC123"))
                .thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any())).thenReturn(transaction);
        when(mapper.toTransactionResponseDTO(any())).thenReturn(responseDTO);

        TransactionResponseDTO result = transactionService.verifyPayment(dto);

        assertEquals(TransactionStatus.FAILED, transaction.getStatus());

        verify(rabbitTemplate, atLeastOnce())
                .convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));

        verify(razorpayRefundService).refundAndNotify(
                any(Transaction.class),
                contains("Signature mismatch")
        );
    }

    // ================= already processed =================
    @Test
    void verifyPayment_ShouldSkip_WhenAlreadyProcessed() throws Exception {

        setField("razorpayKeySecret", "test_secret");

        transaction.setStatus(TransactionStatus.SUCCESS);

        PaymentVerifyRequestDTO dto =
                new PaymentVerifyRequestDTO("order_ABC123", "pay_XYZ789", "any");

        when(transactionRepository.findByRazorpayOrderId("order_ABC123"))
                .thenReturn(Optional.of(transaction));
        when(mapper.toTransactionResponseDTO(transaction)).thenReturn(responseDTO);

        TransactionResponseDTO result = transactionService.verifyPayment(dto);

        verify(transactionRepository, never()).save(any());
    }

    // ================= order not found =================
    @Test
    void verifyPayment_ShouldThrow_WhenOrderNotFound() throws Exception {

        setField("razorpayKeySecret", "test_secret");

        when(transactionRepository.findByRazorpayOrderId("order_X"))
                .thenReturn(Optional.empty());

        PaymentVerifyRequestDTO dto =
                new PaymentVerifyRequestDTO("order_X", "pay", "sig");

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.verifyPayment(dto));
    }

    // ================= helpers =================
    private void setField(String fieldName, String value) throws Exception {
        var field = transactionService.getClass().getDeclaredField(fieldName);
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
            if (s.length() == 1) hex.append('0');
            hex.append(s);
        }
        return hex.toString();
    }
}