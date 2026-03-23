package com.omnicharge.rechargeprocessing.service.implementation;

import com.omnicharge.rechargeprocessing.dto.*;
import com.omnicharge.rechargeprocessing.entity.Recharge;
import com.omnicharge.rechargeprocessing.enums.RechargeStatus;
import com.omnicharge.rechargeprocessing.enums.TransactionStatus;
import com.omnicharge.rechargeprocessing.exception.RechargeNotFoundException;
import com.omnicharge.rechargeprocessing.feignClient.IOperatorPlanClient;
import com.omnicharge.rechargeprocessing.mapper.RechargeMapper;
import com.omnicharge.rechargeprocessing.repository.IRechargeRepository;
import com.omnicharge.rechargeprocessing.service.IRechargeService;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RechargeService implements IRechargeService {

    private final IRechargeRepository rechargeRepository;
    private final RechargeMapper rechargeMapper;
    private final IOperatorPlanClient operatorPlanClient;
    // IPaymentClient intentionally removed — RechargeService no longer calls PaymentService
    // directly. The Transaction is created by the frontend via POST /transaction/create-order.

    @Override
    @Retry(name = "OPERATORPLANMANAGEMENT", fallbackMethod = "addRechargeFallback")
    public RechargeResponseDTO addRecharge(RechargeRequestDTO rechargeRequestDTO) {

        Long userId = getLoggedInUserId();

        // Validate the plan exists (retried via OPERATORPLANMANAGEMENT circuit breaker)
        PlanResponseDTO plan = operatorPlanClient.getPlanById(rechargeRequestDTO.getPlanId());

        // Only save the recharge as PENDING here.
        // The Transaction is created separately when the frontend calls POST /transaction/create-order.
        // This avoids the duplicate PENDING transaction bug.
        Recharge recharge = new Recharge();
        recharge.setUserId(userId);
        recharge.setPlanId(rechargeRequestDTO.getPlanId());
        recharge.setStatus(RechargeStatus.PENDING);
        Recharge saved = rechargeRepository.save(recharge);

        return RechargeResponseDTO.builder()
                .rechargeId(saved.getId())
                .status(saved.getStatus())
                .amount(plan.getAmount())
                .planId(saved.getPlanId())
                .transactionStatus(String.valueOf(TransactionStatus.PENDING))
                .build();
    }

    public RechargeResponseDTO addRechargeFallback(RechargeRequestDTO rechargeRequestDTO, Exception e) {
        log.error("All retry attempts exhausted for addRecharge. planId={}, error={}",
                rechargeRequestDTO.getPlanId(), e.getMessage());
        throw new RuntimeException(
                "Recharge creation failed after retries. Dependent service is unavailable. Please try again later."
        );
    }

    @Override
    public RechargeResponseDTO getRechargeById(Long id) {
        Recharge recharge = rechargeRepository.findById(id)
                .orElseThrow(() -> new RechargeNotFoundException("Recharge not found for id: " + id));
        return rechargeMapper.toRechargeResponseDTO(recharge);
    }

    @Override
    public Page<RechargeResponseDTO> getAllRechargs(Pageable pageable) {
        Page<Recharge> rechargePage = rechargeRepository.findAll(pageable);
        return rechargePage.map(rechargeMapper::toRechargeResponseDTO);
    }

    @Override
    public String deleteRecharge(Long id) {
        Recharge recharge = rechargeRepository.findById(id)
                .orElseThrow(() -> new RechargeNotFoundException("Recharge not found"));
        rechargeRepository.delete(recharge);
        return "Recharge deleted!!";
    }

    @Override
    public List<RechargeResponseDTO> findAllRechargeByUserId(Long userId) {
        List<Recharge> recharges = rechargeRepository.findByUserId(userId);
        return recharges.stream().map(rechargeMapper::toRechargeResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<RechargeResponseDTO> findAllRechargeByPlanId(Long planId) {
        List<Recharge> recharges = rechargeRepository.findByPlanId(planId);
        return recharges.stream().map(rechargeMapper::toRechargeResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<RechargeResponseDTO> getCurrentUserRecharges() {
        Long userId = getLoggedInUserId();
        List<Recharge> recharges = rechargeRepository.findByUserId(userId);
        return recharges.stream()
                .map(rechargeMapper::toRechargeResponseDTO)
                .toList();
    }

    @Override
    public void updateRechargeStatus(Long rechargeId, RechargeStatus status) {
        Recharge recharge = rechargeRepository.findById(rechargeId)
                .orElseThrow(() -> new RechargeNotFoundException("Recharge not found"));
        recharge.setStatus(status);
        rechargeRepository.save(recharge);
    }

    private void validateAccess(Long userId) throws AccessDeniedException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        if ("ROLE_ADMIN".equals(role)) return;
        Long loggedInUserId = getLoggedInUserId();
        if (!loggedInUserId.equals(userId)) {
            throw new AccessDeniedException("Access Denied");
        }
    }

    private Long getLoggedInUserId() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        return Long.parseLong(request.getHeader("X-User-Id"));
    }
}