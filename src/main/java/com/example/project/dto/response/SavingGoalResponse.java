package com.example.project.dto.response;

import com.example.project.Enum.SavingGoalStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SavingGoalResponse {
    private Long id;
    private String title;
    private String icon;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate deadline;
    private String description;
    private SavingGoalStatus status;
    private List<SavingDepositResponse> deposits;
}