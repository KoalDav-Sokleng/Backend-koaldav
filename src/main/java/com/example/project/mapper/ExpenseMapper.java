package com.example.project.mapper;

import com.example.project.Entity.Expense;
import com.example.project.dto.request.ExpenseRequest;
import com.example.project.dto.response.ExpenseResponse;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public Expense toEntity(ExpenseRequest request, String icon) {
        if (request == null)
            return null;

        return Expense.builder()
                .title(request.getTitle())
                .amount(request.getAmount())
                .category(request.getCategory())
                .icon(icon)
                .date(request.getDate())
                .note(request.getNote())
                .build();
    }

    public ExpenseResponse toResponse(Expense expense) {
        if (expense == null)
            return null;

        return ExpenseResponse.builder()
                .id(expense.getId())
                .icon(expense.getIcon())
                .title(expense.getTitle())
                .category(expense.getCategory())
                .note(expense.getNote())
                .date(expense.getDate())
                .amount(expense.getAmount())
                .build();
    }
}
