package com.example.project.mapper;

import com.example.project.Entity.Budget;
import com.example.project.Entity.Expense;
import com.example.project.Entity.Wallet;
import com.example.project.dto.request.ExpenseRequest;
import com.example.project.dto.response.ExpenseResponse;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public Expense toEntity(ExpenseRequest request, String icon, Wallet wallet, Budget budget) {
        if (request == null)
            return null;

        return Expense.builder()
                .title(request.getTitle())
                .amount(request.getAmount())
                .category(request.getCategory())
                .icon(icon)
                .date(request.getDate())
                .note(request.getNote())
                .wallet(wallet)
                .budget(budget)
                .build();
    }

    public Expense toEntity(ExpenseRequest request, String icon) {
        return toEntity(request, icon, null, null);
    }

    public ExpenseResponse toResponse(Expense expense) {
        if (expense == null)
            return null;

        Long walletId = expense.getWallet() != null ? expense.getWallet().getId() : null;
        String walletName = expense.getWallet() != null ? expense.getWallet().getName() : null;
        Long budgetId = expense.getBudget() != null ? expense.getBudget().getId() : null;
        String budgetName = expense.getBudget() != null ? expense.getBudget().getName() : null;

        return ExpenseResponse.builder()
                .id(expense.getId())
                .icon(expense.getIcon())
                .title(expense.getTitle())
                .category(expense.getCategory())
                .note(expense.getNote())
                .date(expense.getDate())
                .amount(expense.getAmount())
                .walletId(walletId)
                .walletName(walletName)
                .budgetId(budgetId)
                .budgetName(budgetName)
                .build();
    }
}
