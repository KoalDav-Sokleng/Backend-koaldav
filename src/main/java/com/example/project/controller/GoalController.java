package com.example.project.controller;

import com.example.project.dto.request.GoalRequest;
import com.example.project.dto.response.GoalResponse;
import com.example.project.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals/projects")
public class GoalController {
    private final GoalService service;

    public GoalController(GoalService service){
        this.service = service;
    }

    @PostMapping
    public GoalResponse createGoal(@Valid @RequestBody GoalRequest request){
        return service.createGoal(request);
    }

    @GetMapping("/{id}")
    public GoalResponse getGoal(@PathVariable Long id){
        return service.getGoal(id);
    }

    @GetMapping
    public List<GoalResponse> getAllGoals() {
        return service.getAllGoals();
    }

    @PutMapping("/{id}")
    public GoalResponse updateGoal(@PathVariable Long id, @Valid @RequestBody GoalRequest request) {
        return service.updateGoal(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void deleteGoal(@PathVariable Long id) {
        service.deleteGoal(id);
    }
}