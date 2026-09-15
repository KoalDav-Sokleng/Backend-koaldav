package com.example.project.mapper;




import com.example.project.Entity.Milestone;
import com.example.project.Enum.MilestoneStatus;
import com.example.project.dto.request.MilestoneRequest;
import com.example.project.dto.response.FocusSessionResponse;
import com.example.project.dto.response.MilestoneResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MilestoneMapper {

    private final FocusSessionMapper focusSessionMapper;

    public MilestoneMapper(FocusSessionMapper focusSessionMapper) {
        this.focusSessionMapper = focusSessionMapper;
    }

    public Milestone toEntity(MilestoneRequest request) {
        Milestone milestone = new Milestone();
        milestone.setTitle(request.getTitle());
        milestone.setStatus(MilestoneStatus.IN_PROGRESS); // default on creation
        return milestone;
    }

    public MilestoneResponse toResponse(Milestone milestone) {
        List<FocusSessionResponse> sessionResponses =
                milestone.getFocusSessions() == null
                        ? Collections.emptyList()
                        : milestone.getFocusSessions().stream()
                        .map(focusSessionMapper::toResponse)
                        .collect(Collectors.toList());

        int totalMinutes = sessionResponses.stream()
                .mapToInt(FocusSessionResponse::getDurationMinutes)
                .sum();

        return new MilestoneResponse(
                milestone.getId(),
                milestone.getTitle(),
                milestone.getStatus().name(),
                totalMinutes,
                sessionResponses
        );
    }
}