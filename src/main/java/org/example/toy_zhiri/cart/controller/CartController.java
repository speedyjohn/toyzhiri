package org.example.toy_zhiri.cart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.cart.dto.AddToCartRequest;
import org.example.toy_zhiri.cart.dto.CartResponse;
import org.example.toy_zhiri.cart.service.CartService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Контроллер для работы с корзиной пользователя.
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "API корзины")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    /**
     * Добавляет услугу в корзину или обновляет существующий элемент.
     *
     * @param request     данные о добавляемой услуге
     * @param userDetails данные аутентифицированного пользователя
     * @return ResponseEntity<MessageResponse> сообщение об успешном добавлении
     */
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

    /**
     * Удаляет конкретный элемент корзины по его ID.
     *
     * @param cartItemId  идентификатор элемента корзины
     * @param userDetails данные аутентифицированного пользователя
     * @return ResponseEntity<MessageResponse> сообщение об успешном удалении
     */
    @DeleteMapping("/item/{cartItemId}")
    @Operation(
            summary = "Удалить элемент из корзины по ID элемента",
            description = "Удалить конкретный элемент корзины по его cartItemId",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> removeCartItem(
            @PathVariable UUID cartItemId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(cartService.removeCartItem(userId, cartItemId));
    }

    /**
     * Удаляет услугу из корзины по ID услуги.
     *
     * @param serviceId   идентификатор услуги
     * @param userDetails данные аутентифицированного пользователя
     * @return ResponseEntity<MessageResponse> сообщение об успешном удалении
     */
    @DeleteMapping("/{serviceId}")
    @Operation(
            summary = "Удалить из корзины по ID услуги",
            description = "Удалить услугу из корзины по serviceId",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> removeFromCart(
            @PathVariable UUID serviceId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(cartService.removeFromCart(userId, serviceId));
    }

    /**
     * Полностью очищает корзину текущего пользователя.
     *
     * @param userDetails данные аутентифицированного пользователя
     * @return ResponseEntity<MessageResponse> сообщение об успешной очистке
     */
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

    /**
     * Возвращает содержимое корзины текущего пользователя с итоговой суммой.
     *
     * @param userDetails данные аутентифицированного пользователя
     * @return ResponseEntity<CartResponse> содержимое корзины
     */
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