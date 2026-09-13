package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    // 1. Gamified Garden & Streaks
    private GardenResponse garden;
    private List<HabitResponse> habits;

    // 2. Goals & Milestones
    private List<GoalResponse> activeGoals;
    private int totalGoalsCount;
    private int completedGoalsCount;

    // 3. Saving Goals
    private List<SavingGoalResponse> activeSavingGoals;
    private BigDecimal totalSavedAmount;
    private BigDecimal totalTargetSavings;

    // 4. Expenses & Finance Analytics
    private FinanceOverviewResponse financeOverview;

    // 5. Notifications
    private List<NotificationResponse> unreadNotifications;
    private int unreadNotificationsCount;
}
