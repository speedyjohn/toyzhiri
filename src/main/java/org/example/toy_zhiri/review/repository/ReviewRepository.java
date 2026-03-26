package org.example.toy_zhiri.review.repository;

import org.example.toy_zhiri.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByBookingId(UUID bookingId);

    Optional<Review> findByBookingIdAndUserId(UUID bookingId, UUID userId);

    // Публичные отзывы по услуге (только видимые)
    Page<Review> findByServiceIdAndIsVisibleTrueOrderByCreatedAtDesc(UUID serviceId, Pageable pageable);

    // Пересчёт среднего рейтинга по услуге
    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r " +
            "WHERE r.service.id = :serviceId AND r.isVisible = true")
    Optional<Double> findAverageRatingByServiceId(@Param("serviceId") UUID serviceId);

    // Количество видимых отзывов по услуге
    long countByServiceIdAndIsVisibleTrue(UUID serviceId);

    // Отзывы клиента
    Page<Review> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}