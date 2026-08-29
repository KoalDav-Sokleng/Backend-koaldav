package com.example.project.dto.response;

import com.example.project.Enum.NotificationType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    Long userId,
    Long goalId,
    String goalTitle,
    LocalDate deadline,
    Long daysLeft,
    String warningMessage,
    NotificationType type,
    Boolean isRead,
    LocalDateTime createdAt
) {}
