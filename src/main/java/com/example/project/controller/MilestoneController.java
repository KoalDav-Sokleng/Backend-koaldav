package com.example.project.controller;

import com.example.project.dto.request.MilestoneRequest;
import com.example.project.dto.response.MilestoneResponse;
import com.example.project.service.MilestoneService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals/{goalId}/milestones")
@CrossOrigin(origins = "*")
public class MilestoneController {
    private final MilestoneService service;

    public MilestoneController(MilestoneService service) {
        this.service = service;
    }

    @PostMapping
    public MilestoneResponse create(
            @PathVariable Long goalId,
            @Valid @RequestBody MilestoneRequest request) {
        return service.create(goalId, request);
    }

    @GetMapping
    public List<MilestoneResponse> getMilestones(@PathVariable Long goalId) {
        return service.getMilestonesByGoalId(goalId);
    }

    @GetMapping("/{milestoneId}")
    public MilestoneResponse getMilestone(
            @PathVariable Long goalId,
            @PathVariable Long milestoneId) {
        return service.getMilestone(goalId, milestoneId);
    }

    @PutMapping("/{milestoneId}")
    public MilestoneResponse update(
            @PathVariable Long goalId,
            @PathVariable Long milestoneId,
            @Valid @RequestBody MilestoneRequest request) {
        return service.updateMilestone(goalId, milestoneId, request);
    }

    @DeleteMapping("/{milestoneId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long goalId,
            @PathVariable Long milestoneId) {
        service.deleteMilestone(goalId, milestoneId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{milestoneId}/complete")
    public MilestoneResponse complete(
            @PathVariable Long goalId,
            @PathVariable Long milestoneId) {
        return service.completeMilestone(goalId, milestoneId);
    }
}
