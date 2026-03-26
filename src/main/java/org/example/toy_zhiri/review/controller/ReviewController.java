package org.example.toy_zhiri.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.review.dto.CreateReviewRequest;
import org.example.toy_zhiri.review.dto.ReviewResponse;
import org.example.toy_zhiri.review.dto.UpdateReviewRequest;
import org.example.toy_zhiri.review.service.ReviewService;
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

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Отзывы и рейтинги услуг")
public class ReviewController {
    private final ReviewService reviewService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Оставить отзыв",
            description = "Клиент оставляет отзыв после завершения сделки (статус бронирования COMPLETED). " +
                    "Один отзыв на одно бронирование. " +
                    "Фото загружаются заранее через POST /api/v1/files/upload, " +
                    "полученные URL передаются в поле imageUrls.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.status(201).body(reviewService.createReview(userId, request));
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Редактировать отзыв",
            description = "Клиент может изменить текст, оценку и фото своего отзыва " +
                    "в течение 24 часов после публикации. После истечения окна редактирование недоступно.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(reviewService.updateReview(userId, reviewId, request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Мои отзывы",
            description = "История отзывов, оставленных текущим клиентом.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<ReviewResponse>> getMyReviews(
            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reviewService.getMyReviews(userId, pageable));
    }

    @GetMapping("/service/{serviceId}")
    @Operation(
            summary = "Отзывы по услуге",
            description = "Публичный список видимых отзывов по конкретной услуге. Доступен без авторизации."
    )
    public ResponseEntity<Page<ReviewResponse>> getServiceReviews(
            @PathVariable UUID serviceId,

            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reviewService.getServiceReviews(serviceId, pageable));
    }
}