package com.oprationPlanManagement.operatorPlanService.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
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
    // Create new plan
    @PostMapping("/create")
    public ResponseEntity<PlanResponseDTO> createPlan(@Valid @RequestBody PlanRequestDTO dto) {
        return ResponseEntity.ok(planService.addNewPlan(dto));
    }

    // Get all plans
    @GetMapping
    public ResponseEntity<List<PlanResponseDTO>> getAllPlans() {
        return ResponseEntity.ok(planService.getPlanList());
    }

    // Get plan by ID
    @GetMapping("/{id}")
    public ResponseEntity<PlanResponseDTO> getPlan(@PathVariable long id) {
        return ResponseEntity.ok(planService.getPlan(id));
    }

    // Update plan
    @PutMapping("/update/{id}")
    public ResponseEntity<PlanResponseDTO> updatePlan(@Valid @PathVariable long id,
                                                      @RequestBody PlanRequestDTO dto) {
        return ResponseEntity.ok(planService.updatePlan(id, dto));
    }

    // Delete plan
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable long id,
                                           @RequestBody(required = false) PlanRequestDTO dto) {
        planService.deletePlan(id, dto);
        return ResponseEntity.noContent().build();
    }
}