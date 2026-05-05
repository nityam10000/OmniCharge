package com.omnicharge.rechargeprocessing.service.implementation;

import com.omnicharge.rechargeprocessing.dto.PlanResponseDTO;
import com.omnicharge.rechargeprocessing.dto.RechargeRequestDTO;
import com.omnicharge.rechargeprocessing.dto.RechargeResponseDTO;
import com.omnicharge.rechargeprocessing.entity.Recharge;
import com.omnicharge.rechargeprocessing.enums.PaymentMethod;
import com.omnicharge.rechargeprocessing.enums.RechargeStatus;
import com.omnicharge.rechargeprocessing.exception.RechargeNotFoundException;
import com.omnicharge.rechargeprocessing.feignClient.IOperatorPlanClient;
import com.omnicharge.rechargeprocessing.mapper.RechargeMapper;
import com.omnicharge.rechargeprocessing.repository.IRechargeRepository;
import com.omnicharge.rechargeprocessing.support.RequestUserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RechargeServiceTest {

    @Mock private IRechargeRepository rechargeRepository;
    @Mock private RechargeMapper rechargeMapper;
    @Mock private IOperatorPlanClient operatorPlanClient;
    @Mock private RequestUserContext requestUserContext;

    @InjectMocks
    private RechargeService rechargeService;

    private Recharge recharge;
    private RechargeRequestDTO requestDTO;
    private RechargeResponseDTO responseDTO;
    private PlanResponseDTO planResponseDTO;

    @BeforeEach
    void setUp() {
        recharge = new Recharge();
        recharge.setId(1L);
        recharge.setUserId(10L);
        recharge.setPlanId(5L);
        recharge.setStatus(RechargeStatus.PENDING);

        requestDTO = RechargeRequestDTO.builder()
                .operatorId(1L)
                .planId(5L)
                .paymentMethod(PaymentMethod.UPI)
                .build();

        responseDTO = RechargeResponseDTO.builder()
                .rechargeId(1L)
                .status(RechargeStatus.PENDING)
                .planId(5L)
                .userId(10L)
                .build();

        planResponseDTO = new PlanResponseDTO(1L, 299.0, "28 days", "Unlimited calls + 2GB/day");
    }

    @Test
    @DisplayName("addRecharge: should save recharge with PENDING status and attach plan amount")
    void addRecharge_ShouldSaveAndReturn_WithPlanAmount() {
        mockLoggedInUserId();
        when(operatorPlanClient.getPlanById(5L)).thenReturn(planResponseDTO);
        when(rechargeRepository.save(any(Recharge.class))).thenReturn(recharge);
        when(rechargeMapper.toRechargeResponseDTO(recharge)).thenReturn(responseDTO);

        RechargeResponseDTO result = rechargeService.addRecharge(requestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getRechargeId());
        assertEquals(RechargeStatus.PENDING, result.getStatus());
        assertEquals(299.0, result.getAmount());
        assertEquals("PENDING", result.getTransactionStatus());
        verify(rechargeRepository).save(any(Recharge.class));
    }

    @Test
    @DisplayName("addRecharge: should throw when user header is invalid")
    void addRecharge_ShouldThrow_WhenUserHeaderInvalid() {
        when(requestUserContext.getUserIdHeader()).thenReturn("abc");

        assertThrows(NumberFormatException.class, () -> rechargeService.addRecharge(requestDTO));
        verify(rechargeRepository, never()).save(any());
    }

    @Test
    @DisplayName("addRechargeFallback: should throw RuntimeException when all retries exhausted")
    void addRechargeFallback_ShouldThrowRuntimeException() {
        Exception cause = new RuntimeException("OperatorPlanManagement is down");

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> rechargeService.addRechargeFallback(requestDTO, cause));

        assertTrue(thrown.getMessage().contains("Recharge creation failed after retries"));
    }

    @Test
    @DisplayName("getRechargeById: should return recharge when found by ID")
    void getRechargeById_ShouldReturnRecharge_WhenFound() {
        when(rechargeRepository.findById(1L)).thenReturn(Optional.of(recharge));
        when(rechargeMapper.toRechargeResponseDTO(recharge)).thenReturn(responseDTO);

        RechargeResponseDTO result = rechargeService.getRechargeById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getRechargeId());
    }

    @Test
    @DisplayName("getRechargeById: should throw RechargeNotFoundException when ID not found")
    void getRechargeById_ShouldThrowException_WhenNotFound() {
        when(rechargeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RechargeNotFoundException.class,
                () -> rechargeService.getRechargeById(99L));
    }

    @Test
    @DisplayName("getAllRechargs: should return paginated list of recharges")
    void getAllRechargs_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Recharge> page = new PageImpl<>(List.of(recharge));

        when(rechargeRepository.findAll(pageable)).thenReturn(page);
        when(rechargeMapper.toRechargeResponseDTO(recharge)).thenReturn(responseDTO);

        Page<RechargeResponseDTO> result = rechargeService.getAllRechargs(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("deleteRecharge: should delete recharge and return success message")
    void deleteRecharge_ShouldDeleteAndReturnMessage() {
        when(rechargeRepository.findById(1L)).thenReturn(Optional.of(recharge));

        String result = rechargeService.deleteRecharge(1L);

        assertEquals("Recharge deleted!!", result);
        verify(rechargeRepository).delete(recharge);
    }

    @Test
    @DisplayName("deleteRecharge: should throw RechargeNotFoundException when ID not found")
    void deleteRecharge_ShouldThrowException_WhenNotFound() {
        when(rechargeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RechargeNotFoundException.class,
                () -> rechargeService.deleteRecharge(99L));
        verify(rechargeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("findAllRechargeByUserId: should return all recharges for a user")
    void findAllRechargeByUserId_ShouldReturnList() {
        when(rechargeRepository.findByUserId(10L)).thenReturn(List.of(recharge));
        when(rechargeMapper.toRechargeResponseDTO(recharge)).thenReturn(responseDTO);

        List<RechargeResponseDTO> result = rechargeService.findAllRechargeByUserId(10L);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findAllRechargeByPlanId: should return all recharges for a plan")
    void findAllRechargeByPlanId_ShouldReturnList() {
        when(rechargeRepository.findByPlanId(5L)).thenReturn(List.of(recharge));
        when(rechargeMapper.toRechargeResponseDTO(recharge)).thenReturn(responseDTO);

        List<RechargeResponseDTO> result = rechargeService.findAllRechargeByPlanId(5L);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getCurrentUserRecharges: should return recharges for the logged-in user")
    void getCurrentUserRecharges_ShouldReturnPage() {
        mockLoggedInUserId();
        Page<Recharge> rechargePage = new PageImpl<>(List.of(recharge));
        when(rechargeRepository.findByUserId(10L, PageRequest.of(0, 10))).thenReturn(rechargePage);
        when(rechargeMapper.toRechargeResponseDTO(recharge)).thenReturn(responseDTO);

        Page<RechargeResponseDTO> result = rechargeService.getCurrentUserRecharges(PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        verify(requestUserContext).getUserIdHeader();
    }

    @Test
    @DisplayName("updateRechargeStatus: should update status on entity and save")
    void updateRechargeStatus_ShouldUpdateAndSave_WhenFound() {
        when(rechargeRepository.findById(1L)).thenReturn(Optional.of(recharge));

        rechargeService.updateRechargeStatus(1L, RechargeStatus.SUCCESS);

        assertEquals(RechargeStatus.SUCCESS, recharge.getStatus());
        verify(rechargeRepository).save(recharge);
    }

    @Test
    @DisplayName("updateRechargeStatus: should throw RechargeNotFoundException when not found")
    void updateRechargeStatus_ShouldThrowException_WhenNotFound() {
        when(rechargeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RechargeNotFoundException.class,
                () -> rechargeService.updateRechargeStatus(99L, RechargeStatus.SUCCESS));
        verify(rechargeRepository, never()).save(any());
    }

    private void mockLoggedInUserId() {
        when(requestUserContext.getUserIdHeader()).thenReturn("10");
    }
}
