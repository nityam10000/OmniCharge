package com.oprationPlanManagement.operatorPlanService.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.OperatorRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.OperatorResponseDTO;
import com.oprationPlanManagement.operatorPlanService.entity.OperatorEntity;
import com.oprationPlanManagement.operatorPlanService.repository.IOperatorRepository;

@Service
public class OperatorServiceImpl implements IOperatorService {

    private final IOperatorRepository operatorRepo;

    public OperatorServiceImpl(IOperatorRepository operatorRepo) {
        this.operatorRepo = operatorRepo;
    }

    @Override
    public OperatorResponseDTO saveOper(OperatorRequestDTO dto) {
        OperatorEntity entity = new OperatorEntity(dto.getName());
        OperatorEntity saved = operatorRepo.save(entity);
        return mapToResponse(saved);
    }

    @Override
    public OperatorResponseDTO getOper(long id) {
        OperatorEntity entity = operatorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Operator not found with id: " + id));
        return mapToResponse(entity);
    }

    @Override
    public List<OperatorResponseDTO> getOperList() {
        return operatorRepo.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OperatorResponseDTO updateResponse(long id, OperatorRequestDTO dto) {
        OperatorEntity entity = operatorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Operator not found with id: " + id));
        entity.setName(dto.getName());
        OperatorEntity updated = operatorRepo.save(entity);
        return mapToResponse(updated);
    }

    @Override
    public void deleteOper(long id) {
        if (!operatorRepo.existsById(id)) {
            throw new RuntimeException("Operator not found with id: " + id);
        }
        operatorRepo.deleteById(id);
    }

    private OperatorResponseDTO mapToResponse(OperatorEntity entity) {
        return new OperatorResponseDTO(entity.getName());
    }
}