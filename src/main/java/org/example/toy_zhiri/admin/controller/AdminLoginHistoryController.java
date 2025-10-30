package org.example.toy_zhiri.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.example.toy_zhiri.admin.dto.LoginHistoryStatsResponse;
import org.example.toy_zhiri.auth.dto.LoginHistoryResponse;
import org.example.toy_zhiri.auth.service.LoginHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Контроллер для просмотра истории входов (только для администраторов).
 */
@RestController
@RequestMapping("/api/v1/admin/login-history")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Login History", description = "История входов пользователей (только для админов)")
public class AdminLoginHistoryController {
    private final LoginHistoryService loginHistoryService;

    /**
     * Получить историю входов конкретного пользователя по ID.
     *
     * @param userId идентификатор пользователя
     * @param page номер страницы
     * @param size размер страницы
     * @return ResponseEntity<Page<LoginHistoryResponse>> страница с историей входов
     */
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "История входов пользователя",
            description = "Получить историю входов/выходов конкретного пользователя по ID. Доступно только администраторам.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<LoginHistoryResponse>> getUserLoginHistory(
            @Parameter(description = "ID пользователя")
            @PathVariable UUID userId,

            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<LoginHistoryResponse> history = loginHistoryService.getUserLoginHistory(userId, pageable);

        return ResponseEntity.ok(history);
    }

    /**
     * Получить общую статистику входов пользователя.
     *
     * @param userId идентификатор пользователя
     * @return ResponseEntity<LoginHistoryStatsResponse> статистика входов
     */
    @GetMapping("/user/{userId}/stats")
    @Operation(
            summary = "Статистика входов пользователя",
            description = "Получить статистику: общее количество входов, последний вход и т.д.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<LoginHistoryStatsResponse> getUserLoginStats(
            @Parameter(description = "ID пользователя")
            @PathVariable UUID userId) {

        long totalLogins = loginHistoryService.getTotalLogins(userId);

        LoginHistoryStatsResponse stats = LoginHistoryStatsResponse.builder()
                .userId(userId)
                .totalSuccessfulLogins(totalLogins)
                .build();

        return ResponseEntity.ok(stats);
    }
}