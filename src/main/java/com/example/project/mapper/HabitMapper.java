package com.example.project.mapper;

import com.example.project.Entity.Habit;
import com.example.project.dto.request.HabitRequest;
import com.example.project.dto.response.HabitResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class HabitMapper {

    /**
     * streak defaults to 0, lastCompletedDate to null, and startDate
     * to today — same spirit as GoalMapper defaulting status to
     * IN_PROGRESS, and the same pattern FocusSessionService uses for
     * focusedDate: set by the server at creation time, never accepted
     * from the client.
     */
    public Habit toEntity(HabitRequest request) {
        Habit habit = new Habit();
        habit.setTitle(request.getTitle());
        habit.setTarget(
            request.getTarget() != null && !request.getTarget().isBlank()
                ? request.getTarget()
                : "Daily goal"
        );
        habit.setType(request.getType() != null ? request.getType() : "other");
        habit.setStreak(0);
        habit.setStartDate(LocalDate.now());
        return habit;
    }

    /**
     * Applies an update request onto an existing entity in place
     * (used by HabitService for PUT /api/habits/{id}) — does NOT
     * touch streak, lastCompletedDate, or startDate; those are either
     * toggle-driven or fixed at creation time, never editable.
     */
    public void applyUpdate(Habit habit, HabitRequest request) {
        habit.setTitle(request.getTitle());
        habit.setTarget(request.getTarget());
        habit.setType(request.getType());
    }

    public HabitResponse toResponse(Habit habit, LocalDate today) {
        return new HabitResponse(
            habit.getId(),
            habit.getTitle(),
            habit.getTarget(),
            habit.getType(),
            habit.getStreak(),
            habit.isCompletedOn(today),
            habit.getStartDate()
        );
    }
}
