
package com.example.project.mapper;

import com.example.project.Entity.Goal;
import com.example.project.Enum.GoalStatus;
import com.example.project.dto.request.GoalRequest;
import com.example.project.dto.response.GoalResponse;
import com.example.project.dto.response.MilestoneResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoalMapper {

    private final MilestoneMapper milestoneMapper;

    public GoalMapper(MilestoneMapper milestoneMapper) {
        this.milestoneMapper = milestoneMapper;
    }

    public Goal toEntity(GoalRequest request) {
        Goal goal = new Goal();
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setDeadline(request.getDeadline());
        goal.setStatus(GoalStatus.IN_PROGRESS); // default on creation
        return goal;
    }

    public GoalResponse toResponse(Goal goal) {
        List<MilestoneResponse> milestoneResponses =
                goal.getMilestone() == null
                        ? Collections.emptyList()
                        : goal.getMilestone().stream()
                        .map(milestoneMapper::toResponse)
                        .collect(Collectors.toList());

        return new GoalResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getDeadline(),
                goal.getStatus().name(),
                milestoneResponses
        );
    }
}