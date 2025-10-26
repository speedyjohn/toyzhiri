package org.example.toy_zhiri.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.user.dto.UserInfoResponse;
import org.example.toy_zhiri.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
     * @return UserInfoResponse информация о пользователе
     */
    @Operation(
            summary = "Информация",
            description = "Получить информацию о моем аккаунте",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/me")
    public UserInfoResponse register(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getUserInfoByEmail(userDetails.getUsername());
    }
}
