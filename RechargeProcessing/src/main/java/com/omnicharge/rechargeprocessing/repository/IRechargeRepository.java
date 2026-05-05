
package com.omnicharge.rechargeprocessing.repository;

import java.time.LocalDateTime;
import java.util.List;
import com.omnicharge.rechargeprocessing.entity.Recharge;
import com.omnicharge.rechargeprocessing.enums.RechargeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface IRechargeRepository extends JpaRepository<Recharge,Long> {
    List<Recharge> findByUserId(Long userId);
    Page<Recharge> findByUserId(Long userId, Pageable pageable);
    List<Recharge> findByPlanId(Long planId);
    
    // ================= PENDING RECHARGE TIMEOUT =================
    @Query("SELECT r FROM Recharge r WHERE r.status = 'PENDING' AND r.createdAt < :cutoffTime")
    List<Recharge> findPendingRechargesOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
}
