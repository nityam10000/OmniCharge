
package com.oprationPlanManagement.operatorPlanService.service;
import java.util.List;
import java.util.stream.Collectors;
import com.oprationPlanManagement.operatorPlanService.exception.ResourceNotFoundException;
import com.oprationPlanManagement.operatorPlanService.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.PlanRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.PlanResponseDTO;
import com.oprationPlanManagement.operatorPlanService.entity.PlanEntity;
import com.oprationPlanManagement.operatorPlanService.repository.IPlanRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanServiceImpl implements IPlanService {

    private final IPlanRepository planRepo;
    private final Mapper mapper;

	@Override
    public PlanResponseDTO addNewPlan(PlanRequestDTO dto) {
        log.info("Adding new plan with details - Operator ID: {}, Amount: {}, Validity: {}", 
                 dto.getOperatorId(), dto.getAmount(), dto.getValidity());
        PlanEntity entity = mapper.dtoToPlanEntity(dto);
        PlanEntity saved = planRepo.save(entity);
        log.info("Plan added successfully with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Cacheable(value = "planList")
    public List<PlanResponseDTO> getPlanList() {
        log.info("Fetching all plans from database");
        List<PlanResponseDTO> plans = planRepo.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.info("Successfully retrieved {} plans", plans.size());
        return plans;
    }

    @Override
    public Page<PlanResponseDTO> getPlanList(Pageable pageable) {
        log.info("Fetching plans with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<PlanEntity> planPage = planRepo.findAll(pageable);
        Page<PlanResponseDTO> responsePage = planPage.map(this::mapToResponse);
        log.info("Successfully retrieved {} plans", responsePage.getNumberOfElements());
        return responsePage;
    }

    @Override
    @Cacheable(value = "planListByOperator", key = "#operatorId")
    public List<PlanResponseDTO> getPlansByOperatorId(Long operatorId) {
        log.info("Fetching plans for operatorId={}", operatorId);
        List<PlanResponseDTO> plans = planRepo.findByOperatorId(operatorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.info("Successfully retrieved {} plans for operatorId={}", plans.size(), operatorId);
        return plans;
    }

    @Override
    @Cacheable(value = "plan", key = "#id")
    public PlanResponseDTO getPlan(long id) {
        log.info("Fetching plan with ID: {}", id);
        PlanEntity entity = planRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Plan not found with id: {}", id);
                    return new ResourceNotFoundException("Plan not found with id: " + id);
                });
        log.info("Plan retrieved successfully with ID: {}", id);
        return mapToResponse(entity);
    }

    @Override
    @Caching(
    	    put = {
    	        @CachePut(value = "plan", key = "#id")
    	    },
    	    evict = {
    	        @CacheEvict(value = "planList", allEntries = true),
                @CacheEvict(value = "planListByOperator", allEntries = true)
    	    }
    	)

    public PlanResponseDTO updatePlan(long id, PlanRequestDTO dto) {
        log.info("Updating plan with ID: {} - New details: Operator ID: {}, Amount: {}, Validity: {}", 
                 id, dto.getOperatorId(), dto.getAmount(), dto.getValidity());
        PlanEntity entity = planRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Plan not found with id: {} during update", id);
                    return new ResourceNotFoundException("Plan not found with id: " + id);
                });
        entity.setAmount(dto.getAmount());
        entity.setValidity(dto.getValidity());
        entity.setDescription(dto.getDescription());
        entity.setOperatorId(dto.getOperatorId());
        PlanEntity updated = planRepo.save(entity);
        log.info("Plan updated successfully with ID: {}", id);
        return mapToResponse(updated);
    }

    @Override
    @Caching(evict = {
    	    @CacheEvict(value = "plan", key = "#id"),
    	    @CacheEvict(value = "planList", allEntries = true),
            @CacheEvict(value = "planListByOperator", allEntries = true)
    	})
    public void deletePlan(long id, PlanRequestDTO dto) {
        log.info("Deleting plan with ID: {}", id);
        if (!planRepo.existsById(id)) {
            log.error("Plan not found with id: {} during deletion", id);
            throw new ResourceNotFoundException("Plan not found with id: " + id);
        }
        planRepo.deleteById(id);
        log.info("Plan deleted successfully with ID: {}", id);
    }

    private PlanResponseDTO mapToResponse(PlanEntity entity) {
        return mapper.planToDTO(entity);
    }
}
