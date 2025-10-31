package org.example.toy_zhiri.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.*;
import org.example.toy_zhiri.admin.service.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Контроллер для управления пользователями администратором.
 * Предоставляет полный CRUD функционал для работы с пользователями.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Users", description = "API для управления пользователями администратором")
public class AdminUserController {
    private final AdminUserService adminUserService;

    /**
     * Получить список всех пользователей с пагинацией и фильтрацией.
     *
     * @param page номер страницы (начиная с 0)
     * @param size размер страницы
     * @param sortBy поле для сортировки (по умолчанию createdAt)
     * @param sortDirection направление сортировки (ASC/DESC)
     * @param role фильтр по роли (опционально)
     * @param search поиск по email, имени (опционально)
     * @param emailVerified фильтр по верификации email (опционально)
     * @return ResponseEntity<Page<AdminUserResponse>> страница с пользователями
     */
    @GetMapping
    @Operation(
        summary = "Получить список пользователей",
        description = "Получение списка всех пользователей с пагинацией, сортировкой и фильтрацией. ",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<AdminUserResponse>> getAllUsers(
        @Parameter(description = "Номер страницы (начиная с 0)")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "Размер страницы")
        @RequestParam(defaultValue = "20") int size,

        @Parameter(description = "Поле для сортировки")
        @RequestParam(defaultValue = "createdAt") String sortBy,

        @Parameter(description = "Направление сортировки (ASC/DESC)")
        @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection,

        @Parameter(description = "Фильтр по роли")
        @RequestParam(required = false) String role,

        @Parameter(description = "Поиск по email, имени")
        @RequestParam(required = false) String search,

        @Parameter(description = "Фильтр по верификации email")
        @RequestParam(required = false) Boolean emailVerified) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<AdminUserResponse> users = adminUserService.getAllUsers(
                pageable, role, search, emailVerified
        );

        return ResponseEntity.ok(users);
    }

    /**
     * Получить информацию о конкретном пользователе по ID.
     *
     * @param userId идентификатор пользователя
     * @return ResponseEntity<AdminUserDetailResponse> детальная информация о пользователе
     */
    @GetMapping("/{userId}")
    @Operation(
        summary = "Получить пользователя по ID",
        description = "Получение детальной информации о пользователе по его идентификатору. ",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<AdminUserDetailResponse> getUserById(
        @Parameter(description = "ID пользователя")
        @PathVariable UUID userId) {
        AdminUserDetailResponse user = adminUserService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Получить информацию о пользователе по email.
     *
     * @param email email пользователя
     * @return ResponseEntity<AdminUserDetailResponse> детальная информация о пользователе
     */
    @GetMapping("/by-email")
    @Operation(
        summary = "Получить пользователя по email",
        description = "Получение детальной информации о пользователе по его email. ",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<AdminUserDetailResponse> getUserByEmail(
        @Parameter(description = "Email пользователя")
        @RequestParam String email) {
        AdminUserDetailResponse user = adminUserService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    /**
     * Создать нового пользователя (администратором).
     *
     * @param request данные для создания пользователя
     * @return ResponseEntity<AdminUserResponse> информация о созданном пользователе
     */
    @PostMapping
    @Operation(
        summary = "Создать пользователя",
        description = "Создание нового пользователя администратором. ",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<AdminUserResponse> createUser(
        @Valid @RequestBody AdminCreateUserRequest request) {
        AdminUserResponse user = adminUserService.createUser(request);
        return ResponseEntity.status(201).body(user);
    }

    /**
     * Обновить информацию о пользователе.
     *
     * @param userId идентификатор пользователя
     * @param request данные для обновления
     * @return ResponseEntity<AdminUserResponse> обновленная информация о пользователе
     */
    @PutMapping("/{userId}")
    @Operation(
        summary = "Обновить пользователя",
        description = "Обновление информации о пользователе администратором. ",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<AdminUserResponse> updateUser(
        @Parameter(description = "ID пользователя")
        @PathVariable UUID userId,

        @Valid @RequestBody AdminUpdateUserRequest request) {
        AdminUserResponse user = adminUserService.updateUser(userId, request);
        return ResponseEntity.ok(user);
    }

    /**
     * Изменить роль пользователя.
     *
     * @param userId идентификатор пользователя
     * @param request новая роль
     * @return ResponseEntity<AdminUserResponse> обновленная информация о пользователе
     */
    @PatchMapping("/{userId}/role")
    @Operation(
        summary = "Изменить роль пользователя",
        description = "Изменение роли пользователя (USER/PARTNER/ADMIN). ",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<AdminUserResponse> changeUserRole(
        @Parameter(description = "ID пользователя")
        @PathVariable UUID userId,

        @Valid @RequestBody AdminChangeRoleRequest request) {
        AdminUserResponse user = adminUserService.changeUserRole(userId, request);
        return ResponseEntity.ok(user);
    }

    /**
     * Изменить статус верификации email пользователя.
     *
     * @param userId идентификатор пользователя
     * @param request статус верификации
     * @return ResponseEntity<AdminUserResponse> обновленная информация о пользователе
     */
    @PatchMapping("/{userId}/email-verification")
    @Operation(
        summary = "Изменить статус верификации email",
        description = "Изменение статуса верификации email пользователя. ",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<AdminUserResponse> changeEmailVerification(
        @Parameter(description = "ID пользователя")
        @PathVariable UUID userId,

        @Valid @RequestBody AdminChangeEmailVerificationRequest request) {
        AdminUserResponse user = adminUserService.changeEmailVerification(userId, request);
        return ResponseEntity.ok(user);
    }

    /**
     * Сбросить пароль пользователя.
     *
     * @param userId идентификатор пользователя
     * @param request новый пароль
     * @return ResponseEntity<MessageResponse> сообщение об успешной операции
     */
    @PatchMapping("/{userId}/reset-password")
    @Operation(
            summary = "Сбросить пароль пользователя",
            description = "Сброс пароля пользователя администратором. ",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> resetUserPassword(
        @Parameter(description = "ID пользователя")
        @PathVariable UUID userId,

        @Valid @RequestBody AdminResetPasswordRequest request) {
        MessageResponse response = adminUserService.resetUserPassword(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Заблокировать/разблокировать пользователя (soft delete).
     *
     * @param userId идентификатор пользователя
     * @param request статус активности
     * @return ResponseEntity<AdminUserResponse> обновленная информация о пользователе
     */
    @PatchMapping("/{userId}/active-status")
    @Operation(
        summary = "Изменить статус активности пользователя",
        description = "Блокировка или разблокировка пользователя. (soft delete) ",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<AdminUserResponse> changeActiveStatus(
        @Parameter(description = "ID пользователя")
        @PathVariable UUID userId,

        @Valid @RequestBody AdminChangeActiveStatusRequest request) {
        AdminUserResponse user = adminUserService.changeActiveStatus(userId, request);
        return ResponseEntity.ok(user);
    }

    /**
     * Удалить пользователя (hard delete).
     *
     * @param userId идентификатор пользователя
     * @return сообщение об успешном удалении
     */
    @DeleteMapping("/{userId}")
    @Operation(
        summary = "Удалить пользователя",
        description = "Полное удаление пользователя из системы (hard delete). ",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> deleteUser(
        @Parameter(description = "ID пользователя")
        @PathVariable UUID userId) {
        MessageResponse response = adminUserService.deleteUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Получить статистику по пользователям.
     *
     * @return ResponseEntity<UserStatisticsResponse> статистика пользователей
     */
    @GetMapping("/statistics")
    @Operation(
        summary = "Получить статистику пользователей",
        description = "Получение общей статистики. ",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserStatisticsResponse> getUserStatistics() {
        UserStatisticsResponse statistics = adminUserService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }
}