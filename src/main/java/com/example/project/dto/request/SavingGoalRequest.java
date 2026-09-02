package com.example.project.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class SavingGoalRequest {
    private String title;
    private String icon;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate deadline;
    private String description;
}