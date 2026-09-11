package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GardenResponse {
    private Integer streak;
    private Integer bestStreak;
    private Integer growthStage;
    private Integer freezes;
    private List<LocalDate> freezeUsedDates;
    private LocalDate lastPerfectDate;
}