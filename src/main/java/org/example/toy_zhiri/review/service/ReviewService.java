package org.example.toy_zhiri.review.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.booking.entity.Booking;
import org.example.toy_zhiri.booking.enums.BookingStatus;
import org.example.toy_zhiri.booking.repository.BookingRepository;
import org.example.toy_zhiri.exception.AccessDeniedException;
import org.example.toy_zhiri.exception.ConflictException;
import org.example.toy_zhiri.exception.InvalidStateException;
import org.example.toy_zhiri.exception.NotFoundException;
import org.example.toy_zhiri.notification.enums.NotificationType;
import org.example.toy_zhiri.notification.enums.RelatedEntityType;
import org.example.toy_zhiri.notification.service.NotificationService;
import org.example.toy_zhiri.partner.entity.Partner;
import org.example.toy_zhiri.partner.repository.PartnerRepository;
import org.example.toy_zhiri.review.dto.CreateReviewRequest;
import org.example.toy_zhiri.review.dto.ReviewResponse;
import org.example.toy_zhiri.review.dto.ReviewSummaryResponse;
import org.example.toy_zhiri.review.dto.UpdateReviewRequest;
import org.example.toy_zhiri.review.entity.Review;
import org.example.toy_zhiri.review.enums.ReviewSortType;
import org.example.toy_zhiri.review.repository.ReviewRepository;
import org.example.toy_zhiri.service.entity.Service;
import org.example.toy_zhiri.service.repository.ServiceRepository;
import org.example.toy_zhiri.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReviewService {
    private static final long EDIT_WINDOW_HOURS = 24;

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final NotificationService notificationService;
    private final PartnerRepository partnerRepository;

    /**
     * Клиент оставляет отзыв.
     * Условия:
     * - бронирование должно принадлежать клиенту
     * - статус бронирования — COMPLETED
     * - отзыв на это бронирование ещё не оставлен
     */
    @Transactional
    public ReviewResponse createReview(UUID userId, CreateReviewRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("У вас нет доступа к этому бронированию");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new InvalidStateException(
                    "Оставить отзыв можно только после завершения сделки (статус COMPLETED). " +
                            "Текущий статус: " + booking.getStatus()
            );
        }

        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new ConflictException("Вы уже оставили отзыв по этому бронированию");
        }

        Review review = Review.builder()
                .booking(booking)
                .user(booking.getUser())
                .service(booking.getService())
                .partner(booking.getPartner())
                .rating(request.getRating())
                .comment(request.getComment())
                .imageUrls(request.getImageUrls())
                .isVisible(true)
                .build();

        Review saved = reviewRepository.save(review);

        recalculateServiceRating(booking.getService().getId());

        // Уведомление партнёру о новом отзыве
        notificationService.send(
                booking.getPartner().getUser().getId(),
                NotificationType.REVIEW_RECEIVED,
                "Новый отзыв",
                "Новый отзыв (★" + request.getRating() + ") от " +
                        booking.getUser().getFullName() + " на услугу " +
                        booking.getService().getName(),
                RelatedEntityType.REVIEW,
                saved.getId()
        );

        return mapToResponse(saved);
    }

    /**
     * Клиент редактирует свой отзыв.
     * Редактирование доступно только в течение {@value EDIT_WINDOW_HOURS} часов после публикации.
     */
    @Transactional
    public ReviewResponse updateReview(UUID userId, UUID reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));

        if (!review.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("У вас нет доступа к этому отзыву");
        }

        if (review.getCreatedAt().plusHours(EDIT_WINDOW_HOURS).isBefore(LocalDateTime.now())) {
            throw new InvalidStateException(
                    "Редактирование отзыва доступно только в течение " + EDIT_WINDOW_HOURS +
                            " часов после публикации"
            );
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setImageUrls(request.getImageUrls());

        Review updated = reviewRepository.save(review);

        recalculateServiceRating(updated.getService().getId());

        return mapToResponse(updated);
    }

    /**
     * Публичный список отзывов по услуге с поддержкой сортировки.
     * Видимы только отзывы с is_visible = true.
     */
    public Page<ReviewResponse> getServiceReviews(UUID serviceId, ReviewSortType sortType, Pageable pageable) {
        serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Услуга не найдена"));

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                buildSort(sortType)
        );

        return reviewRepository
                .findByServiceIdAndIsVisibleTrue(serviceId, sortedPageable)
                .map(this::mapToResponse);
    }

    /**
     * Публичный список отзывов по партнёру с поддержкой сортировки.
     * Агрегирует отзывы по всем услугам партнёра.
     */
    public Page<ReviewResponse> getPartnerReviews(UUID partnerId, ReviewSortType sortType, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                buildSort(sortType)
        );

        return reviewRepository
                .findByPartnerIdAndIsVisibleTrue(partnerId, sortedPageable)
                .map(this::mapToResponse);
    }

    /**
     * Возвращает сводку рейтинга по услуге: средний рейтинг и количество отзывов.
     */
    public ReviewSummaryResponse getServiceReviewSummary(UUID serviceId) {
        serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Услуга не найдена"));

        double avg = reviewRepository.findAverageRatingByServiceId(serviceId).orElse(0.0);
        long count = reviewRepository.countByServiceIdAndIsVisibleTrue(serviceId);

        return ReviewSummaryResponse.builder()
                .averageRating(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP))
                .totalReviews(count)
                .build();
    }

    /**
     * Список отзывов клиента (его личная история).
     */
    public Page<ReviewResponse> getMyReviews(UUID userId, Pageable pageable) {
        return reviewRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Администратор скрывает отзыв (is_visible = false).
     * Отзыв остаётся в БД, но не отображается публично.
     */
    @Transactional
    public void hideReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));

        review.setIsVisible(false);
        reviewRepository.save(review);

        recalculateServiceRating(review.getService().getId());
    }

    /**
     * Администратор восстанавливает скрытый отзыв (is_visible = true).
     */
    @Transactional
    public void showReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));

        review.setIsVisible(true);
        reviewRepository.save(review);

        recalculateServiceRating(review.getService().getId());
    }

    /**
     * Администратор полностью удаляет отзыв из БД.
     */
    @Transactional
    public void deleteReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));

        UUID serviceId = review.getService().getId();

        reviewRepository.delete(review);

        recalculateServiceRating(serviceId);
    }

    /**
     * Возвращает отзывы по услуге партнёра с проверкой владения.
     * Только владелец услуги может вызвать этот метод.
     */
    public Page<ReviewResponse> getPartnerServiceReviews(UUID userId, UUID serviceId,
                                                         ReviewSortType sortType, Pageable pageable) {
        assertServiceOwnership(userId, serviceId);
        return getServiceReviews(serviceId, sortType, pageable);
    }

    /**
     * Возвращает сводку рейтинга по услуге партнёра с проверкой владения.
     */
    public ReviewSummaryResponse getPartnerServiceReviewSummary(UUID userId, UUID serviceId) {
        assertServiceOwnership(userId, serviceId);
        return getServiceReviewSummary(serviceId);
    }

    /**
     * Проверяет, что услуга принадлежит партнёру текущего пользователя.
     */
    private void assertServiceOwnership(UUID userId, UUID serviceId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Партнёр не найден"));

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Услуга не найдена"));

        if (!service.getPartner().getId().equals(partner.getId())) {
            throw new AccessDeniedException("У вас нет доступа к этой услуге");
        }
    }

    /**
     * Строит Sort на основе типа сортировки отзывов.
     */
    private Sort buildSort(ReviewSortType sortType) {
        if (sortType == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sortType) {
            case BEST -> Sort.by(Sort.Direction.DESC, "rating");
            case WORST -> Sort.by(Sort.Direction.ASC, "rating");
            case NEW -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    /**
     * Пересчитывает rating и reviews_count в таблице services
     * на основе актуальных видимых отзывов.
     */
    private void recalculateServiceRating(UUID serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

        double avg = reviewRepository.findAverageRatingByServiceId(serviceId).orElse(0.0);
        long count = reviewRepository.countByServiceIdAndIsVisibleTrue(serviceId);

        service.setRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        service.setReviewsCount((int) count);

        serviceRepository.save(service);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBooking().getId())
                .userId(review.getUser().getId())
                .userFullName(review.getUser().getFullName())
                .serviceId(review.getService().getId())
                .serviceName(review.getService().getName())
                .partnerId(review.getPartner().getId())
                .partnerCompanyName(review.getPartner().getCompanyName())
                .rating(review.getRating())
                .comment(review.getComment())
                .imageUrls(review.getImageUrls())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}