package com.example.project.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * The "date" field is accepted for API compatibility with the
 * frontend's { date } body, but HabitService deliberately ignores
 * it in favor of the server's own LocalDate.now() — see the comment
 * in HabitService.toggleHabit for why a client-supplied date can't
 * be trusted for streak calculations.
 */
@Data
@NoArgsConstructor
public class ToggleRequest {
    private LocalDate date;
}