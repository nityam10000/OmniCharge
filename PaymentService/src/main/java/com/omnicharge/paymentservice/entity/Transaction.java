package com.omnicharge.paymentservice.entity;

import com.omnicharge.paymentservice.enums.TransactionStatus;
import com.omnicharge.paymentservice.enums.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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

    private String failureReason;


    @PrePersist
    public void setTimestamp() {
        this.timestamp = LocalDateTime.now();
    }


	public UUID getTransactionId() {
		return transactionId;
	}


	public Double getAmount() {
		return amount;
	}


	public void setAmount(Double amount) {
		this.amount = amount;
	}


	public TransactionStatus getStatus() {
		return status;
	}


	public void setStatus(TransactionStatus status) {
		this.status = status;
	}


	public LocalDateTime getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}


	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}


	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}


	public Long getRechargeId() {
		return rechargeId;
	}


	public void setRechargeId(Long rechargeId) {
		this.rechargeId = rechargeId;
	}


	public Long getUserId() {
		return userId;
	}


	public void setUserId(Long userId) {
		this.userId = userId;
	}


	public String getFailureReason() {
		return failureReason;
	}


	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}
    
    

}
