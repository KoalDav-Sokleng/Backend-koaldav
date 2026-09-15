package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * What POST /api/habits/{id}/toggle returns. Bundles the updated
 * habit AND the updated garden together so the frontend never has
 * to make a second request to find out whether the whole list just
 * became 100% complete for the day.
 */
@Getter
@Setter
@AllArgsConstructor
public class ToggleResponse {
    private HabitResponse habit;
    private GardenResponse garden;
    private boolean justReachedPerfectDay;
}