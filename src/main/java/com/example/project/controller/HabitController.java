package com.example.project.controller;

import com.example.project.dto.request.HabitRequest;
import com.example.project.dto.request.ToggleRequest;
import com.example.project.dto.response.HabitResponse;
import com.example.project.dto.response.ToggleResponse;
import com.example.project.service.HabitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
@CrossOrigin(origins = "*")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @GetMapping
    public List<HabitResponse> getHabits() {
        return habitService.getHabits();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HabitResponse createHabit(@Valid @RequestBody HabitRequest request) {
        return habitService.createHabit(request);
    }

    @PutMapping("/{id}")
    public HabitResponse updateHabit(@PathVariable Long id, @Valid @RequestBody HabitRequest request) {
        return habitService.updateHabit(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<java.util.Map<String, Object>> deleteHabit(@PathVariable Long id) {
        boolean deleted = habitService.deleteHabit(id);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("status", "success");
        response.put("id", id);
        response.put("message", deleted ? "Habit deleted successfully" : "Habit was already deleted or not found");
        return ResponseEntity.ok(response);
    }

    // ToggleRequest's date field is accepted but intentionally unused —
    // see HabitService.toggleHabit for why the server clock wins.
    @PostMapping("/{id}/toggle")
    public ToggleResponse toggle(@PathVariable Long id, @RequestBody(required = false) ToggleRequest request) {
        return habitService.toggleHabit(id);
    }
}