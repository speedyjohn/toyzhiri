package org.example.toy_zhiri.subscription.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.subscription.dto.SubscriptionResponse;
import org.example.toy_zhiri.subscription.service.SubscriptionService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Контроллер управления подписками для партнёров.
 * Подписка оформляется на конкретную услугу (объявление).
 */
@RestController
@RequestMapping("/api/v1/partner/subscriptions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARTNER')")
@Tag(name = "Partner Subscriptions", description = "Управление подписками на услуги")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    private final UserService userService;

    @PostMapping("/services/{serviceId}/plans/{planId}")
    @Operation(
            summary = "Подписать услугу на тариф",
            description = "Создаёт подписку для конкретной услуги на выбранный тарифный план. " +
                    "Бесплатный тариф активируется сразу, платный — после оплаты.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<SubscriptionResponse> subscribe(
            @Parameter(description = "ID услуги")
            @PathVariable UUID serviceId,

            @Parameter(description = "ID тарифного плана")
            @PathVariable UUID planId,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.status(201).body(subscriptionService.subscribe(userId, serviceId, planId));
    }

    @GetMapping("/services/{serviceId}/active")
    @Operation(
            summary = "Текущая подписка услуги",
            description = "Получить информацию об активной подписке для конкретной услуги",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<SubscriptionResponse> getActiveSubscription(
            @Parameter(description = "ID услуги")
            @PathVariable UUID serviceId,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(subscriptionService.getActiveSubscriptionByService(userId, serviceId));
    }

    @GetMapping("/history")
    @Operation(
            summary = "История подписок",
            description = "Получить историю всех подписок партнёра по всем услугам",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<SubscriptionResponse>> getSubscriptionHistory(
            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(subscriptionService.getSubscriptionHistory(userId, pageable));
    }

    @PostMapping("/{subscriptionId}/cancel")
    @Operation(
            summary = "Отменить подписку",
            description = "Отменить подписку в статусе ожидания оплаты",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            @Parameter(description = "ID подписки")
            @PathVariable UUID subscriptionId,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(subscriptionService.cancelSubscription(userId, subscriptionId));
    }
}