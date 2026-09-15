package com.example.project.mapper;

import com.example.project.Entity.SavingDeposit;
import com.example.project.Entity.SavingGoal;
import com.example.project.Enum.SavingGoalStatus;
import com.example.project.dto.request.SavingDepositRequest;
import com.example.project.dto.request.SavingGoalRequest;
import com.example.project.dto.response.SavingDepositResponse;
import com.example.project.dto.response.SavingGoalResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SavingGoalMapper {

    public SavingGoal toEntity(SavingGoalRequest request) {
        SavingGoal goal = new SavingGoal();
        goal.setTitle(request.getTitle());
        goal.setIcon(request.getIcon() != null ? request.getIcon() : "piggy");
        goal.setTargetAmount(request.getTargetAmount());
        goal.setCurrentAmount(request.getCurrentAmount() != null ? request.getCurrentAmount() : BigDecimal.ZERO);
        goal.setDeadline(request.getDeadline());
        goal.setDescription(request.getDescription());
        goal.setStatus(SavingGoalStatus.ACTIVE);
        return goal;
    }

    public SavingGoalResponse toResponse(SavingGoal goal) {
        List<SavingDepositResponse> depositResponses = goal.getDeposits() == null ? Collections.emptyList() :
                goal.getDeposits().stream()
                        .map(this::toDepositResponse)
                        .collect(Collectors.toList());

        return new SavingGoalResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getIcon(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getDeadline(),
                goal.getDescription(),
                goal.getStatus(),
                depositResponses
        );
    }

    public SavingDeposit toDepositEntity(SavingDepositRequest request, SavingGoal goal) {
        SavingDeposit deposit = new SavingDeposit();
        deposit.setTitle(request.getTitle() != null ? request.getTitle() : "One-time Deposit");
        deposit.setDepositDate(request.getDate() != null ? request.getDate() : java.time.LocalDate.now());
        deposit.setSource(request.getSource());
        deposit.setAmount(request.getAmount());
        deposit.setNotes(request.getNotes());
        deposit.setSavingGoal(goal);
        return deposit;
    }

    public SavingDepositResponse toDepositResponse(SavingDeposit deposit) {
        return new SavingDepositResponse(
                deposit.getId(),
                deposit.getTitle(),
                deposit.getDepositDate(),
                deposit.getSource(),
                deposit.getAmount(),
                deposit.getNotes()
        );
    }
}