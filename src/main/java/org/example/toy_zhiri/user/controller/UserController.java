package org.example.toy_zhiri.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.user.dto.ChangePasswordRequest;
import org.example.toy_zhiri.user.dto.DeleteAccountRequest;
import org.example.toy_zhiri.user.dto.UpdateProfileRequest;
import org.example.toy_zhiri.user.dto.UserInfoResponse;
import org.example.toy_zhiri.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


/**
 * Контроллер для управления пользователями.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    /**
     * Возвращает информацию о текущем аутентифицированном пользователе.
     *
     * @param userDetails данные аутентифицированного пользователя
     * @return ResponseEntity<UserInfoResponse> информация о пользователе
     */
    @Operation(
        summary = "Мой профиль",
        description = "Получить информацию о моем аккаунте",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserInfoResponse response = userService.getUserInfoByEmail(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Обновляет профиль текущего пользователя.
     *
     * @param userDetails данные аутентифицированного пользователя
     * @param request данные для обновления
     * @return обновленная информация о пользователе
     */
    @Operation(
        summary = "Редактировать профиль",
        description = "Обновить информацию о своем профиле (имя, фамилия, телефон, город)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/me")
    public ResponseEntity<UserInfoResponse> updateMyProfile(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody UpdateProfileRequest request) {
        UserInfoResponse response = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Удалить профиль",
        description = "Удалить свой профиль (soft delete)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PatchMapping("/me")
    public ResponseEntity<MessageResponse> deleteMyProfile(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody DeleteAccountRequest request) {
        MessageResponse response = userService.deleteAccount(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Изменяет пароль пользователя.
     *
     * @param userDetails данные аутентифицированного пользователя
     * @param request данные для смены пароля
     * @return ResponseEntity<MessageResponse> сообщение об успехе
     */
    @Operation(
        summary = "Сменить пароль",
        description = "Изменить свой пароль",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/me/password")
    public ResponseEntity<MessageResponse> changePassword(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody ChangePasswordRequest request) {
        MessageResponse response = userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }
}
