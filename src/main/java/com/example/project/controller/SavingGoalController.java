package com.example.project.controller;

import com.example.project.Enum.SavingGoalStatus;
import com.example.project.dto.request.SavingDepositRequest;
import com.example.project.dto.request.SavingGoalRequest;
import com.example.project.dto.response.SavingDepositResponse;
import com.example.project.dto.response.SavingGoalResponse;
import com.example.project.service.SavingGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saving-goals")
@CrossOrigin(origins = "*")
public class SavingGoalController {

    private final SavingGoalService savingGoalService;

    public SavingGoalController(SavingGoalService savingGoalService) {
        this.savingGoalService = savingGoalService;
    }

    @PostMapping
    public ResponseEntity<SavingGoalResponse> createGoal(@RequestBody SavingGoalRequest request) {
        return new ResponseEntity<>(savingGoalService.createGoal(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SavingGoalResponse>> getAllGoals(
            @RequestParam(required = false) SavingGoalStatus status) {
        if (status != null) {
            return ResponseEntity.ok(savingGoalService.getGoalsByStatus(status));
        }
        return ResponseEntity.ok(savingGoalService.getAllGoals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingGoalResponse> getGoalById(@PathVariable Long id) {
        return ResponseEntity.ok(savingGoalService.getGoalById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingGoalResponse> updateGoal(@PathVariable Long id, @RequestBody SavingGoalRequest request) {
        return ResponseEntity.ok(savingGoalService.updateGoal(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        savingGoalService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deposits")
    public ResponseEntity<SavingDepositResponse> addDeposit(@PathVariable Long id, @Valid @RequestBody SavingDepositRequest request) {
        return new ResponseEntity<>(savingGoalService.addDeposit(id, request), HttpStatus.CREATED);
    }
}