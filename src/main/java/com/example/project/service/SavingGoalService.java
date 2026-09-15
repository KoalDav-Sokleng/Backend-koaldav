package com.example.project.service;

import com.example.project.Entity.SavingDeposit;
import com.example.project.Entity.SavingGoal;
import com.example.project.Enum.SavingGoalStatus;
import com.example.project.dto.request.SavingDepositRequest;
import com.example.project.dto.request.SavingGoalRequest;
import com.example.project.dto.response.SavingDepositResponse;
import com.example.project.dto.response.SavingGoalResponse;
import com.example.project.mapper.SavingGoalMapper;
import com.example.project.repository.SavingDepositRepository;
import com.example.project.repository.SavingGoalRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavingGoalService {

    private final SavingGoalRepository savingGoalRepository;
    private final SavingDepositRepository savingDepositRepository;
    private final SavingGoalMapper savingGoalMapper;
    private final NotificationService notificationService;

    public SavingGoalService(SavingGoalRepository savingGoalRepository,
            SavingDepositRepository savingDepositRepository,
            SavingGoalMapper savingGoalMapper,
            NotificationService notificationService) {
        this.savingGoalRepository = savingGoalRepository;
        this.savingDepositRepository = savingDepositRepository;
        this.savingGoalMapper = savingGoalMapper;
        this.notificationService = notificationService;
    }

    @Transactional
    public SavingGoalResponse createGoal(SavingGoalRequest request) {
        SavingGoal goal = savingGoalMapper.toEntity(request);
        SavingGoal saved = savingGoalRepository.save(goal);
        saved.setStatus(deriveStatus(saved));
        notificationService.sendDeadlineAlertForGoal(saved);
        return savingGoalMapper.toResponse(saved);
    }

    @Transactional
    public List<SavingGoalResponse> getAllGoals() {
        refreshStatuses();
        return savingGoalRepository.findAll().stream()
                .map(savingGoalMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<SavingGoalResponse> getGoalsByStatus(SavingGoalStatus status) {
        refreshStatuses();
        return savingGoalRepository.findByStatus(status).stream()
                .map(savingGoalMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SavingGoalResponse getGoalById(Long id) {
        SavingGoal goal = getGoalOrThrow(id);
        refreshStatus(goal);
        return savingGoalMapper.toResponse(goal);
    }

    @Transactional
    public SavingGoalResponse updateGoal(Long id, SavingGoalRequest request) {
        SavingGoal goal = getGoalOrThrow(id);
        refreshStatus(goal);

        if (goal.getStatus() != SavingGoalStatus.ACTIVE) {
            throw new IllegalStateException("Cannot edit a saving goal that is already completed or missed.");
        }

        if (request.getTitle() != null)
            goal.setTitle(request.getTitle());
        if (request.getTargetAmount() != null)
            goal.setTargetAmount(request.getTargetAmount());
        if (request.getDeadline() != null)
            goal.setDeadline(request.getDeadline());
        if (request.getDescription() != null)
            goal.setDescription(request.getDescription());
        if (request.getIcon() != null)
            goal.setIcon(request.getIcon());

        goal.setStatus(deriveStatus(goal));
        SavingGoal saved = savingGoalRepository.save(goal);
        notificationService.sendDeadlineAlertForGoal(saved);

        return savingGoalMapper.toResponse(saved);
    }

    @Transactional
    public void deleteGoal(Long id) {
        SavingGoal goal = getGoalOrThrow(id);
        savingGoalRepository.delete(goal);
    }

    @Transactional
    public SavingDepositResponse addDeposit(Long goalId, SavingDepositRequest request) {
        SavingGoal goal = getGoalOrThrow(goalId);
        refreshStatus(goal);

        if (goal.getStatus() != SavingGoalStatus.ACTIVE) {
            throw new IllegalStateException("Deposits can only be added to a saving goal that is still active");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than 0");
        }

        BigDecimal target = goal.getTargetAmount() == null ? BigDecimal.ZERO : goal.getTargetAmount();
        BigDecimal current = goal.getCurrentAmount() == null ? BigDecimal.ZERO : goal.getCurrentAmount();
        BigDecimal remaining = target.subtract(current);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "This saving goal has already reached its target, no more deposits allowed");
        }
        if (amount.compareTo(remaining) > 0) {
            throw new IllegalStateException("Deposit amount exceeds the remaining target of " + remaining
                    + ", you can only add up to " + remaining);
        }

        SavingDeposit deposit = savingGoalMapper.toDepositEntity(request, goal);
        SavingDeposit savedDeposit = savingDepositRepository.save(deposit);

        goal.setCurrentAmount(current.add(amount));
        goal.setStatus(deriveStatus(goal));
        savingGoalRepository.save(goal);

        return savingGoalMapper.toDepositResponse(savedDeposit);
    }

    private SavingGoal getGoalOrThrow(Long id) {
        return savingGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Saving goal not found with id: " + id));
    }

    private SavingGoalStatus deriveStatus(SavingGoal goal) {
        BigDecimal target = goal.getTargetAmount() == null ? BigDecimal.ZERO : goal.getTargetAmount();
        BigDecimal current = goal.getCurrentAmount() == null ? BigDecimal.ZERO : goal.getCurrentAmount();

        if (current.compareTo(target) >= 0) {
            return SavingGoalStatus.COMPLETED;
        }
        if (goal.getDeadline() != null && goal.getDeadline().isBefore(LocalDate.now())) {
            return SavingGoalStatus.MISSED;
        }
        return SavingGoalStatus.ACTIVE;
    }

    private void refreshStatus(SavingGoal goal) {
        SavingGoalStatus derived = deriveStatus(goal);
        if (derived != goal.getStatus()) {
            goal.setStatus(derived);
            savingGoalRepository.save(goal);
        }
    }

    private void refreshStatuses() {
        List<SavingGoal> activeGoals = savingGoalRepository.findByStatus(SavingGoalStatus.ACTIVE);
        for (SavingGoal goal : activeGoals) {
            refreshStatus(goal);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateMissedGoals() {
        List<SavingGoal> overdueGoals = savingGoalRepository.findByStatusAndDeadlineBefore(
                SavingGoalStatus.ACTIVE, LocalDate.now());
        for (SavingGoal goal : overdueGoals) {
            goal.setStatus(SavingGoalStatus.MISSED);
            savingGoalRepository.save(goal);
        }
    }
}