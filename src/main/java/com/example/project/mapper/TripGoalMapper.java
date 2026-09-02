package com.example.project.mapper;

import com.example.project.Entity.TripGoal;
import com.example.project.dto.request.TripGoalRequest;
import com.example.project.dto.response.TripGoalResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TripGoalMapper {

    public TripGoal toEntity(TripGoalRequest request) {
        if (request == null)
            return null;
        return TripGoal.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type("trip")
                .target(request.getTarget() != null ? request.getTarget() : BigDecimal.ZERO)
                .saved(BigDecimal.ZERO)
                .deadline(request.getDeadline())
                .image(request.getImageUrl())
                .build();
    }

    public TripGoalResponse toResponse(TripGoal entity) {
        if (entity == null)
            return null;
        return TripGoalResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .target(entity.getTarget())
                .saved(entity.getSaved())
                .deadline(entity.getDeadline())
                .image(entity.getImage())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public void updateEntityFromRequest(TripGoalRequest request, TripGoal entity) {
        if (request == null || entity == null)
            return;
        if (request.getName() != null)
            entity.setName(request.getName());
        if (request.getDescription() != null)
            entity.setDescription(request.getDescription());
        if (request.getTarget() != null)
            entity.setTarget(request.getTarget());
        if (request.getDeadline() != null)
            entity.setDeadline(request.getDeadline());
    }
}
