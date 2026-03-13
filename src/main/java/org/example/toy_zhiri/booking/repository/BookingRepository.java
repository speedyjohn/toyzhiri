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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    // --- Клиентские запросы ---

    Page<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Booking> findByUserIdAndStatusOrderByCreatedAtDesc(
            UUID userId, BookingStatus status, Pageable pageable
    );

    // --- Партнёрские запросы ---

    Page<Booking> findByPartnerIdOrderByCreatedAtDesc(UUID partnerId, Pageable pageable);

    Page<Booking> findByPartnerIdAndStatusOrderByCreatedAtDesc(
            UUID partnerId, BookingStatus status, Pageable pageable
    );

    List<Booking> findByPartnerIdAndEventDateBetweenOrderByEventDateAsc(
            UUID partnerId, LocalDate from, LocalDate to
    );

    // --- Проверка конфликтов по дате ---

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.service.id = :serviceId " +
            "AND b.eventDate = :date " +
            "AND b.status IN ('PENDING_CONFIRMATION', 'CONFIRMED', 'PAID')")
    boolean existsActiveBookingForServiceOnDate(
            @Param("serviceId") UUID serviceId,
            @Param("date") LocalDate date
    );

    // --- Истекшие бронирования (для scheduled job) ---

    List<Booking> findByStatusAndExpiresAtBefore(
            BookingStatus status, LocalDateTime now
    );

    /**
     * Возвращает занятые даты для услуги за период.
     * Занятыми считаются даты с активными бронированиями.
     */
    @Query("SELECT b.eventDate FROM Booking b " +
            "WHERE b.service.id = :serviceId " +
            "AND b.eventDate BETWEEN :from AND :to " +
            "AND b.status IN ('PENDING_CONFIRMATION', 'CONFIRMED', 'PAID')")
    List<LocalDate> findBookedDatesByServiceIdAndPeriod(
            @Param("serviceId") UUID serviceId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}