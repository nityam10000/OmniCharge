package com.omnicharge.rechargeprocessing.feignClient;
import java.util.List;

import com.omnicharge.rechargeprocessing.feignClient.fallback.IPaymentClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.omnicharge.rechargeprocessing.dto.TransactionRequestDTO;
import com.omnicharge.rechargeprocessing.dto.TransactionResponseDTO;

@FeignClient(name = "PAYMENTSERVICE", fallback = IPaymentClientFallback.class)
public interface IPaymentClient {
	 	@PostMapping("/transaction/add")
	    TransactionResponseDTO createTransaction(@RequestBody TransactionRequestDTO transactionRequestDTO);

	    @GetMapping("/transaction/user/{userId}")
	    List<TransactionResponseDTO> getAllTransactionsByUserId(@PathVariable("userId") Long userId);

	    @GetMapping("/transaction/recharge/{rechargeId}")
	    TransactionResponseDTO getTransactionByRechargeId(@PathVariable("rechargeId") Long rechargeId);
	

}