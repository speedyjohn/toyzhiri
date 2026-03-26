package org.example.toy_zhiri.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Reviews", description = "Модерация отзывов администратором")
public class AdminReviewController {
    private final ReviewService reviewService;

    @PatchMapping("/{reviewId}/hide")
    @Operation(
            summary = "Скрыть отзыв",
            description = "Скрывает отзыв от публичного просмотра (is_visible = false). " +
                    "Отзыв сохраняется в БД. Рейтинг услуги пересчитывается.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> hideReview(
            @Parameter(description = "ID отзыва")
            @PathVariable UUID reviewId) {

        reviewService.hideReview(reviewId);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Отзыв скрыт")
                .build());
    }

    @PatchMapping("/{reviewId}/show")
    @Operation(
            summary = "Восстановить отзыв",
            description = "Делает ранее скрытый отзыв видимым (is_visible = true). " +
                    "Рейтинг услуги пересчитывается.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> showReview(
            @Parameter(description = "ID отзыва")
            @PathVariable UUID reviewId) {

        reviewService.showReview(reviewId);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Отзыв восстановлен")
                .build());
    }

    @DeleteMapping("/{reviewId}")
    @Operation(
            summary = "Удалить отзыв",
            description = "Полностью удаляет отзыв из БД. Действие необратимо. " +
                    "Рейтинг услуги пересчитывается.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> deleteReview(
            @Parameter(description = "ID отзыва")
            @PathVariable UUID reviewId) {

        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Отзыв удалён")
                .build());
    }
}