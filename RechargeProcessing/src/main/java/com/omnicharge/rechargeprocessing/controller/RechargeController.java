package com.omnicharge.rechargeprocessing.controller;

import com.omnicharge.rechargeprocessing.dto.RechargeRequestDTO;
import com.omnicharge.rechargeprocessing.dto.RechargeResponseDTO;
import com.omnicharge.rechargeprocessing.service.IRechargeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recharge")
@RequiredArgsConstructor
public class RechargeController {

    private final IRechargeService rechargeService;

    @PostMapping("/add-recharge")
    public ResponseEntity<RechargeResponseDTO> addRecharge(
            @Valid @RequestBody RechargeRequestDTO rechargeRequestDTO) {
        RechargeResponseDTO dto = rechargeService.addRecharge(rechargeRequestDTO);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    //called internally by PaymentService via Feign to fetch planId
    @GetMapping("/{id}")
    public ResponseEntity<RechargeResponseDTO> getRechargeById(@PathVariable Long id) {
        RechargeResponseDTO dto = rechargeService.getRechargeById(id);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<RechargeResponseDTO>> getAllRecharges(Pageable pageable) {
        Page<RechargeResponseDTO> page = rechargeService.getAllRechargs(pageable);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete-recharge/{id}")
    public ResponseEntity<String> deleteRecharge(@PathVariable Long id) {
        String status = rechargeService.deleteRecharge(id);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RechargeResponseDTO>> getRechargeByUserId(@PathVariable Long userId) {
        List<RechargeResponseDTO> list = rechargeService.findAllRechargeByUserId(userId);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/myrecharges")
    public ResponseEntity<Page<RechargeResponseDTO>> getMyRecharges(Pageable pageable) {
        return ResponseEntity.ok(rechargeService.getCurrentUserRecharges(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("plan/{planId}")
    public ResponseEntity<List<RechargeResponseDTO>> getRechargeByPlanId(@PathVariable Long planId) {
        List<RechargeResponseDTO> list = rechargeService.findAllRechargeByPlanId(planId);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateRechargeStatus(
            @PathVariable Long id,
            @RequestParam("status") String status) {
        rechargeService.updateRechargeStatus(
                id,
                com.omnicharge.rechargeprocessing.enums.RechargeStatus.valueOf(status.toUpperCase())
        );
        return new ResponseEntity<>("Status updated successfully", HttpStatus.OK);
    }
}