package org.example.toy_zhiri.partner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.auth.service.AuthService;
import org.example.toy_zhiri.partner.dto.PartnerRegistrationRequest;
import org.example.toy_zhiri.partner.dto.PartnerResponse;
import org.example.toy_zhiri.partner.service.PartnerService;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для управления заявками на партнерство.
 */
@RestController
@RequestMapping("/api/v1/partner")
@RequiredArgsConstructor
@Tag(name = "Partner", description = "API для работы с партнерами")
public class PartnerController {
    private final PartnerService partnerService;
    private final UserService userService;

    /**
     * Подает заявку пользователя на статус партнера.
     *
     * @param userDetails данные аутентифицированного пользователя
     * @param request данные для регистрации партнера
     * @return PartnerResponse информация о созданной заявке
     */
    @PostMapping("/register")
    @Operation(
        summary = "Подать заявку на партнерство",
        description = "Зарегистрированный пользователь может подать заявку на статус партнера",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PartnerResponse> registerAsPartner(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PartnerRegistrationRequest request) {
        User user = userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        PartnerResponse response = partnerService.registerPartner(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Возвращает информацию по заявке текущего пользователя на партнерство.
     *
     * @param userDetails данные аутентифицированного пользователя
     * @return PartnerResponse информация о заявке пользователя
     */
    @GetMapping("/my-application")
    @Operation(
        summary = "Получить свою заявку на партнерство",
        description = "Просмотр статуса своей заявки",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PartnerResponse> getMyApplication(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmailOrThrow(userDetails.getUsername());
        PartnerResponse response = partnerService.getPartnerByUserId(user.getId());
        return ResponseEntity.ok(response);
    }

}