package com.omnicharge.rechargeprocessing.mapper;


import com.omnicharge.rechargeprocessing.dto.RechargeRequestDTO;
import com.omnicharge.rechargeprocessing.dto.RechargeResponseDTO;
import com.omnicharge.rechargeprocessing.entity.Recharge;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;


@Data
@Component
public class RechargeMapper {

    public RechargeResponseDTO toRechargeResponseDTO(Recharge recharge) {
        RechargeResponseDTO rechargeResponseDTO = new RechargeResponseDTO();
        rechargeResponseDTO.setId(recharge.getId());
        rechargeResponseDTO.setStatus(recharge.getStatus());
        return rechargeResponseDTO;
    }

    public Recharge toRecharge(RechargeRequestDTO rechargeRequestDTO) {
        Recharge recharge = new Recharge();
        recharge.setStatus(rechargeRequestDTO.getStatus());
        recharge.setPlanId(rechargeRequestDTO.getPlanId());

        return recharge;
    }
}
