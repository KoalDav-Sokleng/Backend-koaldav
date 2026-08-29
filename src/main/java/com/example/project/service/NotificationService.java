package com.example.project.service;

import com.example.project.Entity.Goal;
import com.example.project.Entity.Notification;
import com.example.project.Enum.GoalStatus;
import com.example.project.Enum.NotificationType;
import com.example.project.dto.response.NotificationResponse;
import com.example.project.mapper.NotificationMapper;
import com.example.project.repository.GoalRepository;
import com.example.project.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final GoalRepository goalRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;

    @Value("${app.notification.default-email:sreyleng143@gmail.com}")
    private String defaultEmail;

    public NotificationService(
            NotificationRepository notificationRepository,
            GoalRepository goalRepository,
            NotificationMapper notificationMapper,
            EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.goalRepository = goalRepository;
        this.notificationMapper = notificationMapper;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void checkGoalDeadlines() {
        LocalDate today = LocalDate.now();
        LocalDate maxTargetDate = today.plusDays(3);

        List<Goal> upcomingGoals = goalRepository.findByStatusNotAndDeadlineBetween(
                GoalStatus.COMPLETED, today, maxTargetDate
        );

        Long mockUserId = 1L;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

        for (Goal goal : upcomingGoals) {
            long daysRemaining = ChronoUnit.DAYS.between(today, goal.getDeadline());

            String warningText;
            String emailSubject;

            if (daysRemaining == 0) {
                warningText = "🚨 Urgent: Deadline is today!";
                emailSubject = "🚨 Due Today: " + goal.getTitle();
            } else if (daysRemaining == 1) {
                warningText = "⚠️ Warning: You only have 1 day left!";
                emailSubject = "⚠️ 1 Day Left: " + goal.getTitle();
            } else {
                warningText = "⚠️ Warning: You only have " + daysRemaining + " days left!";
                emailSubject = "⚠️ Deadline Alert: " + goal.getTitle();
            }

            Optional<Notification> existingOpt = notificationRepository.findByUserIdAndGoalId(mockUserId, goal.getId());
            Notification notification;
            boolean shouldSendEmail = false;

            if (existingOpt.isPresent()) {
                notification = existingOpt.get();

                // If days remaining changed (e.g. 3 -> 2 -> 1 -> 0), update card in-place and relight the red dot
                if (daysRemaining != notification.getDaysLeft()) {
                    notification.setDaysLeft(daysRemaining);
                    notification.setDeadline(goal.getDeadline());
                    notification.setGoalTitle(goal.getTitle());
                    notification.setWarningMessage(warningText);
                    notification.setIsRead(false);
                    notification.setCreatedAt(LocalDateTime.now());
                    shouldSendEmail = true;
                }
            } else {
                // First time creating notification for this goal
                notification = new Notification();
                notification.setUserId(mockUserId);
                notification.setGoalId(goal.getId());
                notification.setGoalTitle(goal.getTitle());
                notification.setDeadline(goal.getDeadline());
                notification.setDaysLeft(daysRemaining);
                notification.setWarningMessage(warningText);
                notification.setType(NotificationType.DEADLINE_ALERT);
                notification.setIsRead(false);
                shouldSendEmail = true;
            }

            Notification saved = notificationRepository.save(notification);

            if (shouldSendEmail) {
                String formattedDate = goal.getDeadline().format(formatter);
                String viewLink = "http://localhost:8081/api/notifications/" + saved.getId() + "/open";
                String body = "Goal:\n" + goal.getTitle()
                        + "\n\nDeadline:\n" + formattedDate
                        + "\n\n" + warningText
                        + "\n\nClick here to view your goal and clear this alert:\n" + viewLink;

                emailService.sendEmail(defaultEmail, emailSubject, body);
            }
        }
    }

    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id " + id));
        notification.setIsRead(true);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }
}