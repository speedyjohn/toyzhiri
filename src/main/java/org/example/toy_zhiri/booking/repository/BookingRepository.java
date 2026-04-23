package org.example.toy_zhiri.booking.repository;

import org.example.toy_zhiri.booking.entity.Booking;
import org.example.toy_zhiri.booking.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    // Клиентские запросы

    Page<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Booking> findByUserIdAndStatusOrderByCreatedAtDesc(
            UUID userId, BookingStatus status, Pageable pageable
    );

    /**
     * История бронирований клиента с расширенной фильтрацией.
     * Поддерживает фильтрацию по статусу, категории услуги, диапазону дат создания и мероприятия.
     */
    @Query("SELECT b FROM Booking b " +
            "JOIN b.service s " +
            "JOIN s.category c " +
            "WHERE b.user.id = :userId " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (:categoryId IS NULL OR c.id = :categoryId) " +
            "AND (:createdFrom IS NULL OR CAST(b.createdAt AS date) >= :createdFrom) " +
            "AND (:createdTo IS NULL OR CAST(b.createdAt AS date) <= :createdTo) " +
            "AND (:eventFrom IS NULL OR b.eventDate >= :eventFrom) " +
            "AND (:eventTo IS NULL OR b.eventDate <= :eventTo) " +
            "ORDER BY b.createdAt DESC")
    Page<Booking> findByUserIdWithFilters(
            @Param("userId") UUID userId,
            @Param("status") BookingStatus status,
            @Param("categoryId") UUID categoryId,
            @Param("createdFrom") LocalDate createdFrom,
            @Param("createdTo") LocalDate createdTo,
            @Param("eventFrom") LocalDate eventFrom,
            @Param("eventTo") LocalDate eventTo,
            Pageable pageable
    );

    // Партнёрские запросы

    Page<Booking> findByPartnerIdOrderByCreatedAtDesc(UUID partnerId, Pageable pageable);

    Page<Booking> findByPartnerIdAndStatusOrderByCreatedAtDesc(
            UUID partnerId, BookingStatus status, Pageable pageable
    );

    List<Booking> findByPartnerIdAndEventDateBetweenOrderByEventDateAsc(
            UUID partnerId, LocalDate from, LocalDate to
    );

    // Проверка конфликтов по дате.
    // PAID убран — активными считаются только PENDING_CONFIRMATION и CONFIRMED.

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.service.id = :serviceId " +
            "AND b.eventDate = :date " +
            "AND b.status IN ('PENDING_CONFIRMATION', 'CONFIRMED')")
    boolean existsActiveBookingForServiceOnDate(
            @Param("serviceId") UUID serviceId,
            @Param("date") LocalDate date
    );

    // Истекшие бронирования (для scheduled job)

    List<Booking> findByStatusAndExpiresAtBefore(
            BookingStatus status, LocalDateTime now
    );

    /**
     * Возвращает занятые даты для услуги за период.
     * Занятыми считаются даты с активными бронированиями (PENDING_CONFIRMATION, CONFIRMED).
     */
    @Query("SELECT b.eventDate FROM Booking b " +
            "WHERE b.service.id = :serviceId " +
            "AND b.eventDate BETWEEN :from AND :to " +
            "AND b.status IN ('PENDING_CONFIRMATION', 'CONFIRMED')")
    List<LocalDate> findBookedDatesByServiceIdAndPeriod(
            @Param("serviceId") UUID serviceId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
            
                SELECT COUNT(b) > 0 FROM Booking b
            WHERE b.variant.id = :variantId
              AND b.eventDate = :date
              AND b.status IN (
                  org.example.toy_zhiri.booking.enums.BookingStatus.PENDING_CONFIRMATION,
                  org.example.toy_zhiri.booking.enums.BookingStatus.CONFIRMED
              )
            """)
    boolean existsActiveBookingForVariantOnDate(
            @Param("variantId") UUID variantId,
            @Param("date") LocalDate date);
}