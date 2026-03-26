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

    // Отзывы по услуге (только видимые) — с поддержкой любой сортировки через Pageable
    Page<Review> findByServiceIdAndIsVisibleTrue(UUID serviceId, Pageable pageable);

    // Отзывы по партнёру (только видимые) — с поддержкой любой сортировки через Pageable
    Page<Review> findByPartnerIdAndIsVisibleTrue(UUID partnerId, Pageable pageable);

    // Пересчёт среднего рейтинга по услуге
    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r " +
            "WHERE r.service.id = :serviceId AND r.isVisible = true")
    Optional<Double> findAverageRatingByServiceId(@Param("serviceId") UUID serviceId);

    // Количество видимых отзывов по услуге
    long countByServiceIdAndIsVisibleTrue(UUID serviceId);

    // Отзывы клиента (его личная история)
    Page<Review> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}