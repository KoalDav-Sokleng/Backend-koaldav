package com.example.project.service;

import com.example.project.Entity.Goal;
import com.example.project.dto.request.GoalRequest;
import com.example.project.dto.response.GoalResponse;
import com.example.project.mapper.GoalMapper;
import com.example.project.repository.GoalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoalService {

    private final GoalRepository repository;
    private final GoalMapper mapper;
    private final NotificationService notificationService;

    public GoalService(GoalRepository repository, GoalMapper mapper, NotificationService notificationService) {
        this.repository = repository;
        this.mapper = mapper;
        this.notificationService = notificationService;
    }

    public GoalResponse createGoal(GoalRequest request) {
        Goal goal = mapper.toEntity(request);
        Goal saved = repository.save(goal);
        notificationService.sendDeadlineAlertForGoal(saved);
        return mapper.toResponse(saved);
    }

    public GoalResponse getGoal(Long id) {
        Goal goal = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found: " + id));
        return mapper.toResponse(goal);
    }

    public List<GoalResponse> getAllGoals() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<GoalResponse> getGoalsByStatus(com.example.project.Enum.GoalStatus status) {
        return repository.findByStatus(status).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public GoalResponse updateGoal(Long id, GoalRequest request) {
        Goal goal = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found: " + id));
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setDeadline(request.getDeadline());

        if (goal.getStatus() == com.example.project.Enum.GoalStatus.MISSED
                && request.getDeadline() != null
                && !request.getDeadline().isBefore(java.time.LocalDate.now())) {
            goal.setStatus(com.example.project.Enum.GoalStatus.IN_PROGRESS);
        }

        Goal saved = repository.save(goal);
        notificationService.sendDeadlineAlertForGoal(saved);
        return mapper.toResponse(saved);
    }

    public void deleteGoal(Long id) {
        repository.deleteById(id);
    }

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 0 * * ?")
    public void updateMissedGoals() {
        List<Goal> overdueGoals = repository.findByStatusAndDeadlineBefore(
                com.example.project.Enum.GoalStatus.IN_PROGRESS,
                java.time.LocalDate.now());
        for (Goal goal : overdueGoals) {
            goal.setStatus(com.example.project.Enum.GoalStatus.MISSED);
            if (goal.getMilestone() != null) {
                for (com.example.project.Entity.Milestone m : goal.getMilestone()) {
                    if (m.getStatus() == com.example.project.Enum.MilestoneStatus.IN_PROGRESS) {
                        m.setStatus(com.example.project.Enum.MilestoneStatus.MISSED);
                    }
                }
            }
            repository.save(goal);
        }
    }
}