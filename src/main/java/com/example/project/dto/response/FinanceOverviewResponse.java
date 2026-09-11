package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceOverviewResponse {

    private List<MonthlyExpenseDTO> monthlyData;
    private List<CategoryExpenseDTO> categoryData;
    private Double totalAmount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyExpenseDTO {
        private String month;
        private Double amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryExpenseDTO {
        private String name;
        private Double value;
        private String color;
        private String icon;
    }
}
