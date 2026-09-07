package com.example.project.service;

import com.example.project.Entity.Habit;
import com.example.project.dto.exception.ResourceNotFoundException;
import com.example.project.dto.request.HabitRequest;
import com.example.project.dto.response.GardenResponse;
import com.example.project.dto.response.HabitResponse;
import com.example.project.dto.response.ToggleResponse;
import com.example.project.mapper.HabitMapper;
import com.example.project.repository.HabitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitMapper habitMapper;
    private final GardenService gardenService;

    public HabitService(HabitRepository habitRepository, HabitMapper habitMapper, GardenService gardenService) {
        this.habitRepository = habitRepository;
        this.habitMapper = habitMapper;
        this.gardenService = gardenService;
    }

    @Transactional(readOnly = true)
    public List<HabitResponse> getHabits() {
        LocalDate today = LocalDate.now();
        return habitRepository.findAll()
                .stream()
                .map(h -> habitMapper.toResponse(h, today))
                .toList();
    }

    @Transactional
    public HabitResponse createHabit(HabitRequest request) {
        Habit habit = habitMapper.toEntity(request);
        habit = habitRepository.save(habit);
        return habitMapper.toResponse(habit, LocalDate.now());
    }

    @Transactional
    public HabitResponse updateHabit(Long id, HabitRequest request) {
        Habit habit = findHabitOrThrow(id);
        habitMapper.applyUpdate(habit, request);
        habit = habitRepository.save(habit);
        return habitMapper.toResponse(habit, LocalDate.now());
    }

    @Transactional
    public boolean deleteHabit(Long id) {
        Optional<Habit> habitOpt = habitRepository.findById(id);
        if (habitOpt.isEmpty()) {
            return false;
        }

        habitRepository.delete(habitOpt.get());
        habitRepository.flush();

        LocalDate today = LocalDate.now();
        List<Habit> allHabits = habitRepository.findAll();
        boolean allCompletedToday = !allHabits.isEmpty()
                && allHabits.stream().allMatch(h -> h.isCompletedOn(today));
        gardenService.registerPerfectDayIfNeeded(allCompletedToday);
        return true;
    }

    /**
     * Marks a habit done/undone for today (the server's clock, not
     * whatever date the client sent — see ToggleRequest). Then asks
     * GardenService to check whether every habit is now completed
     * for today, so the streak/flower/freeze state and the
     * "justReachedPerfectDay" flag stay in sync with this same request.
     */
    @Transactional
    public ToggleResponse toggleHabit(Long id) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Habit habit = findHabitOrThrow(id);
        boolean wasCompletedToday = habit.isCompletedOn(today);
        boolean nextCompleted = !wasCompletedToday;

        if (nextCompleted) {
            boolean continuing = yesterday.equals(habit.getLastCompletedDate());
            habit.setStreak(continuing ? habit.getStreak() + 1 : 1);
            habit.setLastCompletedDate(today);
        } else {
            int prevStreak = Math.max(0, habit.getStreak() - 1);
            habit.setStreak(prevStreak);
            habit.setLastCompletedDate(prevStreak > 0 ? yesterday : null);
        }
        habit = habitRepository.save(habit);

        List<Habit> allHabits = habitRepository.findAll();
        boolean allCompletedToday = !allHabits.isEmpty()
                && allHabits.stream().allMatch(h -> h.isCompletedOn(today));

        boolean justReachedPerfectDay = gardenService.registerPerfectDayIfNeeded(allCompletedToday);
        GardenResponse gardenResponse = gardenService.currentState();

        return new ToggleResponse(
                habitMapper.toResponse(habit, today),
                gardenResponse,
                justReachedPerfectDay);
    }

    private Habit findHabitOrThrow(Long id) {
        return habitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found: " + id));
    }
}