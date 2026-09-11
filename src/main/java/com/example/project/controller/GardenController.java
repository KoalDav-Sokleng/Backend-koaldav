package com.example.project.controller;

import com.example.project.dto.request.FreezeRequest;
import com.example.project.dto.response.GardenResponse;
import com.example.project.service.GardenService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/garden")
@CrossOrigin(origins = "*")
public class GardenController {

    private final GardenService gardenService;

    public GardenController(GardenService gardenService) {
        this.gardenService = gardenService;
    }

    @GetMapping
    public GardenResponse getGarden() {
        return gardenService.getGarden();
    }

    @PostMapping("/freeze")
    public GardenResponse useFreeze(@RequestBody(required = false) FreezeRequest request) {
        return gardenService.useFreeze();
    }

    /* ---------------- Developer / Testing Endpoints ---------------- */

    @PostMapping("/dev/grant-freezes")
    public GardenResponse grantFreezes(@RequestParam(defaultValue = "3") int count) {
        return gardenService.grantFreezes(count);
    }

    @PostMapping("/dev/reset-today")
    public GardenResponse resetToday() {
        return gardenService.resetToday();
    }

    @PostMapping("/dev/set-state")
    public GardenResponse setDevState(
            @RequestParam(required = false) Integer streak,
            @RequestParam(required = false) Integer bestStreak,
            @RequestParam(required = false) Integer growthStage,
            @RequestParam(required = false) Integer freezes) {
        return gardenService.setDevState(streak, bestStreak, growthStage, freezes);
    }
}