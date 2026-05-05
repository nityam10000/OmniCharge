package com.omnicharge.paymentservice.service;

import com.omnicharge.paymentservice.dto.NotificationEvent;
import com.omnicharge.paymentservice.entity.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RazorpayRefundServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RazorpayRefundService refundService;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        refundService = new RazorpayRefundService(rabbitTemplate);
        ReflectionTestUtils.setField(refundService, "razorpayKeyId", "key");
        ReflectionTestUtils.setField(refundService, "razorpayKeySecret", "secret");

        transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setAmount(299.0);
        transaction.setUserEmail("rahul@example.com");
        transaction.setUserContactNo("9876543210");
        transaction.setRazorpayPaymentId("");
    }

    @Test
    void initiateRazorpayRefund_ShouldReturnNullForBlankPaymentId() {
        String refundId = refundService.initiateRazorpayRefund("", 299.0, "reason");

        assertNull(refundId);
    }

    @Test
    void refundAndNotify_ShouldPublishNotificationEvenWithoutRefundId() {
        refundService.refundAndNotify(transaction, "reason");

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationEvent.class));
    }

    @Test
    void refundAndNotify_ShouldSwallowNotificationFailure() {
        doThrow(new RuntimeException("amqp down"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationEvent.class));

        assertDoesNotThrow(() -> refundService.refundAndNotify(transaction, "reason"));
    }
}
