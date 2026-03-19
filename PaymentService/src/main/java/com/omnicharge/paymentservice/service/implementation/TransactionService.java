package com.omnicharge.paymentservice.service.implementation;

import com.omnicharge.paymentservice.dto.TransactionRequestDTO;
import com.omnicharge.paymentservice.dto.TransactionResponseDTO;
import com.omnicharge.paymentservice.entity.Transaction;
import com.omnicharge.paymentservice.enums.TransactionStatus;
import com.omnicharge.paymentservice.exception.TransactionNotFoundException;
import com.omnicharge.paymentservice.mapper.Mapper;
import com.omnicharge.paymentservice.repository.ITransactionRepository;
import com.omnicharge.paymentservice.service.ITransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {
    private final ITransactionRepository transactionRepository;
    private final Mapper mapper;
    
   
    @Override
    public TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequestDTO) {
        Transaction transaction = mapper.toTransaction(transactionRequestDTO);

        // Simple dummy logic for testing
        if (transactionRequestDTO.getAmount() >= 1) {
            transaction.setStatus(TransactionStatus.SUCCESS);
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapper.toTransactionResponseDTO(savedTransaction);
    }

    @Override
    public List<TransactionResponseDTO> getAllTransactionsByUserId(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return transactions.stream().map(mapper::toTransactionResponseDTO).collect(Collectors.toList());

    }

    @Override
    public TransactionResponseDTO getTransactionByRechargeId(Long rechargeId) {
        Transaction transactions= transactionRepository.findByRechargeId(rechargeId).orElseThrow(()->new TransactionNotFoundException("Transaction Not Found!!"));
        return  mapper.toTransactionResponseDTO(transactions);
    }


}
