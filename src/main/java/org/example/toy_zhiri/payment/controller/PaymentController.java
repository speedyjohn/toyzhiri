package org.example.toy_zhiri.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.payment.dto.PaymentRequest;
import org.example.toy_zhiri.payment.dto.PaymentResponse;
import org.example.toy_zhiri.payment.service.PaymentService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Контроллер оплаты подписок.
 * Маршрутизирует платежи на соответствующий метод (Kaspi, карта, Google/Apple Pay).
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARTNER')")
@Tag(name = "Payments", description = "Оплата подписок")
public class PaymentController {
    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping
    @Operation(
            summary = "Оплатить подписку",
            description = "Обрабатывает оплату подписки выбранным методом. " +
                    "Поддерживаемые методы: KASPI, BANK_CARD, GOOGLE_PAY, APPLE_PAY. " +
                    "На данный момент — заглушка: оплата всегда проходит успешно.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(paymentService.processPayment(userId, request));
    }
}