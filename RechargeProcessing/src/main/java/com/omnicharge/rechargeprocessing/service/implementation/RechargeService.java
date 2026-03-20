package com.omnicharge.rechargeprocessing.service.implementation;


import com.omnicharge.rechargeprocessing.dto.RechargeRequestDTO;
import com.omnicharge.rechargeprocessing.dto.RechargeResponseDTO;
import com.omnicharge.rechargeprocessing.dto.UserResponseDTO;
import com.omnicharge.rechargeprocessing.entity.Recharge;
import com.omnicharge.rechargeprocessing.exception.RechargeNotFoundException;
import com.omnicharge.rechargeprocessing.mapper.RechargeMapper;
import com.omnicharge.rechargeprocessing.repository.IRechargeRepository;
import com.omnicharge.rechargeprocessing.service.IRechargeService;
import com.omnicharge.rechargeprocessing.service.IUserClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.mapper.Mapper;
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

@Service
@RequiredArgsConstructor
public class RechargeService implements IRechargeService {

    private final IRechargeRepository rechargeRepository;
    private final RechargeMapper rechargeMapper;



    @Override
    public RechargeResponseDTO addRecharge(RechargeRequestDTO rechargeRequestDTO) {
        Long userId = getLoggedInUserId();
        Recharge recharge = rechargeMapper.toRecharge(rechargeRequestDTO);
        recharge.setUserId(userId);
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


    private void validateAccess(Long userId) throws AccessDeniedException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();
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
    @Override
    public List<RechargeResponseDTO> getCurrentUserRecharges() {

        Long userId = getLoggedInUserId(); // 🔥 already exists

        List<Recharge> recharges = rechargeRepository.findByUserId(userId);

        return recharges.stream()
                .map(rechargeMapper::toRechargeResponseDTO)
                .toList();
    }
}


