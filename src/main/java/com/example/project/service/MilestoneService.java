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
import org.springframework.transaction.annotation.Transactional;

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

        String title = request.getTitle() != null ? request.getTitle().trim() : "";
        if (title.isEmpty()) {
            throw new RuntimeException("Milestone title cannot be empty");
        }

        boolean exists = milestoneRepository.findAll().stream()
                .anyMatch(m -> m.getGoal() != null && m.getGoal().getId().equals(goalId)
                        && m.getTitle() != null && m.getTitle().trim().equalsIgnoreCase(title));
        if (exists) {
            throw new RuntimeException("A milestone named '" + title + "' already exists for this goal.");
        }

        Milestone milestone = mapper.toEntity(request);
        milestone.setTitle(title);
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

    public MilestoneResponse updateMilestone(Long goalId, Long milestoneId, MilestoneRequest request) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found: " + milestoneId));

        String title = request.getTitle() != null ? request.getTitle().trim() : "";
        if (title.isEmpty()) {
            throw new RuntimeException("Milestone title cannot be empty");
        }

        boolean exists = milestoneRepository.findAll().stream()
                .anyMatch(m -> m.getGoal() != null && m.getGoal().getId().equals(goalId)
                        && !m.getId().equals(milestoneId)
                        && m.getTitle() != null && m.getTitle().trim().equalsIgnoreCase(title));
        if (exists) {
            throw new RuntimeException("A milestone named '" + title + "' already exists for this goal.");
        }

        milestone.setTitle(title);
        Milestone saved = milestoneRepository.save(milestone);
        return mapper.toResponse(saved);
    }

    @Transactional
    public void deleteMilestone(Long goalId, Long milestoneId) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found: " + milestoneId));

        Goal goal = milestone.getGoal();
        if (goal != null && goal.getMilestone() != null) {
            goal.getMilestone().remove(milestone);
        }
        milestoneRepository.delete(milestone);
    }

    public MilestoneResponse completeMilestone(Long goalId, Long milestoneId) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found: " + milestoneId));

        int totalMinutes = milestone.getFocusSessions() == null ? 0
                : milestone.getFocusSessions().stream()
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