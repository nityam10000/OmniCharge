package com.omnicharge.rechargeprocessing.repository;

import com.omnicharge.rechargeprocessing.entity.Recharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IRechargeRepository extends JpaRepository<Recharge,Long> {
    List<Recharge> findByUserId(Long userId);
    List<Recharge> findByPlanId(Long planId);
}
