package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponse {
    private Long id;
    private String icon;
    private String title;
    private String category;
    private String note;
    private LocalDate date;
    private Double amount;
    private Long walletId;
    private String walletName;
    private Long budgetId;
    private String budgetName;
}
