package com.example.project.service;

import com.example.project.Entity.Garden;
import com.example.project.dto.response.GardenResponse;
import com.example.project.mapper.GardenMapper;
import com.example.project.repository.GardenRepository;
import com.example.project.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GardenServiceTest {

    @Mock
    private GardenRepository gardenRepository;

    @Mock
    private HabitRepository habitRepository;

    @Spy
    private GardenMapper gardenMapper = new GardenMapper();

    @InjectMocks
    private GardenService gardenService;

    private Garden garden;

    @BeforeEach
    void setUp() {
        garden = new Garden();
        garden.setId(1L);
        garden.setGrowthStage(6);
        garden.setStreak(6);
        garden.setBestStreak(6);
        garden.setFreezes(2);
        garden.setFreezeUsedDates(new ArrayList<>());
    }

    @Test
    void settleDecay_WhenMissedMultipleDays_ShouldDecayStepByStep() {
        LocalDate today = LocalDate.now();
        // Checked 3 days ago, last perfect day was 3 days ago
        LocalDate threeDaysAgo = today.minusDays(3);
        garden.setLastCheckedDate(threeDaysAgo);
        garden.setLastPerfectDate(threeDaysAgo);

        when(gardenRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(garden));
        when(gardenRepository.save(any(Garden.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GardenResponse response = gardenService.getGarden();

        assertNotNull(response);
        // 3 days elapsed (2 missed days: 2 days ago and 1 day ago) -> 6 - 2 = 4
        assertEquals(4, response.getGrowthStage());
        assertEquals(0, response.getStreak());
    }

    @Test
    void settleDecay_WhenFreezeUsed_ShouldProtectThatDay() {
        LocalDate today = LocalDate.now();
        LocalDate twoDaysAgo = today.minusDays(2);
        LocalDate yesterday = today.minusDays(1);

        garden.setLastCheckedDate(twoDaysAgo);
        garden.setLastPerfectDate(twoDaysAgo);
        // Yesterday was protected by freeze
        garden.getFreezeUsedDates().add(yesterday);

        when(gardenRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(garden));
        when(gardenRepository.save(any(Garden.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GardenResponse response = gardenService.getGarden();

        assertNotNull(response);
        // Yesterday was frozen, so no decay!
        assertEquals(6, response.getGrowthStage());
    }
}
