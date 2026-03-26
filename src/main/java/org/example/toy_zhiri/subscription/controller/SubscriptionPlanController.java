package org.example.toy_zhiri.subscription.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.subscription.dto.SubscriptionPlanResponse;
import org.example.toy_zhiri.subscription.service.SubscriptionPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Публичный контроллер для просмотра доступных тарифных планов.
 */
@RestController
@RequestMapping("/api/v1/subscription-plans")
@RequiredArgsConstructor
@Tag(name = "Subscription Plans", description = "Просмотр доступных тарифов")
public class SubscriptionPlanController {
    private final SubscriptionPlanService planService;

    @GetMapping
    @Operation(
            summary = "Получить доступные тарифы",
            description = "Список всех активных тарифных планов для партнёров"
    )
    public ResponseEntity<List<SubscriptionPlanResponse>> getActivePlans() {
        return ResponseEntity.ok(planService.getActivePlans());
    }
}