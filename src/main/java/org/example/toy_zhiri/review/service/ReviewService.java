package org.example.toy_zhiri.review.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.booking.entity.Booking;
import org.example.toy_zhiri.booking.enums.BookingStatus;
import org.example.toy_zhiri.booking.repository.BookingRepository;
import org.example.toy_zhiri.review.dto.CreateReviewRequest;
import org.example.toy_zhiri.review.dto.ReviewResponse;
import org.example.toy_zhiri.review.dto.UpdateReviewRequest;
import org.example.toy_zhiri.review.entity.Review;
import org.example.toy_zhiri.review.repository.ReviewRepository;
import org.example.toy_zhiri.service.entity.Service;
import org.example.toy_zhiri.service.repository.ServiceRepository;
import org.example.toy_zhiri.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private static final long EDIT_WINDOW_HOURS = 24;


    /**
     * Клиент оставляет отзыв.
     * Условия:
     *  - бронирование должно принадлежать клиенту
     *  - статус бронирования — COMPLETED
     *  - отзыв на это бронирование ещё не оставлен
     */
    @Transactional
    public ReviewResponse createReview(UUID userId, CreateReviewRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("У вас нет доступа к этому бронированию");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new RuntimeException(
                    "Оставить отзыв можно только после завершения сделки (статус COMPLETED). " +
                            "Текущий статус: " + booking.getStatus()
            );
        }

        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new RuntimeException("Вы уже оставили отзыв по этому бронированию");
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

        return mapToResponse(saved);
    }

    /**
     * Публичный список отзывов по услуге (только видимые, с пагинацией).
     */
    public Page<ReviewResponse> getServiceReviews(UUID serviceId, Pageable pageable) {
        serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

        return reviewRepository
                .findByServiceIdAndIsVisibleTrueOrderByCreatedAtDesc(serviceId, pageable)
                .map(this::mapToResponse);
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
     * Пересчитывает rating и reviews_count в таблице services
     * на основе актуальных видимых отзывов.
     */
    private void recalculateServiceRating(UUID serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

        double avg = reviewRepository.findAverageRatingByServiceId(serviceId)
                .orElse(0.0);
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

    @Transactional
    public ReviewResponse updateReview(UUID userId, UUID reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));

        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException("У вас нет доступа к этому отзыву");
        }

        if (review.getCreatedAt().plusHours(EDIT_WINDOW_HOURS).isBefore(LocalDateTime.now())) {
            throw new RuntimeException(
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
}