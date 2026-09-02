package com.example.project.mapper;

import com.example.project.Entity.Notification;
import com.example.project.dto.response.NotificationResponse;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationResponse toResponse(Notification entity) {
        if (entity == null)
            return null;
        return new NotificationResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getGoalId(),
                entity.getGoalType(),
                entity.getGoalTitle(),
                entity.getDeadline(),
                entity.getDaysLeft(),
                entity.getWarningMessage(),
                entity.getType(),
                entity.getIsRead(),
                entity.getCreatedAt());
    }
}
