package org.example.toy_zhiri.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.service.dto.AddToCartRequest;
import org.example.toy_zhiri.service.dto.CartResponse;
import org.example.toy_zhiri.service.service.CartService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "API корзины")
public class CartController {
    private final CartService cartService;
    private final UserService userService;

    @PostMapping
    @Operation(
            summary = "Добавить в корзину",
            description = "Добавить услугу в корзину или обновить существующую",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(cartService.addToCart(userId, request));
    }

    @DeleteMapping("/{serviceId}")
    @Operation(
            summary = "Удалить из корзины",
            description = "Удалить услугу из корзины",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> removeFromCart(
            @PathVariable UUID serviceId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(cartService.removeFromCart(userId, serviceId));
    }

    @DeleteMapping
    @Operation(
            summary = "Очистить корзину",
            description = "Удалить все услуги из корзины",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(cartService.clearCart(userId));
    }

    @GetMapping
    @Operation(
            summary = "Моя корзина",
            description = "Получить содержимое корзины с итоговой суммой",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(cartService.getCart(userId));
    }
}