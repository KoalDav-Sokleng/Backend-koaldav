package com.example.project.mapper;

import com.example.project.Entity.Garden;
import com.example.project.dto.response.GardenResponse;
import org.springframework.stereotype.Component;

@Component
public class GardenMapper {
    public GardenResponse toResponse(Garden garden) {
        return new GardenResponse(
            garden.getStreak(),
            garden.getBestStreak(),
            garden.getGrowthStage(),
            garden.getFreezes(),
            garden.getFreezeUsedDates(),
            garden.getLastPerfectDate()
        );
    }
}