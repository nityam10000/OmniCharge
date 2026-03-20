package com.oprationPlanManagement.operatorPlanService.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.PlanRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.PlanResponseDTO;
import com.oprationPlanManagement.operatorPlanService.mapper.Mapper;
import com.oprationPlanManagement.operatorPlanService.repository.IPlanRepository;
import com.oprationPlanManagement.operatorPlanService.service.IPlanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
public class PlanController {

    private final IPlanService planService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<PlanResponseDTO> createPlan(@Valid @RequestBody PlanRequestDTO dto) {
        return ResponseEntity.ok(planService.addNewPlan(dto));
    }


    @GetMapping
    public ResponseEntity<List<PlanResponseDTO>> getAllPlans() {
        return ResponseEntity.ok(planService.getPlanList());
    }

    // Get plan by ID
    @GetMapping("/{id}")
    public ResponseEntity<PlanResponseDTO> getPlan(@PathVariable long id) {
        return ResponseEntity.ok(planService.getPlan(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<PlanResponseDTO> updatePlan(@Valid @PathVariable long id,
                                                      @RequestBody PlanRequestDTO dto) {
        return ResponseEntity.ok(planService.updatePlan(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable long id,
                                           @RequestBody(required = false) PlanRequestDTO dto) {
        planService.deletePlan(id, dto);
        return ResponseEntity.noContent().build();
    }
}