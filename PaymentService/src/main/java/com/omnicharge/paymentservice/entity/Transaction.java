package com.omnicharge.paymentservice.entity;

import com.omnicharge.paymentservice.enums.TransactionStatus;
import com.omnicharge.paymentservice.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID transactionId;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private Long rechargeId;

    private Long userId;

    // ✅ Stored at createOrder() time so notifications always go to the correct user,
    //    not whoever happens to be in the SecurityContext at verify time.
    private String userEmail;

    private String failureReason;

    @PrePersist
    public void setTimestamp() {
        this.timestamp = LocalDateTime.now();
    }

    private String razorpayOrderId;
    private String razorpayPaymentId;
}