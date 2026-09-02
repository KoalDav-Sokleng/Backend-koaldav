package com.example.project.service;

import com.example.project.Entity.Goal;
import com.example.project.Entity.Notification;
import com.example.project.Entity.SavingGoal;
import com.example.project.Entity.TripGoal;
import com.example.project.Enum.GoalStatus;
import com.example.project.Enum.NotificationType;
import com.example.project.Enum.SavingGoalStatus;
import com.example.project.dto.response.NotificationResponse;
import com.example.project.mapper.NotificationMapper;
import com.example.project.repository.GoalRepository;
import com.example.project.repository.NotificationRepository;
import com.example.project.repository.SavingGoalRepository;
import com.example.project.repository.TripGoalRepository;
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

import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final GoalRepository goalRepository;
    private final SavingGoalRepository savingGoalRepository;
    private final TripGoalRepository tripGoalRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.notification.default-email:${MAIL_DEFAULT_RECIPIENT:${MAIL_USERNAME:sreyleng143@gmail.com}}}")
    private String defaultEmail;

    public NotificationService(
            NotificationRepository notificationRepository,
            GoalRepository goalRepository,
            SavingGoalRepository savingGoalRepository,
            TripGoalRepository tripGoalRepository,
            NotificationMapper notificationMapper,
            EmailService emailService,
            SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.goalRepository = goalRepository;
        this.savingGoalRepository = savingGoalRepository;
        this.tripGoalRepository = tripGoalRepository;
        this.notificationMapper = notificationMapper;
        this.emailService = emailService;
        this.messagingTemplate = messagingTemplate;
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void checkGoalDeadlines() {
        LocalDate today = LocalDate.now();
        LocalDate maxTargetDate = today.plusDays(3);

        // Project Goals
        List<Goal> upcomingGoals = goalRepository.findByStatusNotAndDeadlineBetween(
                GoalStatus.COMPLETED, today, maxTargetDate);
        for (Goal goal : upcomingGoals) {
            sendDeadlineAlertForGoal(goal);
        }

        // Saving Goals
        List<SavingGoal> upcomingSaving = savingGoalRepository.findByStatus(SavingGoalStatus.ACTIVE)
                .stream()
                .filter(g -> g.getDeadline() != null && !g.getDeadline().isBefore(today)
                        && !g.getDeadline().isAfter(maxTargetDate))
                .collect(Collectors.toList());
        for (SavingGoal g : upcomingSaving) {
            sendDeadlineAlertForGoal(g);
        }

        // Trip Goals
        List<TripGoal> upcomingTrips = tripGoalRepository.findAll()
                .stream()
                .filter(g -> g.getDeadline() != null && !g.getDeadline().isBefore(today)
                        && !g.getDeadline().isAfter(maxTargetDate))
                .collect(Collectors.toList());
        for (TripGoal g : upcomingTrips) {
            sendDeadlineAlertForGoal(g);
        }
    }

    public void sendDeadlineAlertForGoal(Goal goal) {
        if (goal != null && goal.getDeadline() != null) {
            processAlert(goal.getId(), goal.getTitle(), goal.getDeadline(), "PROJECT");
        }
    }

    public void sendDeadlineAlertForGoal(SavingGoal goal) {
        if (goal != null && goal.getDeadline() != null) {
            processAlert(goal.getId(), goal.getTitle(), goal.getDeadline(), "SAVING");
        }
    }

    public void sendDeadlineAlertForGoal(TripGoal goal) {
        if (goal != null && goal.getDeadline() != null) {
            processAlert(goal.getId(), goal.getName(), goal.getDeadline(), "TRIP");
        }
    }

    private void processAlert(Long goalId, String goalTitle, LocalDate deadline, String goalType) {
        LocalDate today = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(today, deadline);

        if (daysRemaining < 0 || daysRemaining > 3) {
            return;
        }

        String warningText;
        String emailSubject;

        if (daysRemaining == 0) {
            warningText = "🚨 Urgent: Deadline is today!";
            emailSubject = "🚨 Due Today: " + goalTitle;
        } else if (daysRemaining == 1) {
            warningText = "⚠️ Warning: You only have 1 day left!";
            emailSubject = "⚠️ 1 Day Left: " + goalTitle;
        } else {
            warningText = "⚠️ Warning: You only have " + daysRemaining + " days left!";
            emailSubject = "⚠️ Deadline Alert: " + goalTitle;
        }

        Long mockUserId = 1L;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        Optional<Notification> existingOpt = notificationRepository.findByUserIdAndGoalIdAndGoalType(mockUserId, goalId,
                goalType);
        Notification notification;
        boolean shouldSendEmail = false;

        if (existingOpt.isPresent()) {
            notification = existingOpt.get();
            if (daysRemaining != notification.getDaysLeft()) {
                notification.setDaysLeft(daysRemaining);
                notification.setDeadline(deadline);
                notification.setGoalTitle(goalTitle);
                notification.setWarningMessage(warningText);
                notification.setIsRead(false);
                notification.setCreatedAt(LocalDateTime.now());
                shouldSendEmail = true;
            }
        } else {
            notification = new Notification();
            notification.setUserId(mockUserId);
            notification.setGoalId(goalId);
            notification.setGoalType(goalType);
            notification.setGoalTitle(goalTitle);
            notification.setDeadline(deadline);
            notification.setDaysLeft(daysRemaining);
            notification.setWarningMessage(warningText);
            notification.setType(NotificationType.DEADLINE_ALERT);
            notification.setIsRead(false);
            shouldSendEmail = true;
        }

        Notification saved = notificationRepository.save(notification);

        if (shouldSendEmail) {
            // Push real-time notification to the frontend
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + mockUserId,
                    notificationMapper.toResponse(saved));

            String formattedDate = deadline.format(formatter);
            String viewLink = "http://localhost:8080/api/notifications/" + saved.getId() + "/open";
            String body = "Goal (" + goalType + "):\n" + goalTitle
                    + "\n\nDeadline:\n" + formattedDate
                    + "\n\n" + warningText
                    + "\n\nClick here to view your goal and clear this alert:\n" + viewLink;

            String recipient = resolveDefaultEmail(defaultEmail);
            emailService.sendEmail(recipient, emailSubject, body);
        }
    }

    public String resolveDefaultEmail(String configuredEmail) {
        if (configuredEmail == null) {
            return "sreyleng143@gmail.com";
        }

        String trimmed = configuredEmail.trim();
        return trimmed.isEmpty() ? "sreyleng143@gmail.com" : trimmed;
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