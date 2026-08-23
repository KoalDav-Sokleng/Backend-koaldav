package com.example.project.controller;

import com.example.project.dto.request.MilestoneRequest;
import com.example.project.dto.response.MilestoneResponse;
import com.example.project.service.MilestoneService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goals/projects/{goalId}/milestones")
public class MilestoneController {
    private final MilestoneService service;

    public MilestoneController(MilestoneService service) {
        this.service = service;
    }

    @PostMapping
    public MilestoneResponse create(
            @PathVariable Long goalId,
            @Valid @RequestBody MilestoneRequest request
    ){
        return service.create(goalId, request);
    }

    @GetMapping
    public java.util.List<MilestoneResponse> getMilestones(@PathVariable Long goalId) {
        return service.getMilestonesByGoalId(goalId);
    }

    @GetMapping("/{milestoneId}")
    public MilestoneResponse getMilestone(
            @PathVariable Long goalId,
            @PathVariable Long milestoneId) {
        return service.getMilestone(goalId, milestoneId);
    }

    @PatchMapping("/{milestoneId}/complete")
    public MilestoneResponse complete(
            @PathVariable Long goalId,
            @PathVariable Long milestoneId) {
        return service.completeMilestone(goalId, milestoneId);
    }
}


