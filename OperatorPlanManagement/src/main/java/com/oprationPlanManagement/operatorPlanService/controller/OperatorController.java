package com.oprationPlanManagement.operatorPlanService.controller;

import java.util.List;

<<<<<<< HEAD
=======
import lombok.RequiredArgsConstructor;
>>>>>>> origin/bhavik
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.oprationPlanManagement.operatorPlanService.dto.requestDTO.OperatorRequestDTO;
import com.oprationPlanManagement.operatorPlanService.dto.responseDTO.OperatorResponseDTO;
import com.oprationPlanManagement.operatorPlanService.service.IOperatorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/operators")
<<<<<<< HEAD
=======
@RequiredArgsConstructor
>>>>>>> origin/bhavik
public class OperatorController {

    private final IOperatorService operatorService;

<<<<<<< HEAD
    public OperatorController(IOperatorService operatorService) {
        this.operatorService = operatorService;
    }

    // Create
=======

>>>>>>> origin/bhavik
    @PostMapping("/register")
    public ResponseEntity<OperatorResponseDTO> createOperator(@Valid @RequestBody OperatorRequestDTO dto) {
        OperatorResponseDTO response = operatorService.saveOper(dto);
        return ResponseEntity.ok(response);
    }

<<<<<<< HEAD
    // Read single
=======
>>>>>>> origin/bhavik
    @GetMapping("/{id}")
    public ResponseEntity<OperatorResponseDTO> getOperator(@PathVariable long id) {
        OperatorResponseDTO response = operatorService.getOper(id);
        return ResponseEntity.ok(response);
    }

<<<<<<< HEAD
    // Read all
=======
>>>>>>> origin/bhavik
    @GetMapping("/getList")
    public ResponseEntity<List<OperatorResponseDTO>> getAllOperators() {
        List<OperatorResponseDTO> responseList = operatorService.getOperList();
        return ResponseEntity.ok(responseList);
    }

<<<<<<< HEAD
    // Update
=======
>>>>>>> origin/bhavik
    @PutMapping("/update/{id}")
    public ResponseEntity<OperatorResponseDTO> updateOperator(@Valid @PathVariable long id,
                                                              @RequestBody OperatorRequestDTO dto) {
        OperatorResponseDTO response = operatorService.updateResponse(id, dto);
        return ResponseEntity.ok(response);
    }

<<<<<<< HEAD
    // Delete
=======
>>>>>>> origin/bhavik
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteOperator(@PathVariable long id) {
        operatorService.deleteOper(id);
        return ResponseEntity.noContent().build();
    }
}