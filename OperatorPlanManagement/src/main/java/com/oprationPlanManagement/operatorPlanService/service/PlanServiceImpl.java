package com.oprationPlanManagement.operatorPlanService.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.PlanRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.PlanResponseDTO;
import com.oprationPlanManagement.operatorPlanService.entity.PlanEntity;
import com.oprationPlanManagement.operatorPlanService.repository.IPlanRepository;

@Service
public class PlanServiceImpl implements IPlanService {

    private final IPlanRepository planRepo;

    public PlanServiceImpl(IPlanRepository planRepo) {
        this.planRepo = planRepo;
    }

    @Override
    public PlanResponseDTO addNewPlan(PlanRequestDTO dto) {
        PlanEntity entity = new PlanEntity(dto.getAmount(), dto.getValidity(), dto.getDescription(), dto.getOperatorId());
        PlanEntity saved = planRepo.save(entity);
        return mapToResponse(saved);
    }

    @Override
    public List<PlanResponseDTO> getPlanList() {
        return planRepo.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PlanResponseDTO getPlan(long id) {
        PlanEntity entity = planRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found with id: " + id));
        return mapToResponse(entity);
    }

    @Override
    public PlanResponseDTO updatePlan(long id, PlanRequestDTO dto) {
        PlanEntity entity = planRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found with id: " + id));
        entity.setAmount(dto.getAmount());
        entity.setValidity(dto.getValidity());
        entity.setDescription(dto.getDescription());
        entity.setOperatorId(dto.getOperatorId());
        PlanEntity updated = planRepo.save(entity);
        return mapToResponse(updated);
    }

    @Override
    public void deletePlan(long id, PlanRequestDTO dto) {
        if (!planRepo.existsById(id)) {
            throw new RuntimeException("Plan not found with id: " + id);
        }
        planRepo.deleteById(id);
    }

    private PlanResponseDTO mapToResponse(PlanEntity entity) {
        return new PlanResponseDTO(entity.getAmount(), entity.getValidity(), entity.getDescription());
    }
}