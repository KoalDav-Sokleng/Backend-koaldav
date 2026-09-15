package com.example.project.controller;

import com.example.project.dto.request.FocusSessionRequest;
import com.example.project.dto.response.FocusSessionResponse;
import com.example.project.service.FocusSessionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/milestones/{milestoneId}/sessions")
public class FocusSessioncontroller {
    private final FocusSessionService service;

    public FocusSessioncontroller(FocusSessionService service) {
        this.service = service;
    }

    @PostMapping
    public FocusSessionResponse create(@PathVariable Long milestoneId, @Valid @RequestBody FocusSessionRequest request) {
        return service.create(milestoneId, request);
    }

    @GetMapping
    public java.util.List<FocusSessionResponse> getSessions(@PathVariable Long milestoneId) {
        return service.getSessionsByMilestoneId(milestoneId);
    }
}


