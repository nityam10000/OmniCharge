package com.omnicharge.paymentservice.controller;

import com.omnicharge.paymentservice.dto.TransactionRequestDTO;
import com.omnicharge.paymentservice.dto.TransactionResponseDTO;
import com.omnicharge.paymentservice.service.ITransactionService;
import com.omnicharge.paymentservice.service.implementation.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final ITransactionService transactionService;
    
    

    public TransactionController(ITransactionService transactionService) {
		super();
		this.transactionService = transactionService;
	}

	@PostMapping("add")
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody TransactionRequestDTO transactionRequestDTO) {
        TransactionResponseDTO transactionResponseDTO = transactionService.createTransaction(transactionRequestDTO);
        return new ResponseEntity<>(transactionResponseDTO, HttpStatus.OK );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactionsByUserId(@PathVariable Long userId) {
        List<TransactionResponseDTO> transactionResponseDTO = transactionService.getAllTransactionsByUserId(userId);
        return new ResponseEntity<>(transactionResponseDTO, HttpStatus.OK );
    }

    @GetMapping("recharge/{rechargeId}")
    public ResponseEntity<TransactionResponseDTO> getTransactionByRechargeId(@PathVariable Long rechargeId) {
        TransactionResponseDTO transactionResponseDTO = transactionService.getTransactionByRechargeId(rechargeId);
        return new ResponseEntity<>(transactionResponseDTO, HttpStatus.OK );
    }

}
