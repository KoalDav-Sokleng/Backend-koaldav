package com.example.project.controller;

import com.example.project.dto.response.DashboardResponse;
import com.example.project.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
@Tag(name = "Dashboard", description = "Unified endpoint aggregating Goals, Habits, Garden, Savings, and Financial Overview")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "Get aggregated dashboard summary data")
    public ResponseEntity<DashboardResponse> getDashboardSummary(
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        DashboardResponse summary = dashboardService.getDashboardSummary(userId);
        return ResponseEntity.ok(summary);
    }
}
