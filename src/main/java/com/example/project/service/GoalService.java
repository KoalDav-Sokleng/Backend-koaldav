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
        notificationService.checkGoalDeadlines();
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

    public GoalResponse updateGoal(Long id, GoalRequest request) {
        Goal goal = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found: " + id));
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setDeadline(request.getDeadline());
        Goal saved = repository.save(goal);
        notificationService.checkGoalDeadlines();
        return mapper.toResponse(saved);
    }

    public void deleteGoal(Long id) {
        repository.deleteById(id);
    }
}