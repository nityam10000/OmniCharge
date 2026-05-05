package com.omnicharge.rechargeprocessing.service;

import com.omnicharge.rechargeprocessing.dto.RechargeRequestDTO;
import com.omnicharge.rechargeprocessing.dto.RechargeResponseDTO;
import com.omnicharge.rechargeprocessing.enums.RechargeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IRechargeService {

    RechargeResponseDTO addRecharge(RechargeRequestDTO rechargeRequestDTO);
    Page<RechargeResponseDTO> getAllRechargs(Pageable pageable);
    String deleteRecharge(Long id);
    List<RechargeResponseDTO> findAllRechargeByUserId(Long userId);
    List<RechargeResponseDTO> findAllRechargeByPlanId(Long planId);
    Page<RechargeResponseDTO> getCurrentUserRecharges(Pageable pageable);
    void updateRechargeStatus(Long rechargeId, RechargeStatus status);

    // NEW — used by PaymentService via Feign to get planId without amount spoofing
    RechargeResponseDTO getRechargeById(Long id);
}