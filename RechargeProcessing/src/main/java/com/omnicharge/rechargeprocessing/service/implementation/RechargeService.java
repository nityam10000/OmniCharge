package com.omnicharge.rechargeprocessing.service.implementation;

import com.omnicharge.rechargeprocessing.dto.PlanResponseDTO;
import com.omnicharge.rechargeprocessing.dto.RechargeRequestDTO;
import com.omnicharge.rechargeprocessing.dto.RechargeResponseDTO;
import com.omnicharge.rechargeprocessing.entity.Recharge;
import com.omnicharge.rechargeprocessing.enums.RechargeStatus;
import com.omnicharge.rechargeprocessing.enums.TransactionStatus;
import com.omnicharge.rechargeprocessing.exception.OperatorPlanMismachedException;
import com.omnicharge.rechargeprocessing.exception.RechargeNotFoundException;
import com.omnicharge.rechargeprocessing.exception.ServiceUnavailableException;
import com.omnicharge.rechargeprocessing.feignClient.IOperatorPlanClient;
import com.omnicharge.rechargeprocessing.mapper.RechargeMapper;
import com.omnicharge.rechargeprocessing.repository.IRechargeRepository;
import com.omnicharge.rechargeprocessing.service.IRechargeService;
import com.omnicharge.rechargeprocessing.support.RequestUserContext;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
    private final RequestUserContext requestUserContext;

    @Override
    @Retry(name = "OPERATORPLANMANAGEMENT", fallbackMethod = "addRechargeFallback")
    public RechargeResponseDTO addRecharge(RechargeRequestDTO rechargeRequestDTO) {
        Long userId = getLoggedInUserId();
        PlanResponseDTO plan = operatorPlanClient.getPlanById(rechargeRequestDTO.getPlanId());

        if (plan == null) {
            throw new OperatorPlanMismachedException("Plan not found for id: " + rechargeRequestDTO.getPlanId());
        }
        if (plan.getOperatorId() == null) {
            throw new OperatorPlanMismachedException("Selected plan does not have an operator mapping.");
        }
        if (!plan.getOperatorId().equals(rechargeRequestDTO.getOperatorId())) {
            throw new OperatorPlanMismachedException(
                    "Selected plan does not belong to operatorId: " + rechargeRequestDTO.getOperatorId()
            );
        }

        Recharge recharge = new Recharge();
        recharge.setUserId(userId);
        recharge.setPlanId(rechargeRequestDTO.getPlanId());
        recharge.setStatus(RechargeStatus.PENDING);
        recharge.setAmount(plan.getAmount());                        // ← add this
        recharge.setTransactionStatus(TransactionStatus.PENDING);
        Recharge saved = rechargeRepository.save(recharge);

        RechargeResponseDTO response = rechargeMapper.toRechargeResponseDTO(saved);
        response.setAmount(plan.getAmount());
        response.setTransactionStatus(String.valueOf(TransactionStatus.PENDING));
        return response;
    }

    public RechargeResponseDTO addRechargeFallback(RechargeRequestDTO rechargeRequestDTO, Exception e) {
        if (e instanceof OperatorPlanMismachedException operatorPlanMismachedException) {
            throw operatorPlanMismachedException;
        }
        if (e.getCause() instanceof OperatorPlanMismachedException operatorPlanMismachedException) {
            throw operatorPlanMismachedException;
        }
        if (e instanceof NumberFormatException) {
            throw (NumberFormatException) e;
        }

        log.error("All retry attempts exhausted for addRecharge. operatorId={}, planId={}, error={}",
                rechargeRequestDTO.getOperatorId(), rechargeRequestDTO.getPlanId(), e.getMessage());
        throw new ServiceUnavailableException(
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
    public Page<RechargeResponseDTO> getCurrentUserRecharges(Pageable pageable) {
        Long userId = getLoggedInUserId();
        Page<Recharge> rechargePage = rechargeRepository.findByUserId(userId, pageable);
        return rechargePage.map(rechargeMapper::toRechargeResponseDTO);
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
        if ("ROLE_ADMIN".equals(role)) {
            return;
        }
        Long loggedInUserId = getLoggedInUserId();
        if (!loggedInUserId.equals(userId)) {
            throw new AccessDeniedException("Access Denied");
        }
    }

    private Long getLoggedInUserId() {
        return Long.parseLong(requestUserContext.getUserIdHeader());
    }
}
