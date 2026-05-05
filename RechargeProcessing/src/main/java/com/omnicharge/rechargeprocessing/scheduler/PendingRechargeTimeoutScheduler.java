package com.omnicharge.rechargeprocessing.scheduler;

import com.omnicharge.rechargeprocessing.entity.Recharge;
import com.omnicharge.rechargeprocessing.enums.RechargeStatus;
import com.omnicharge.rechargeprocessing.repository.IRechargeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingRechargeTimeoutScheduler {

    private final IRechargeRepository rechargeRepository;
    
    // ================= SCHEDULED TASK =================
    // Runs every 5 minutes to check for pending recharges older than 10 minutes
    @Scheduled(fixedDelay = 300000) // 300000 ms = 5 minutes
    public void markExpiredPendingRechargesAsFailed() {
        log.info("=== Starting Pending Recharge Timeout Check ===");
        
        try {
            // Calculate the cutoff time: 10 minutes ago
            LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
            
            // Find all pending recharges older than 10 minutes
            List<Recharge> expiredRecharges = rechargeRepository.findPendingRechargesOlderThan(tenMinutesAgo);
            
            if (expiredRecharges.isEmpty()) {
                log.info("No expired pending recharges found.");
                return;
            }
            
            log.warn("Found {} expired pending recharges. Marking them as FAILED...", expiredRecharges.size());
            
            // Mark each as failed
            for (Recharge recharge : expiredRecharges) {
                recharge.setStatus(RechargeStatus.FAILED);
                rechargeRepository.save(recharge);
                log.warn("Recharge ID {} marked as FAILED (pending for more than 10 minutes). CreatedAt: {}", 
                        recharge.getId(), recharge.getCreatedAt());
            }
            
            log.info("=== Completed Pending Recharge Timeout Check. {} recharges marked as FAILED ===", expiredRecharges.size());
            
        } catch (Exception e) {
            log.error("Error in markExpiredPendingRechargesAsFailed scheduler: {}", e.getMessage(), e);
        }
    }
}
