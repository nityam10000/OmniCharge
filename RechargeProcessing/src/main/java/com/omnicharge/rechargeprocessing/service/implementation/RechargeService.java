package com.omnicharge.rechargeprocessing.service.implementation;


import com.omnicharge.rechargeprocessing.dto.RechargeRequestDTO;
import com.omnicharge.rechargeprocessing.dto.RechargeResponseDTO;
import com.omnicharge.rechargeprocessing.entity.Recharge;
import com.omnicharge.rechargeprocessing.exception.RechargeNotFoundException;
import com.omnicharge.rechargeprocessing.mapper.RechargeMapper;
import com.omnicharge.rechargeprocessing.repository.IRechargeRepository;
import com.omnicharge.rechargeprocessing.service.IRechargeService;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.mapper.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RechargeService implements IRechargeService {

    private final IRechargeRepository rechargeRepository;
    private final RechargeMapper rechargeMapper;


    @Override
    public RechargeResponseDTO addRecharge(RechargeRequestDTO rechargeRequestDTO) {
        Recharge recharge = rechargeMapper.toRecharge(rechargeRequestDTO);
        Recharge savedRecharge = rechargeRepository.save(recharge);
        return rechargeMapper.toRechargeResponseDTO(savedRecharge);
    }

    @Override
    public Page<RechargeResponseDTO> getAllRechargs(Pageable pageable) {
        Page<Recharge> rechargePage = rechargeRepository.findAll(pageable);
        return rechargePage.map(rechargeMapper::toRechargeResponseDTO);
    }

    @Override
    public String deleteRecharge(Long id) {
        Recharge recharge = rechargeRepository.findById(id).orElseThrow(()->new RechargeNotFoundException("Recharge not found"));
        rechargeRepository.delete(recharge);
        return "Recharge deleted!!";
    }


    public List<RechargeResponseDTO> findAllRechargeByUserId(Long userId) {
        List<Recharge> recharges = rechargeRepository.findByUserId(userId);
        return recharges.stream().map(rechargeMapper::toRechargeResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<RechargeResponseDTO> findAllRechargeByPlanId(Long planId) {
        List<Recharge> recharges = rechargeRepository.findByPlanId(planId);
        return recharges.stream().map(rechargeMapper::toRechargeResponseDTO).collect(Collectors.toList());
    }
}
