package com.omnicharge.paymentservice.service.implementation;

import com.omnicharge.paymentservice.dto.PlanResponseDTO;
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
import com.omnicharge.paymentservice.support.AuthenticatedUserContext;
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
    @Mock private AuthenticatedUserContext authenticatedUserContext;

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
        transaction.setTimestamp(LocalDateTime.now());

        responseDTO = TransactionResponseDTO.builder()
                .id(1L)
                .rechargeId(5L)
                .userId(10L)
                .amount(299.0)
                .paymentMethod(PaymentMethod.UPI)
                .status(TransactionStatus.PENDING)
                .createdAt(transaction.getTimestamp())
                .build();

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
        mockAuthenticatedUser();
        RechargeResponseDTO recharge = new RechargeResponseDTO(5L, "PENDING", 299.0, 7L, "PENDING", 10L);
        PlanResponseDTO plan = new PlanResponseDTO(1L, 299.0, "28 days", "Unlimited");
        UserResponseDTO user = UserResponseDTO.builder().contactNo("9876543210").build();

        when(rechargeClient.getRechargeById("ROLE_USER", "rahul@example.com", 5L)).thenReturn(recharge);
        when(operatorPlanClient.getPlanById("ROLE_USER", "rahul@example.com", 7L)).thenReturn(plan);
        when(userClient.getUserByEmail("ROLE_USER", "rahul@example.com", "rahul@example.com")).thenReturn(user);
                .thenReturn("order_999");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));


        assertEquals(299.0, result.getAmount());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction saved = captor.getValue();
        assertEquals(TransactionStatus.PENDING, saved.getStatus());
        assertEquals(PaymentMethod.UPI, saved.getPaymentMethod());
        assertEquals("9876543210", saved.getUserContactNo());
    }

    @Test
        mockAuthenticatedUser();
        when(rechargeClient.getRechargeById("ROLE_USER", "rahul@example.com", 5L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,

        verify(transactionRepository, never()).save(any());
    }

    @Test
        mockAuthenticatedUser();
        RechargeResponseDTO recharge = new RechargeResponseDTO(5L, "PENDING", 299.0, 7L, "PENDING", 22L);
        when(rechargeClient.getRechargeById("ROLE_USER", "rahul@example.com", 5L)).thenReturn(recharge);

        RuntimeException ex = assertThrows(RuntimeException.class,

                ex.getMessage());
    }

    @Test
        mockAuthenticatedUser();
        RechargeResponseDTO recharge = new RechargeResponseDTO(5L, "PENDING", 299.0, 7L, "PENDING", 10L);

        when(rechargeClient.getRechargeById("ROLE_USER", "rahul@example.com", 5L)).thenReturn(recharge);
        when(operatorPlanClient.getPlanById("ROLE_USER", "rahul@example.com", 7L))
                .thenReturn(new PlanResponseDTO(1L, null, "28 days", "Unlimited"));

        RuntimeException ex = assertThrows(RuntimeException.class,

                ex.getMessage());
    }

    @Test
        mockAuthenticatedUser();
        RechargeResponseDTO recharge = new RechargeResponseDTO(5L, "PENDING", 299.0, 7L, "PENDING", 10L);
        PlanResponseDTO plan = new PlanResponseDTO(1L, 299.0, "28 days", "Unlimited");

        when(rechargeClient.getRechargeById("ROLE_USER", "rahul@example.com", 5L)).thenReturn(recharge);
        when(operatorPlanClient.getPlanById("ROLE_USER", "rahul@example.com", 7L)).thenReturn(plan);
        when(userClient.getUserByEmail("ROLE_USER", "rahul@example.com", "rahul@example.com"))
                .thenThrow(new RuntimeException("user service down"));
                .thenReturn("order_contact_fail");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));


        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertNull(captor.getValue().getUserContactNo());
        assertEquals(PaymentMethod.CARD, captor.getValue().getPaymentMethod());
    }

    @Test
        mockAuthenticatedUserIdAndEmail();
        Transaction failed = new Transaction();
        failed.setTransactionId(UUID.randomUUID());
        failed.setRechargeId(5L);
        failed.setUserId(10L);
        failed.setUserEmail("rahul@example.com");
        failed.setStatus(TransactionStatus.FAILED);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(failed);

        RuntimeException ex = assertThrows(RuntimeException.class,

        assertEquals(
                "Order creation failed - RechargeProcessing is unavailable. Your recharge has been cancelled. Please try again later.",
                ex.getMessage()
        );
        verify(transactionRepository).save(any(Transaction.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(PaymentSagaEvent.class));
    }

    @Test
        mockAuthenticatedUserIdAndEmail();
        when(transactionRepository.save(any(Transaction.class))).thenThrow(new RuntimeException("db down"));

        RuntimeException ex = assertThrows(RuntimeException.class,

        assertEquals(
                "Order creation failed - RechargeProcessing is unavailable. Your recharge has been cancelled. Please try again later.",
                ex.getMessage()
        );
    }

    @Test
        String orderId = "order_ABC123";
        String paymentId = "pay_XYZ789";
        String signature = computeHmac("test_secret", orderId + "|" + paymentId);


        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toTransactionResponseDTO(any(Transaction.class))).thenReturn(responseDTO);


        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCESS, transaction.getStatus());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(PaymentSagaEvent.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationEvent.class));
    }

    @Test

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toTransactionResponseDTO(any(Transaction.class))).thenReturn(responseDTO);


        assertEquals(TransactionStatus.FAILED, transaction.getStatus());
                any(Transaction.class),
                org.mockito.ArgumentMatchers.contains("Signature mismatch")
        );
    }

    @Test

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toTransactionResponseDTO(any(Transaction.class))).thenReturn(responseDTO);


        assertEquals(TransactionStatus.FAILED, transaction.getStatus());
    }

    @Test
        transaction.setStatus(TransactionStatus.SUCCESS);
        when(mapper.toTransactionResponseDTO(transaction)).thenReturn(responseDTO);


        assertNotNull(result);
        verify(transactionRepository, never()).save(any());
    }

    @Test

        assertThrows(TransactionNotFoundException.class,
    }

    @Test
    void getMyTransactions_ShouldThrow_WhenUserHeaderMissing() {
        when(authenticatedUserContext.getUserIdHeader()).thenReturn("");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transactionService.getMyTransactions(PageRequest.of(0, 10)));

        assertEquals("X-User-Id header is missing", ex.getMessage());
    }

    @Test
        String orderId = "order_ABC123";
        String paymentId = "pay_XYZ789";
        String signature = computeHmac("test_secret", orderId + "|" + paymentId);

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toTransactionResponseDTO(any(Transaction.class))).thenReturn(responseDTO);
        doThrow(new RuntimeException("amqp down"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationEvent.class));


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
