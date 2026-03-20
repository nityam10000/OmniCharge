package com.omnicharge.rechargeprocessing.service;


import com.omnicharge.rechargeprocessing.dto.RechargeRequestDTO;
import com.omnicharge.rechargeprocessing.dto.RechargeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IRechargeService {

    public RechargeResponseDTO addRecharge(RechargeRequestDTO rechargeRequestDTO);
    public Page<RechargeResponseDTO> getAllRechargs(Pageable pageable);
    public String deleteRecharge(Long id);
    public List<RechargeResponseDTO> findAllRechargeByUserId(Long userId);
    public List<RechargeResponseDTO> findAllRechargeByPlanId(Long planId);
    public List<RechargeResponseDTO> getCurrentUserRecharges();
}
