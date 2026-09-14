package com.example.project.dto.request;

import com.example.project.Enum.BudgetPeriod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetRequest {

    @NotBlank(message = "Budget name is required")
    @Size(max = 100, message = "Budget name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Limit amount is required")
    @Positive(message = "Limit amount must be greater than 0")
    private Double limitAmount;

    private BudgetPeriod period;

    private LocalDate startDate;

    private LocalDate endDate;

    private String icon;

    private String color;
}
