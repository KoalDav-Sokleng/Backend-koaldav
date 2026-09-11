package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Field names match HabitPage.jsx / habitApi.js exactly — no
 * frontend-side renaming needed. Constructed positionally by
 * HabitMapper, matching GoalResponse/MilestoneResponse convention.
 *
 * reminder was dropped (unused). startDate is included here even
 * though it's never accepted in the request — it's set server-side
 * at creation, same pattern as FocusSession.focusedDate.
 */
@Getter
@Setter
@AllArgsConstructor
public class HabitResponse {
    private Long id;
    private String title;
    private String target;
    private String type;
    private Integer streak;
    private Boolean completed;
    private LocalDate startDate;
}
