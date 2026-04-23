package org.example.toy_zhiri.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.service.dto.CreateServiceVariantRequest;
import org.example.toy_zhiri.service.dto.ServiceVariantResponse;
import org.example.toy_zhiri.service.dto.UpdateServiceVariantRequest;
import org.example.toy_zhiri.service.service.ServiceVariantService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер управления вариантами услуг партнёром.
 * Партнёр получает доступ только к вариантам своих услуг.
 */
@RestController
@RequestMapping("/api/v1/partner/services/{serviceId}/variants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARTNER')")
@Tag(name = "Partner Service Variants", description = "API партнёра для управления вариантами услуг")
public class PartnerServiceVariantController {

    private final ServiceVariantService variantService;
    private final UserService userService;

    /**
     * Возвращает все варианты услуги партнёра (включая неактивные).
     *
     * @param userDetails данные текущего пользователя
     * @param serviceId   идентификатор услуги
     * @return ResponseEntity<List<ServiceVariantResponse>> список вариантов
     */
    @GetMapping
    @Operation(
            summary = "Список вариантов услуги",
            description = "Возвращает все варианты услуги, включая неактивные",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<ServiceVariantResponse>> list(
            @AuthenticationPrincipal UserDetails userDetails,

            @Parameter(description = "ID услуги")
            @PathVariable UUID serviceId) {
        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(variantService.listForPartner(userId, serviceId));
    }

    /**
     * Создаёт новый вариант услуги.
     * Значения атрибутов валидируются по схеме категории.
     *
     * @param userDetails данные текущего пользователя
     * @param serviceId   идентификатор услуги
     * @param request     данные для создания
     * @return ResponseEntity<ServiceVariantResponse> созданный вариант
     */
    @PostMapping
    @Operation(
            summary = "Создать вариант услуги",
            description = "Создаёт вариант услуги (зал ресторана, модель авто и т.п.)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ServiceVariantResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,

            @Parameter(description = "ID услуги")
            @PathVariable UUID serviceId,

            @Valid @RequestBody CreateServiceVariantRequest request) {
        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.status(201).body(variantService.create(userId, serviceId, request));
    }

    /**
     * Обновляет вариант услуги. Все поля опциональны.
     *
     * @param userDetails данные текущего пользователя
     * @param serviceId   идентификатор услуги
     * @param variantId   идентификатор варианта
     * @param request     данные для обновления
     * @return ResponseEntity<ServiceVariantResponse> обновлённый вариант
     */
    @PutMapping("/{variantId}")
    @Operation(
            summary = "Обновить вариант услуги",
            description = "Обновляет поля варианта. При изменении атрибутов — повторная валидация",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ServiceVariantResponse> update(
            @AuthenticationPrincipal UserDetails userDetails,

            @Parameter(description = "ID услуги")
            @PathVariable UUID serviceId,

            @Parameter(description = "ID варианта")
            @PathVariable UUID variantId,

            @Valid @RequestBody UpdateServiceVariantRequest request) {
        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(variantService.update(userId, serviceId, variantId, request));
    }

    /**
     * Удаляет вариант услуги.
     * Если на вариант есть активные бронирования — удаление запрещено.
     *
     * @param userDetails данные текущего пользователя
     * @param serviceId   идентификатор услуги
     * @param variantId   идентификатор варианта
     * @return ResponseEntity<Void> пустой ответ
     */
    @DeleteMapping("/{variantId}")
    @Operation(
            summary = "Удалить вариант услуги",
            description = "Удаляет вариант. Запрещено при наличии активных бронирований",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,

            @Parameter(description = "ID услуги")
            @PathVariable UUID serviceId,

            @Parameter(description = "ID варианта")
            @PathVariable UUID variantId) {
        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        variantService.delete(userId, serviceId, variantId);
        return ResponseEntity.noContent().build();
    }
}