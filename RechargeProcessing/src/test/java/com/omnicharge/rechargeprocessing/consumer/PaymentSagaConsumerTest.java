package com.omnicharge.rechargeprocessing.consumer;

import com.omnicharge.rechargeprocessing.dto.PaymentSagaEvent;
import com.omnicharge.rechargeprocessing.entity.Recharge;
import com.omnicharge.rechargeprocessing.enums.RechargeStatus;
import com.omnicharge.rechargeprocessing.exception.RechargeNotFoundException;
import com.omnicharge.rechargeprocessing.repository.IRechargeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentSagaConsumerTest {

    @Mock
    private IRechargeRepository rechargeRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private PaymentSagaConsumer consumer;
    private PaymentSagaEvent event;
    private Recharge recharge;

    @BeforeEach
    void setUp() {
        consumer = new PaymentSagaConsumer(rechargeRepository, rabbitTemplate);
        event = PaymentSagaEvent.builder()
                .sagaId("saga-1")
                .transactionId(UUID.randomUUID())
                .rechargeId(5L)
                .userId(10L)
                .userEmail("rahul@example.com")
                .eventType("payment.completed")
                .build();
        recharge = new Recharge();
        recharge.setId(5L);
        recharge.setStatus(RechargeStatus.PENDING);
    }

    @Test
    void onPaymentSagaEvent_ShouldUpdateRechargeToSuccess() {
        when(rechargeRepository.findById(5L)).thenReturn(Optional.of(recharge));

        consumer.onPaymentSagaEvent(event);

        assertEquals(RechargeStatus.SUCCESS, recharge.getStatus());
        verify(rechargeRepository).save(recharge);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(PaymentSagaEvent.class));
    }

    @Test
    void onPaymentSagaEvent_ShouldSkipDuplicateStatusAndStillReply() {
        recharge.setStatus(RechargeStatus.SUCCESS);
        when(rechargeRepository.findById(5L)).thenReturn(Optional.of(recharge));

        consumer.onPaymentSagaEvent(event);

        verify(rechargeRepository, never()).save(any());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(PaymentSagaEvent.class));
    }

    @Test
    void onPaymentSagaEvent_ShouldUpdateRechargeToFailed() {
        event.setEventType("payment.failed");
        when(rechargeRepository.findById(5L)).thenReturn(Optional.of(recharge));

        consumer.onPaymentSagaEvent(event);

        assertEquals(RechargeStatus.FAILED, recharge.getStatus());
        verify(rechargeRepository).save(recharge);
    }

    @Test
    void onPaymentSagaEvent_ShouldThrowWhenRechargeMissing() {
        when(rechargeRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(RechargeNotFoundException.class, () -> consumer.onPaymentSagaEvent(event));
    }

    @Test
    void onPaymentSagaEvent_ShouldThrowForUnknownEventType() {
        event.setEventType("unknown");

        assertThrows(IllegalArgumentException.class, () -> consumer.onPaymentSagaEvent(event));
        verify(rechargeRepository, never()).findById(any());
    }
}
