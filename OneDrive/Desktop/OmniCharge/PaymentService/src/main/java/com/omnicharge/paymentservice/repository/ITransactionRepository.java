package com.omnicharge.paymentservice.repository;

import com.omnicharge.paymentservice.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByUserId(Long userId);
    Optional<Transaction> findByRechargeId(Long rechargeId);
}
