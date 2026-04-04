package org.example.toy_zhiri.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.notification.dto.TestNotificationRequest;
import org.example.toy_zhiri.notification.service.NotificationService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/notifications/test")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Notification Test", description = "Тестирование каналов уведомлений (только для админов)")
public class AdminNotificationTestController {
    private final NotificationService notificationService;
    private final UserService userService;

    @PostMapping
    @Operation(
            summary = "Отправить тестовое уведомление",
            description = "Отправляет тестовое уведомление текущему администратору " +
                    "по выбранным каналам (push, email, sms). " +
                    "Используется для проверки работоспособности каналов.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> sendTestNotification(
            @Valid @RequestBody TestNotificationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        String result = notificationService.sendTest(userId, request);

        return ResponseEntity.ok(MessageResponse.builder()
                .message(result)
                .build());
    }
}