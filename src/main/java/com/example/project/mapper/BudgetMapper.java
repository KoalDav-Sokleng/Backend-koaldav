package com.example.project.mapper;

import com.example.project.Entity.Budget;
import com.example.project.Enum.BudgetPeriod;
import com.example.project.dto.request.BudgetRequest;
import com.example.project.dto.response.BudgetResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class BudgetMapper {

    public Budget toEntity(BudgetRequest request) {
        if (request == null)
            return null;

        BudgetPeriod period = request.getPeriod() != null ? request.getPeriod() : BudgetPeriod.MONTHLY;
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate()
                : LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : startDate.plusMonths(1).minusDays(1);
        String icon = request.getIcon() != null && !request.getIcon().isBlank() ? request.getIcon() : "🎯";
        String color = request.getColor() != null && !request.getColor().isBlank() ? request.getColor() : "#10B981";

        return Budget.builder()
                .name(request.getName().trim())
                .category(request.getCategory().trim())
                .limitAmount(request.getLimitAmount())
                .spentAmount(0.0)
                .period(period)
                .startDate(startDate)
                .endDate(endDate)
                .icon(icon)
                .color(color)
                .build();
    }

    public BudgetResponse toResponse(Budget budget) {
        if (budget == null)
            return null;

        double limit = budget.getLimitAmount() != null ? budget.getLimitAmount() : 0.0;
        double spent = budget.getSpentAmount() != null ? budget.getSpentAmount() : 0.0;
        double remaining = Math.round((limit - spent) * 100.0) / 100.0;
        double percentage = limit > 0 ? Math.min(100.0, Math.round((spent / limit) * 10000.0) / 100.0) : 0.0;
        boolean isOver = spent > limit;

        return BudgetResponse.builder()
                .id(budget.getId())
                .name(budget.getName())
                .category(budget.getCategory())
                .limitAmount(Math.round(limit * 100.0) / 100.0)
                .spentAmount(Math.round(spent * 100.0) / 100.0)
                .remainingAmount(remaining)
                .percentageUsed(percentage)
                .isOverbudget(isOver)
                .period(budget.getPeriod())
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .icon(budget.getIcon())
                .color(budget.getColor())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}
