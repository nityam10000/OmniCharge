package com.omnicharge.paymentservice.service.implementation;

import com.omnicharge.paymentservice.dto.TransactionRequestDTO;
import com.omnicharge.paymentservice.dto.TransactionResponseDTO;
import com.omnicharge.paymentservice.entity.Transaction;
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
    
    


    public TransactionService(ITransactionRepository transactionRepository, Mapper mapper) {
		super();
		this.transactionRepository = transactionRepository;
		this.mapper = mapper;
	}

	@Override
    public TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequestDTO) {
        return mapper.toTransactionResponseDTO(transactionRepository.save(mapper.toTransaction(transactionRequestDTO)));
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
