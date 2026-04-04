package org.example.toy_zhiri.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.notification.dto.NotificationResponse;
import org.example.toy_zhiri.notification.dto.UnreadCountResponse;
import org.example.toy_zhiri.notification.service.NotificationService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Уведомления пользователя")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Получить уведомления",
            description = "Возвращает список уведомлений текущего пользователя с пагинацией.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, pageable));
    }

    @GetMapping("/unread")
    @Operation(
            summary = "Непрочитанные уведомления",
            description = "Возвращает только непрочитанные уведомления.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId, pageable));
    }

    @GetMapping("/unread/count")
    @Operation(
            summary = "Количество непрочитанных",
            description = "Возвращает число непрочитанных уведомлений. " +
                    "Используется для бейджика на иконке колокольчика.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(
            summary = "Пометить как прочитанное",
            description = "Помечает конкретное уведомление как прочитанное.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(notificationService.markAsRead(userId, notificationId));
    }

    @PatchMapping("/read-all")
    @Operation(
            summary = "Прочитать все",
            description = "Помечает все уведомления пользователя как прочитанные.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(notificationService.markAllAsRead(userId));
    }
}