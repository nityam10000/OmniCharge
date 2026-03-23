package com.omnicharge.paymentservice.dto;

import com.omnicharge.paymentservice.enums.PaymentMethod;
import com.omnicharge.paymentservice.enums.TransactionStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransactionRequestDTO {
    @Min(value = 1, message = "Invalid amount!!")
    private Double amount;

    @NotNull
    private Long rechargeId;

    @NotNull
    private TransactionStatus status;

    @NotNull
    private Long userId;

    @NotNull
    private PaymentMethod paymentMethod;

	public TransactionRequestDTO(@Min(value = 1, message = "Invalid amount!!") Double amount, @NotNull Long rechargeId,
			@NotNull TransactionStatus status, @NotNull Long userId, @NotNull PaymentMethod paymentMethod) {
		super();
		this.amount = amount;
		this.rechargeId = rechargeId;
		this.status = status;
		this.userId = userId;
		this.paymentMethod = paymentMethod;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Long getRechargeId() {
		return rechargeId;
	}

	public void setRechargeId(Long rechargeId) {
		this.rechargeId = rechargeId;
	}

	public TransactionStatus getStatus() {
		return status;
	}

	public void setStatus(TransactionStatus status) {
		this.status = status;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public TransactionRequestDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    
}
