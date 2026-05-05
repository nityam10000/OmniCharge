package com.omnicharge.paymentservice.controller;

import com.omnicharge.paymentservice.dto.*;
import com.omnicharge.paymentservice.service.ITransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final ITransactionService transactionService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactionsByUserId(
            @PathVariable Long userId) {
        List<TransactionResponseDTO> response = transactionService.getAllTransactionsByUserId(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/mytransactions")
    public ResponseEntity<Page<TransactionResponseDTO>> getMyTransactions(Pageable pageable) {
        return ResponseEntity.ok(transactionService.getMyTransactions(pageable));
    }

    @GetMapping("/recharge/{rechargeId}")
    public ResponseEntity<TransactionResponseDTO> getTransactionByRechargeId(
            @PathVariable Long rechargeId) {
        TransactionResponseDTO response = transactionService.getTransactionByRechargeId(rechargeId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/create-order")
    public ResponseEntity<RazorpayOrderResponseDTO> createOrder(
            @Valid @RequestBody RazorpayOrderRequestDTO dto) {
        return ResponseEntity.ok(transactionService.createOrder(dto));
    }

    @PostMapping("/verify")
    public ResponseEntity<TransactionResponseDTO> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequestDTO dto) {
        return ResponseEntity.ok(transactionService.verifyPayment(dto));
    }
}