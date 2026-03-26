package com.oprationPlanManagement.operatorPlanService.service;

import java.util.List;
import java.util.stream.Collectors;

import com.oprationPlanManagement.operatorPlanService.mapper.Mapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.OperatorRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.OperatorResponseDTO;
import com.oprationPlanManagement.operatorPlanService.entity.OperatorEntity;
import com.oprationPlanManagement.operatorPlanService.repository.IOperatorRepository;
import com.oprationPlanManagement.operatorPlanService.repository.IPlanRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperatorServiceImpl implements IOperatorService {

    private final IOperatorRepository operatorRepo;
    private final Mapper mapper;
    
	@Override
    public OperatorResponseDTO saveOper(OperatorRequestDTO dto) {
        log.info("Saving new operator with name: {}", dto.getName());
        OperatorEntity entity = mapper.dtoToOperatorEntity(dto);
        OperatorEntity saved = operatorRepo.save(entity);
        log.info("Operator saved successfully with ID: {}, Name: {}", saved.getId(), saved.getName());
        return mapToResponse(saved);
    }

    @Override
    @Cacheable(value="operator", key = "#id")
    public OperatorResponseDTO getOper(long id) {
        log.info("Fetching operator with ID: {}", id);
        OperatorEntity entity = operatorRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Operator not found with id: {}", id);
                    return new RuntimeException("Operator not found with id: " + id);
                });
        log.info("Operator retrieved successfully with ID: {}, Name: {}", entity.getId(), entity.getName());
        return mapToResponse(entity);
    }

    @Override
    @Cacheable(value = "operatorList")
    public List<OperatorResponseDTO> getOperList() {
        log.info("Fetching all operators from database");
        List<OperatorResponseDTO> operators = operatorRepo.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.info("Successfully retrieved {} operators", operators.size());
        return operators;
    }

    @Override
    @Cacheable(value="operator", key = "#id")
    @CacheEvict(value = "operatorList", allEntries = true)
    public OperatorResponseDTO updateResponse(long id, OperatorRequestDTO dto) {
        log.info("Updating operator with ID: {} - New name: {}", id, dto.getName());
        OperatorEntity entity = operatorRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Operator not found with id: {} during update", id);
                    return new RuntimeException("Operator not found with id: " + id);
                });
        entity.setName(dto.getName());
        OperatorEntity updated = operatorRepo.save(entity);
        log.info("Operator updated successfully with ID: {}, New name: {}", id, updated.getName());
        return mapToResponse(updated);
    }

    @Override
    @Caching(evict = {
    	    @CacheEvict(value = "operator", key = "#id"),
    	    @CacheEvict(value = "operatorList", allEntries = true)
    	})
    public void deleteOper(long id) {
        log.info("Deleting operator with ID: {}", id);
        if (!operatorRepo.existsById(id)) {
            log.error("Operator not found with id: {} during deletion", id);
            throw new RuntimeException("Operator not found with id: " + id);
        }
        operatorRepo.deleteById(id);
        log.info("Operator deleted successfully with ID: {}", id);
    }

    private OperatorResponseDTO mapToResponse(OperatorEntity entity) {
        return new OperatorResponseDTO(entity.getName());
    }
}