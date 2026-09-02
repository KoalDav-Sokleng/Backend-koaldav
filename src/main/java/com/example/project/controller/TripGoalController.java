package com.example.project.controller;

import com.example.project.dto.request.TripDepositRequest;
import com.example.project.dto.request.TripGoalRequest;
import com.example.project.dto.response.TripGoalResponse;
import com.example.project.service.TripGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RequiredArgsConstructor
public class TripGoalController {

    private final TripGoalService service;

    @GetMapping
    public ResponseEntity<List<TripGoalResponse>> getAllGoals() {
        return ResponseEntity.ok(service.getAllGoals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripGoalResponse> getGoalById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getGoalById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TripGoalResponse> createGoal(@ModelAttribute TripGoalRequest request) throws IOException {
        return new ResponseEntity<>(service.createGoal(request), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TripGoalResponse> updateGoal(
            @PathVariable Long id,
            @ModelAttribute TripGoalRequest request) throws IOException {
        return ResponseEntity.ok(service.updateGoal(id, request));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<TripGoalResponse> deposit(
            @PathVariable Long id,
            @RequestBody TripDepositRequest request) {
        return ResponseEntity.ok(service.deposit(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        service.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }
}
