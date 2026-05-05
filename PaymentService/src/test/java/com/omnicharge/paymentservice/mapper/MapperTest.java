package com.omnicharge.paymentservice.mapper;

import com.omnicharge.paymentservice.dto.TransactionResponseDTO;
import com.omnicharge.paymentservice.entity.Transaction;
import com.omnicharge.paymentservice.enums.PaymentMethod;
import com.omnicharge.paymentservice.enums.TransactionStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MapperTest {

    private final Mapper mapper = new Mapper();

    @Test
    void toTransactionResponseDTO_ShouldMapCorrectly() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setRechargeId(456L);
        transaction.setUserId(1L);
        transaction.setAmount(100.0);
        transaction.setPaymentMethod(PaymentMethod.UPI);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setRazorpayOrderId("order_789");
        transaction.setRazorpayPaymentId("pay_012");
        transaction.setTimestamp(LocalDateTime.now());

        TransactionResponseDTO dto = mapper.toTransactionResponseDTO(transaction);

        assertNotNull(dto.getId());
        assertEquals(456L, dto.getRechargeId());
        assertEquals(1L, dto.getUserId());
        assertEquals(100.0, dto.getAmount());
        assertEquals(PaymentMethod.UPI, dto.getPaymentMethod());
        assertEquals(TransactionStatus.SUCCESS, dto.getStatus());
        assertEquals("order_789", dto.getRazorpayOrderId());
        assertEquals("pay_012", dto.getRazorpayPaymentId());
        assertNotNull(dto.getCreatedAt());
    }
}
