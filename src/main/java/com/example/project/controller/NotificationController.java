package com.example.project.controller;

import com.example.project.dto.response.NotificationResponse;
import com.example.project.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications(@RequestParam(defaultValue = "1") Long userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(@RequestParam(defaultValue = "1") Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @GetMapping("/{id}/open")
    public RedirectView openNotificationFromEmail(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return new RedirectView("http://localhost:5173/goal");
    }

    @PostMapping("/trigger-check")
    public ResponseEntity<String> triggerCheck() {
        notificationService.checkGoalDeadlines();
        return ResponseEntity.ok("Goal deadline check ran successfully.");
    }
}
