package com.example.project.dto.response;

import com.example.project.Enum.BudgetPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetResponse {
    private Long id;
    private String name;
    private String category;
    private Double limitAmount;
    private Double spentAmount;
    private Double remainingAmount;
    private Double percentageUsed;
    private Boolean isOverbudget;
    private BudgetPeriod period;
    private LocalDate startDate;
    private LocalDate endDate;
    private String icon;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
