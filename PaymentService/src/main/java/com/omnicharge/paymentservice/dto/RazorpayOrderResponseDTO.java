package com.omnicharge.paymentservice.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RazorpayOrderResponseDTO implements Serializable{
	private final long serialIzableID = 1L;
    private String razorpayOrderId;
    private Double amount;
    private String currency;
    private Long rechargeId;
}