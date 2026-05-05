package com.omnicharge.paymentservice.consumer;

import com.omnicharge.paymentservice.dto.PaymentSagaEvent;
import com.omnicharge.paymentservice.entity.Transaction;
import com.omnicharge.paymentservice.enums.TransactionStatus;
import com.omnicharge.paymentservice.repository.ITransactionRepository;
import com.omnicharge.paymentservice.service.RazorpayRefundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SagaReplyConsumerTest {

    @Mock
    private ITransactionRepository transactionRepository;

    @Mock
    private RazorpayRefundService razorpayRefundService;

    private SagaReplyConsumer consumer;
    private PaymentSagaEvent event;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        consumer = new SagaReplyConsumer(transactionRepository, razorpayRefundService);
        transactionId = UUID.randomUUID();
        event = PaymentSagaEvent.builder()
                .sagaId("saga-1")
                .transactionId(transactionId)
                .rechargeId(5L)
                .build();
    }

    @Test
    void onSagaDeadLetter_ShouldRefundSuccessfulTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setRechargeId(5L);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setRazorpayPaymentId("pay_123");
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        consumer.onSagaDeadLetter(event);

        verify(transactionRepository).save(transaction);
        verify(razorpayRefundService).refundAndNotify(same(transaction), contains("rechargeId=5"));
    }

    @Test
    void onSagaDeadLetter_ShouldSkipRefundWhenPaymentIdMissing() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setRechargeId(5L);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setRazorpayPaymentId("");
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        consumer.onSagaDeadLetter(event);

        verify(transactionRepository).save(transaction);
        verify(razorpayRefundService, never()).refundAndNotify(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void onSagaDeadLetter_ShouldSkipAlreadyFailedTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setStatus(TransactionStatus.FAILED);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        consumer.onSagaDeadLetter(event);

        verify(transactionRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(razorpayRefundService, never()).refundAndNotify(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void onSagaDeadLetter_ShouldHandleMissingTransaction() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        consumer.onSagaDeadLetter(event);

        verify(transactionRepository).findById(transactionId);
        verify(razorpayRefundService, never()).refundAndNotify(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void onRechargeUpdated_ShouldOnlyLog() {
        consumer.onRechargeUpdated(event);
    }
}
