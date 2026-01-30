package org.example.toy_zhiri.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.service.dto.ServiceResponse;
import org.example.toy_zhiri.service.service.FavoriteService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "API избранных услуг")
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final UserService userService;

    @PostMapping("/{serviceId}")
    @Operation(
        summary = "Добавить в избранное",
        description = "Добавить услугу в список избранных",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> addToFavorites(
            @PathVariable UUID serviceId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(favoriteService.addToFavorites(userId, serviceId));
    }

    @DeleteMapping("/{serviceId}")
    @Operation(
        summary = "Удалить из избранного",
        description = "Удалить услугу из списка избранных",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> removeFromFavorites(
            @PathVariable UUID serviceId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(favoriteService.removeFromFavorites(userId, serviceId));
    }

    @GetMapping
    @Operation(
        summary = "Мои избранные",
        description = "Получить список избранных услуг",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<ServiceResponse>> getFavorites(
            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(favoriteService.getFavorites(userId, pageable));
    }
}