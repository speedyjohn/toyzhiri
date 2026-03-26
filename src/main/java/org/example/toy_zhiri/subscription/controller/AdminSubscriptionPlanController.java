package org.example.toy_zhiri.subscription.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.subscription.dto.CreateSubscriptionPlanRequest;
import org.example.toy_zhiri.subscription.dto.SubscriptionPlanResponse;
import org.example.toy_zhiri.subscription.dto.UpdateSubscriptionPlanRequest;
import org.example.toy_zhiri.subscription.service.SubscriptionPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер управления тарифными планами (только для администраторов).
 */
@RestController
@RequestMapping("/api/v1/admin/subscription-plans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Subscription Plans", description = "Управление тарифами (админ)")
public class AdminSubscriptionPlanController {
    private final SubscriptionPlanService planService;

    @GetMapping
    @Operation(
            summary = "Получить все тарифы",
            description = "Получение списка всех тарифов (включая неактивные)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<SubscriptionPlanResponse>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @GetMapping("/{planId}")
    @Operation(
            summary = "Получить тариф по ID",
            description = "Детальная информация о тарифном плане",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<SubscriptionPlanResponse> getPlanById(
            @Parameter(description = "ID тарифа")
            @PathVariable UUID planId) {
        return ResponseEntity.ok(planService.getPlanById(planId));
    }

    @PostMapping
    @Operation(
            summary = "Создать тарифный план",
            description = "Создание нового тарифа для партнёров",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<SubscriptionPlanResponse> createPlan(
            @Valid @RequestBody CreateSubscriptionPlanRequest request) {
        return ResponseEntity.status(201).body(planService.createPlan(request));
    }

    @PutMapping("/{planId}")
    @Operation(
            summary = "Обновить тарифный план",
            description = "Обновление параметров существующего тарифа",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<SubscriptionPlanResponse> updatePlan(
            @Parameter(description = "ID тарифа")
            @PathVariable UUID planId,
            @Valid @RequestBody UpdateSubscriptionPlanRequest request) {
        return ResponseEntity.ok(planService.updatePlan(planId, request));
    }

    @PatchMapping("/{planId}/deactivate")
    @Operation(
            summary = "Деактивировать тариф",
            description = "Скрыть тариф из каталога (не удаляя из БД)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> deactivatePlan(
            @Parameter(description = "ID тарифа")
            @PathVariable UUID planId) {
        return ResponseEntity.ok(planService.deactivatePlan(planId));
    }

    @PatchMapping("/{planId}/activate")
    @Operation(
            summary = "Активировать тариф",
            description = "Сделать тариф снова доступным для покупки",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> activatePlan(
            @Parameter(description = "ID тарифа")
            @PathVariable UUID planId) {
        return ResponseEntity.ok(planService.activatePlan(planId));
    }
}