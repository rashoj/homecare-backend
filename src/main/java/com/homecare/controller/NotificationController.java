package com.homecare.controller;

import com.homecare.dto.NotificationRequest;
import com.homecare.dto.NotificationResponse;
import com.homecare.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin("*")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public NotificationResponse createNotification(
            @RequestBody NotificationRequest request
    ) {
        return notificationService.createNotification(request);
    }

    @GetMapping("/user/{userId}")
    public List<NotificationResponse> getUserNotifications(
            @PathVariable Long userId
    ) {
        return notificationService.getUserNotifications(userId);
    }

    @GetMapping("/user/{userId}/unread")
    public List<NotificationResponse> getUnreadNotifications(
            @PathVariable Long userId
    ) {
        return notificationService.getUnreadNotifications(userId);
    }

    @GetMapping("/user/{userId}/unread-count")
    public Long getUnreadCount(
            @PathVariable Long userId
    ) {
        return notificationService.getUnreadCount(userId);
    }

    @PutMapping("/{id}/read")
    public NotificationResponse markAsRead(
            @PathVariable Long id
    ) {
        return notificationService.markAsRead(id);
    }
}