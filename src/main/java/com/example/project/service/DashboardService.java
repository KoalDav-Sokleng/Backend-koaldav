package com.example.project.service;

import com.example.project.Enum.SavingGoalStatus;
import com.example.project.dto.response.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final GardenService gardenService;
    private final HabitService habitService;
    private final GoalService goalService;
    private final SavingGoalService savingGoalService;
    private final ExpenseService expenseService;
    private final NotificationService notificationService;

    public DashboardService(
            GardenService gardenService,
            HabitService habitService,
            GoalService goalService,
            SavingGoalService savingGoalService,
            ExpenseService expenseService,
            NotificationService notificationService) {
        this.gardenService = gardenService;
        this.habitService = habitService;
        this.goalService = goalService;
        this.savingGoalService = savingGoalService;
        this.expenseService = expenseService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardSummary(Long userId) {
        Long targetUserId = (userId != null && userId > 0) ? userId : 1L;

        // 1. Garden & Habits
        GardenResponse garden = gardenService.getGarden();
        List<HabitResponse> habits = habitService.getHabits();

        // 2. Goals & Milestones
        List<GoalResponse> allGoals = goalService.getAllGoals();
        List<GoalResponse> activeGoals = allGoals.stream()
                .filter(g -> "IN_PROGRESS".equalsIgnoreCase(g.getStatus()))
                .collect(Collectors.toList());
        int totalGoalsCount = allGoals.size();
        int completedGoalsCount = (int) allGoals.stream()
                .filter(g -> "COMPLETED".equalsIgnoreCase(g.getStatus()))
                .count();

        // 3. Saving Goals
        List<SavingGoalResponse> allSavings = savingGoalService.getAllGoals();
        List<SavingGoalResponse> activeSavingGoals = allSavings.stream()
                .filter(s -> s.getStatus() == SavingGoalStatus.ACTIVE)
                .collect(Collectors.toList());

        BigDecimal totalSaved = BigDecimal.ZERO;
        BigDecimal totalTarget = BigDecimal.ZERO;
        for (SavingGoalResponse s : allSavings) {
            if (s.getCurrentAmount() != null) {
                totalSaved = totalSaved.add(s.getCurrentAmount());
            }
            if (s.getTargetAmount() != null) {
                totalTarget = totalTarget.add(s.getTargetAmount());
            }
        }

        // 4. Expenses & Finance Analytics
        FinanceOverviewResponse financeOverview = expenseService.getOverview(LocalDate.now().getYear());

        // 5. Notifications
        List<NotificationResponse> unreadNotifications = notificationService.getUnreadNotifications(targetUserId);

        return DashboardResponse.builder()
                .garden(garden)
                .habits(habits)
                .activeGoals(activeGoals)
                .totalGoalsCount(totalGoalsCount)
                .completedGoalsCount(completedGoalsCount)
                .activeSavingGoals(activeSavingGoals)
                .totalSavedAmount(totalSaved)
                .totalTargetSavings(totalTarget)
                .financeOverview(financeOverview)
                .unreadNotifications(unreadNotifications)
                .unreadNotificationsCount(unreadNotifications.size())
                .build();
    }
}
