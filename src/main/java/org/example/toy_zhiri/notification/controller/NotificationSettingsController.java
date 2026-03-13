package org.example.toy_zhiri.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.notification.dto.NotificationSettingsResponse;
import org.example.toy_zhiri.notification.dto.UpdateNotificationSettingsRequest;
import org.example.toy_zhiri.notification.service.NotificationSettingsService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications/settings")
@RequiredArgsConstructor
@Tag(name = "Notification Settings", description = "Управление настройками уведомлений")
public class NotificationSettingsController {
    private final NotificationSettingsService notificationSettingsService;
    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Получить настройки уведомлений",
            description = "Возвращает текущие настройки уведомлений пользователя. " +
                    "Если настройки ещё не заданы — возвращает дефолтные значения.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<NotificationSettingsResponse> getSettings(
            @AuthenticationPrincipal UserDetails userDetails) {

        java.util.UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(notificationSettingsService.getSettings(userId));
    }

    @PatchMapping
    @Operation(
            summary = "Обновить настройки уведомлений",
            description = "Обновляет настройки уведомлений. Передавать только те поля, которые нужно изменить.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<NotificationSettingsResponse> updateSettings(
            @RequestBody UpdateNotificationSettingsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        java.util.UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(notificationSettingsService.updateSettings(userId, request));
    }
}