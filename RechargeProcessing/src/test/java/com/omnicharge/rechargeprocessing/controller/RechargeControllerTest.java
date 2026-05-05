package com.omnicharge.rechargeprocessing.controller;

import com.omnicharge.rechargeprocessing.dto.RechargeRequestDTO;
import com.omnicharge.rechargeprocessing.dto.RechargeResponseDTO;
import com.omnicharge.rechargeprocessing.enums.PaymentMethod;
import com.omnicharge.rechargeprocessing.enums.RechargeStatus;
import com.omnicharge.rechargeprocessing.service.IRechargeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RechargeControllerTest {

    @Mock
    private IRechargeService rechargeService;

    private RechargeController controller;
    private RechargeRequestDTO requestDTO;
    private RechargeResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        controller = new RechargeController(rechargeService);
        requestDTO = RechargeRequestDTO.builder()
                .planId(5L)
                .paymentMethod(PaymentMethod.UPI)
                .build();
        responseDTO = RechargeResponseDTO.builder()
                .rechargeId(1L)
                .status(RechargeStatus.PENDING)
                .planId(5L)
                .userId(10L)
                .build();
    }

    @Test
    void addRecharge_ShouldReturnOk() {
        when(rechargeService.addRecharge(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<RechargeResponseDTO> response = controller.addRecharge(requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responseDTO, response.getBody());
    }

    @Test
    void getRechargeById_ShouldReturnBody() {
        when(rechargeService.getRechargeById(1L)).thenReturn(responseDTO);

        ResponseEntity<RechargeResponseDTO> response = controller.getRechargeById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responseDTO, response.getBody());
    }

    @Test
    void getAllRecharges_ShouldReturnPage() {
        Page<RechargeResponseDTO> page = new PageImpl<>(List.of(responseDTO), PageRequest.of(0, 10), 1);
        when(rechargeService.getAllRechargs(PageRequest.of(0, 10))).thenReturn(page);

        ResponseEntity<Page<RechargeResponseDTO>> response = controller.getAllRecharges(PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void deleteRecharge_ShouldReturnMessage() {
        when(rechargeService.deleteRecharge(1L)).thenReturn("Recharge deleted!!");

        ResponseEntity<String> response = controller.deleteRecharge(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Recharge deleted!!", response.getBody());
    }

    @Test
    void getRechargeByUserId_ShouldReturnList() {
        List<RechargeResponseDTO> responses = List.of(responseDTO);
        when(rechargeService.findAllRechargeByUserId(10L)).thenReturn(responses);

        ResponseEntity<List<RechargeResponseDTO>> response = controller.getRechargeByUserId(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responses, response.getBody());
    }

    @Test
    void getMyRecharges_ShouldReturnCurrentUserRecharges() {
        Page<RechargeResponseDTO> responses = new PageImpl<>(List.of(responseDTO));
        when(rechargeService.getCurrentUserRecharges(PageRequest.of(0, 10))).thenReturn(responses);

        ResponseEntity<Page<RechargeResponseDTO>> response = controller.getMyRecharges(PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responses, response.getBody());
    }

    @Test
    void getRechargeByPlanId_ShouldReturnList() {
        List<RechargeResponseDTO> responses = List.of(responseDTO);
        when(rechargeService.findAllRechargeByPlanId(5L)).thenReturn(responses);

        ResponseEntity<List<RechargeResponseDTO>> response = controller.getRechargeByPlanId(5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responses, response.getBody());
    }

    @Test
    void updateRechargeStatus_ShouldDelegateUsingUpperCaseStatus() {
        ResponseEntity<String> response = controller.updateRechargeStatus(1L, "success");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Status updated successfully", response.getBody());
        verify(rechargeService).updateRechargeStatus(1L, RechargeStatus.SUCCESS);
    }
}
