package com.example.project.service;

import com.example.project.Entity.Garden;
import com.example.project.Entity.Habit;
import com.example.project.dto.response.GardenResponse;
import com.example.project.mapper.GardenMapper;
import com.example.project.repository.GardenRepository;
import com.example.project.repository.HabitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * All streak / growth-stage / freeze math lives here. The frontend
 * only ever displays what this service returns — never computes it
 * itself — which is what makes it tamper-proof against a user
 * editing devtools state or spoofing their device clock.
 */
@Service
public class GardenService {

    private final GardenRepository gardenRepository;
    private final HabitRepository habitRepository;
    private final GardenMapper gardenMapper;

    public GardenService(GardenRepository gardenRepository, HabitRepository habitRepository,
            GardenMapper gardenMapper) {
        this.gardenRepository = gardenRepository;
        this.habitRepository = habitRepository;
        this.gardenMapper = gardenMapper;
    }

    public Garden getOrCreate() {
        return gardenRepository.findTopByOrderByIdAsc()
                .orElseGet(() -> gardenRepository.save(new Garden()));
    }

    public GardenResponse getGarden() {
        LocalDate today = LocalDate.now();
        Garden garden = getOrCreate();
        settleDecay(garden, today);
        garden.setLastCheckedDate(today);
        garden = gardenRepository.save(garden);
        return gardenMapper.toResponse(garden);
    }

    public GardenResponse useFreeze() {
        LocalDate today = LocalDate.now();
        Garden garden = getOrCreate();
        settleDecay(garden, today);

        boolean frozenToday = garden.getFreezeUsedDates().contains(today);
        boolean alreadyPerfectToday = today.equals(garden.getLastPerfectDate());

        if (garden.getFreezes() > 0 && !frozenToday && !alreadyPerfectToday) {
            garden.setFreezes(garden.getFreezes() - 1);
            garden.getFreezeUsedDates().add(today);
        }
        garden.setLastCheckedDate(today);
        garden = gardenRepository.save(garden);
        return gardenMapper.toResponse(garden);
    }

    /**
     * Called by HabitService right after a habit toggle. If every
     * habit is now completed for today (and today wasn't already
     * counted), advances the streak, grows the flower one stage
     * (capped at 7), updates the best streak, and banks 2 freezes
     * on every 7-day milestone.
     */
    public boolean registerPerfectDayIfNeeded(boolean allHabitsCompletedToday) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Garden garden = getOrCreate();
        settleDecay(garden, today);

        boolean justReached = false;
        if (allHabitsCompletedToday && !today.equals(garden.getLastPerfectDate())) {
            boolean continuing = yesterday.equals(garden.getLastPerfectDate())
                    || garden.getFreezeUsedDates().contains(yesterday);
            int newStreak = continuing ? garden.getStreak() + 1 : 1;

            garden.setStreak(newStreak);
            garden.setGrowthStage(Math.min(7, garden.getGrowthStage() + 1));
            garden.setBestStreak(Math.max(garden.getBestStreak(), newStreak));
            if (newStreak % 7 == 0) {
                garden.setFreezes(garden.getFreezes() + 2);
            }
            garden.setLastPerfectDate(today);
            justReached = true;
        } else if (!allHabitsCompletedToday && today.equals(garden.getLastPerfectDate())) {
            // User unchecked a habit today: revert today's completion mark
            int prevStreak = Math.max(0, garden.getStreak() - 1);
            garden.setStreak(prevStreak);
            garden.setLastPerfectDate(prevStreak > 0 ? yesterday : null);
            garden.setGrowthStage(Math.max(0, garden.getGrowthStage() - 1));
        }
        garden.setLastCheckedDate(today);
        gardenRepository.save(garden);
        return justReached;
    }

    public GardenResponse grantFreezes(int count) {
        Garden garden = getOrCreate();
        garden.setFreezes(Math.max(0, count));
        garden = gardenRepository.save(garden);
        return gardenMapper.toResponse(garden);
    }

    public GardenResponse resetToday() {
        LocalDate today = LocalDate.now();
        Garden garden = getOrCreate();
        if (today.equals(garden.getLastPerfectDate())) {
            garden.setLastPerfectDate(null);
        }
        garden.getFreezeUsedDates().remove(today);
        garden = gardenRepository.save(garden);
        return gardenMapper.toResponse(garden);
    }

    /**
     * Dev helper: sets simulated streak/growthStage/freezes and sets
     * lastPerfectDate to YESTERDAY so checking off habits TODAY naturally
     * and realistically advances to (streak + 1) and triggers Day 7 milestone
     * rewards!
     */
    public GardenResponse setDevState(Integer streak, Integer bestStreak, Integer growthStage, Integer freezes) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Garden garden = getOrCreate();
        if (streak != null) {
            int s = Math.max(0, streak);
            garden.setStreak(s);
            if (s > 0) {
                garden.setLastPerfectDate(yesterday);
                garden.setLastCheckedDate(yesterday);
            } else {
                garden.setLastPerfectDate(null);
                garden.setLastCheckedDate(today);
            }
        }

        int targetBest = bestStreak != null ? Math.max(0, bestStreak) : garden.getBestStreak();
        garden.setBestStreak(Math.max(targetBest, garden.getStreak()));

        if (growthStage != null)
            garden.setGrowthStage(Math.max(0, Math.min(7, growthStage)));
        if (freezes != null)
            garden.setFreezes(Math.max(0, freezes));
        garden.getFreezeUsedDates().remove(today);
        garden = gardenRepository.save(garden);

        // Also sync all habits to have yesterday as completed date and ready for
        // today's check
        if (streak != null) {
            List<Habit> habits = habitRepository.findAll();
            for (Habit h : habits) {
                if (streak > 0) {
                    h.setStreak(streak);
                    h.setLastCompletedDate(yesterday);
                } else {
                    h.setStreak(0);
                    h.setLastCompletedDate(null);
                }
            }
            habitRepository.saveAll(habits);
        }

        return gardenMapper.toResponse(garden);
    }

    public GardenResponse currentState() {
        return gardenMapper.toResponse(getOrCreate());
    }

    /**
     * Runs at most once per calendar day (gated by lastCheckedDate).
     * If yesterday was never covered — not fully completed, and no
     * freeze protected it — the flower wilts one stage and the
     * streak resets to 0.
     */
    private void settleDecay(Garden garden, LocalDate today) {
        if (today.equals(garden.getLastCheckedDate()))
            return;

        LocalDate yesterday = today.minusDays(1);
        boolean freshStart = garden.getLastCheckedDate() == null;
        boolean yesterdayCovered = yesterday.equals(garden.getLastPerfectDate())
                || garden.getFreezeUsedDates().contains(yesterday);
        boolean alreadyPerfectToday = today.equals(garden.getLastPerfectDate());

        if (!freshStart && !yesterdayCovered && !alreadyPerfectToday) {
            garden.setGrowthStage(Math.max(0, garden.getGrowthStage() - 1));
            garden.setStreak(0);
        }
    }
}