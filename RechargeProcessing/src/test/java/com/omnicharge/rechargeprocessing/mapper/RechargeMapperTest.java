package com.omnicharge.rechargeprocessing.mapper;

import com.omnicharge.rechargeprocessing.dto.RechargeRequestDTO;
import com.omnicharge.rechargeprocessing.dto.RechargeResponseDTO;
import com.omnicharge.rechargeprocessing.entity.Recharge;
import com.omnicharge.rechargeprocessing.enums.PaymentMethod;
import com.omnicharge.rechargeprocessing.enums.RechargeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RechargeMapperTest {

    private RechargeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RechargeMapper();
    }

    @Test
    void toRechargeResponseDTO_ShouldMapEntityFields() {
        Recharge recharge = new Recharge();
        recharge.setId(1L);
        recharge.setUserId(10L);
        recharge.setPlanId(5L);
        recharge.setStatus(RechargeStatus.SUCCESS);

        RechargeResponseDTO response = mapper.toRechargeResponseDTO(recharge);

        assertEquals(1L, response.getRechargeId());
        assertEquals(10L, response.getUserId());
        assertEquals(5L, response.getPlanId());
        assertEquals(RechargeStatus.SUCCESS, response.getStatus());
    }

    @Test
    void toRecharge_ShouldMapPlanIdAndLeaveStatusNull() {
        RechargeRequestDTO dto = RechargeRequestDTO.builder()
                .planId(5L)
                .paymentMethod(PaymentMethod.UPI)
                .build();

        Recharge recharge = mapper.toRecharge(dto);

        assertEquals(5L, recharge.getPlanId());
        assertNull(recharge.getStatus());
    }
}
