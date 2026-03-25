package com.omnicharge.rechargeprocessing.mapper;


import com.omnicharge.rechargeprocessing.dto.RechargeRequestDTO;
import com.omnicharge.rechargeprocessing.dto.RechargeResponseDTO;
import com.omnicharge.rechargeprocessing.entity.Recharge;
import org.springframework.stereotype.Component;


@Component
public class RechargeMapper {

    public RechargeResponseDTO toRechargeResponseDTO(Recharge recharge) {
        return RechargeResponseDTO.builder()
                .rechargeId(recharge.getId())
                .status(recharge.getStatus())
                .planId(recharge.getPlanId())
                .userId(recharge.getUserId())  // exposed for ownership validation
                .build();
    }

    public Recharge toRecharge(RechargeRequestDTO dto) {
        Recharge recharge = new Recharge();
        recharge.setPlanId(dto.getPlanId());
        recharge.setStatus(null); // always set manually in service
        return recharge;
    }
}