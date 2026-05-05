package com.omnicharge.paymentservice.repository;

import com.omnicharge.paymentservice.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByUserId(Long userId);
    Page<Transaction> findByUserId(Long userId, Pageable pageable);
    
    @Query(value = "SELECT t FROM Transaction t WHERE t.rechargeId = :rechargeId ORDER BY t.timestamp DESC LIMIT 1")
    Optional<Transaction> findByRechargeId(@Param("rechargeId") Long rechargeId);
    
    Optional<Transaction> findByRazorpayOrderId(String razorpayOrderId);
}

