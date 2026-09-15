package com.example.project.service;

import com.example.project.Enum.SavingGoalStatus;
import com.example.project.dto.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    private GardenService gardenService;
    private HabitService habitService;
    private GoalService goalService;
    private SavingGoalService savingGoalService;
    private ExpenseService expenseService;
    private NotificationService notificationService;
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        gardenService = Mockito.mock(GardenService.class);
        habitService = Mockito.mock(HabitService.class);
        goalService = Mockito.mock(GoalService.class);
        savingGoalService = Mockito.mock(SavingGoalService.class);
        expenseService = Mockito.mock(ExpenseService.class);
        notificationService = Mockito.mock(NotificationService.class);

        dashboardService = new DashboardService(
                gardenService,
                habitService,
                goalService,
                savingGoalService,
                expenseService,
                notificationService);
    }

    @Test
    void testGetDashboardSummary() {
        GardenResponse mockGarden = new GardenResponse(5, 10, 3, 2, List.of(), LocalDate.now());
        when(gardenService.getGarden()).thenReturn(mockGarden);

        HabitResponse mockHabit = new HabitResponse(1L, "Workout", "Everyday", "HEALTH", 3, true, LocalDate.now());
        when(habitService.getHabits()).thenReturn(List.of(mockHabit));

        GoalResponse activeGoal = new GoalResponse(1L, "Learn Spring Boot", "Backend skills", null, "IN_PROGRESS",
                List.of());
        GoalResponse completedGoal = new GoalResponse(2L, "Build Portfolio", "Frontend", null, "COMPLETED", List.of());
        when(goalService.getAllGoals()).thenReturn(List.of(activeGoal, completedGoal));

        SavingGoalResponse savingGoal = new SavingGoalResponse(1L, "MacBook Pro", "laptop", new BigDecimal("2000.00"),
                new BigDecimal("500.00"), null, null, SavingGoalStatus.ACTIVE, List.of());
        when(savingGoalService.getAllGoals()).thenReturn(List.of(savingGoal));

        FinanceOverviewResponse mockFinance = FinanceOverviewResponse.builder()
                .totalAmount(450.0)
                .monthlyData(List.of())
                .categoryData(List.of())
                .build();
        when(expenseService.getOverview(Mockito.anyInt())).thenReturn(mockFinance);

        when(notificationService.getUnreadNotifications(1L)).thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboardSummary(1L);

        assertNotNull(response);
        assertEquals(5, response.getGarden().getStreak());
        assertEquals(1, response.getHabits().size());
        assertEquals(1, response.getActiveGoals().size());
        assertEquals(2, response.getTotalGoalsCount());
        assertEquals(1, response.getCompletedGoalsCount());
        assertEquals(1, response.getActiveSavingGoals().size());
        assertEquals(new BigDecimal("500.00"), response.getTotalSavedAmount());
        assertEquals(new BigDecimal("2000.00"), response.getTotalTargetSavings());
        assertEquals(450.0, response.getFinanceOverview().getTotalAmount());
        assertEquals(0, response.getUnreadNotificationsCount());
    }
}
