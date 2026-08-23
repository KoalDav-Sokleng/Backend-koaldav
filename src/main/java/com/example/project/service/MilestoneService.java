package com.example.project.service;

import com.example.project.Entity.Goal;
import com.example.project.Entity.Milestone;
import com.example.project.Enum.GoalStatus;
import com.example.project.Enum.MilestoneStatus;
import com.example.project.dto.request.MilestoneRequest;
import com.example.project.dto.response.MilestoneResponse;
import com.example.project.mapper.MilestoneMapper;
import com.example.project.repository.GoalRepository;
import com.example.project.repository.MilestoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final GoalRepository goalRepository;
    private final MilestoneMapper mapper;

    public MilestoneService(MilestoneRepository milestoneRepository,
                            GoalRepository goalRepository,
                            MilestoneMapper mapper) {
        this.milestoneRepository = milestoneRepository;
        this.goalRepository = goalRepository;
        this.mapper = mapper;
    }

    public MilestoneResponse create(Long goalId, MilestoneRequest request) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found: " + goalId));

        Milestone milestone = mapper.toEntity(request);
        milestone.setGoal(goal);
        Milestone saved = milestoneRepository.save(milestone);
        return mapper.toResponse(saved);
    }

    public List<MilestoneResponse> getMilestonesByGoalId(Long goalId) {
        return milestoneRepository.findAll().stream()
                .filter(m -> m.getGoal() != null && m.getGoal().getId().equals(goalId))
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public MilestoneResponse getMilestone(Long goalId, Long milestoneId) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found: " + milestoneId));
        return mapper.toResponse(milestone);
    }

    public MilestoneResponse completeMilestone(Long goalId, Long milestoneId) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found: " + milestoneId));

        int totalMinutes = milestone.getFocusSessions() == null ? 0 :
                milestone.getFocusSessions().stream()
                        .mapToInt(fs -> fs.getDurationMinutes() == null ? 0 : fs.getDurationMinutes())
                        .sum();

        if (totalMinutes < 60) {
            throw new RuntimeException("Need at least 1 hour focus time");
        }

        milestone.setStatus(MilestoneStatus.COMPLETED);
        Milestone saved = milestoneRepository.save(milestone);

        Goal goal = saved.getGoal();
        boolean allComplete = goal.getMilestone().stream()
                .allMatch(m -> m.getStatus() == MilestoneStatus.COMPLETED);
        if (allComplete) {
            goal.setStatus(GoalStatus.COMPLETED);
            goalRepository.save(goal);
        }

        return mapper.toResponse(saved);
    }
}