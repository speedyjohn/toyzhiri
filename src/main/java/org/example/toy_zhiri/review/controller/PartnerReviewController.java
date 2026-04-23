package org.example.toy_zhiri.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.review.dto.ReviewResponse;
import org.example.toy_zhiri.review.dto.ReviewSummaryResponse;
import org.example.toy_zhiri.review.enums.ReviewSortType;
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

/**
 * Контроллер для просмотра отзывов партнёром.
 * Партнёр может просматривать отзывы только к своим услугам.
 */
@RestController
@RequestMapping("/api/v1/partner/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARTNER')")
@Tag(name = "Partner Reviews", description = "Просмотр отзывов партнёром")
public class PartnerReviewController {
    private final ReviewService reviewService;
    private final UserService userService;

    /**
     * Возвращает отзывы к конкретной услуге текущего партнёра.
     *
     * @param serviceId   идентификатор услуги
     * @param sort        тип сортировки (NEW, BEST, WORST)
     * @param page        номер страницы
     * @param size        размер страницы
     * @param userDetails данные аутентифицированного пользователя
     * @return ResponseEntity<Page<ReviewResponse>> страница с отзывами
     */
    @GetMapping("/service/{serviceId}")
    @Operation(
            summary = "Отзывы по моей услуге",
            description = "Партнёр просматривает отзывы к своей услуге с сортировкой. " +
                    "Доступны только видимые отзывы.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<ReviewResponse>> getServiceReviews(
            @PathVariable UUID serviceId,

            @Parameter(description = "Сортировка: NEW, BEST, WORST")
            @RequestParam(defaultValue = "NEW") ReviewSortType sort,

            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reviewService.getPartnerServiceReviews(userId, serviceId, sort, pageable));
    }

    /**
     * Возвращает сводку рейтинга по услуге текущего партнёра.
     *
     * @param serviceId   идентификатор услуги
     * @param userDetails данные аутентифицированного пользователя
     * @return ResponseEntity<ReviewSummaryResponse> средний рейтинг и количество отзывов
     */
    @GetMapping("/service/{serviceId}/summary")
    @Operation(
            summary = "Сводка рейтинга по моей услуге",
            description = "Возвращает средний рейтинг и общее количество отзывов по услуге партнёра.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReviewSummaryResponse> getServiceReviewSummary(
            @PathVariable UUID serviceId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(reviewService.getPartnerServiceReviewSummary(userId, serviceId));
    }
}