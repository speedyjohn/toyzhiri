package org.example.toy_zhiri.booking.job;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.toy_zhiri.booking.entity.Booking;
import org.example.toy_zhiri.booking.enums.BookingStatus;
import org.example.toy_zhiri.booking.repository.BookingRepository;
import org.example.toy_zhiri.notification.enums.NotificationType;
import org.example.toy_zhiri.notification.enums.RelatedEntityType;
import org.example.toy_zhiri.notification.service.NotificationService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Плановое задание для обработки просроченных бронирований.
 *
 * Каждые 30 минут проверяет бронирования со статусом PENDING_CONFIRMATION,
 * у которых истёк срок ожидания ответа от партнёра (expires_at < now).
 * Такие бронирования переводятся в статус EXPIRED, после чего
 * клиент и партнёр получают соответствующие уведомления.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpirationJob {

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void expireOverdueBookings() {
        List<Booking> overdueBookings = bookingRepository.findByStatusAndExpiresAtBefore(
                BookingStatus.PENDING_CONFIRMATION,
                LocalDateTime.now()
        );

        if (overdueBookings.isEmpty()) {
            return;
        }

        log.info("BookingExpirationJob: найдено {} просроченных бронирований", overdueBookings.size());

        for (Booking booking : overdueBookings) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);

            String serviceName = booking.getService().getName();
            String eventDate = booking.getEventDate().toString();

            // Уведомление клиенту
            notificationService.send(
                    booking.getUser().getId(),
                    NotificationType.BOOKING_EXPIRED,
                    "Бронирование истекло",
                    "Партнёр не ответил в течение 24 часов. " +
                            "Бронирование отменено — " + serviceName + ", " + eventDate,
                    RelatedEntityType.BOOKING,
                    booking.getId()
            );

            // Уведомление партнёру
            notificationService.send(
                    booking.getPartner().getUser().getId(),
                    NotificationType.BOOKING_EXPIRED,
                    "Запрос на бронирование истёк",
                    "Запрос от " + booking.getUser().getFullName() + " на " +
                            serviceName + " (" + eventDate + ") был автоматически отменён " +
                            "из-за отсутствия ответа",
                    RelatedEntityType.BOOKING,
                    booking.getId()
            );

            log.info("Бронирование {} переведено в EXPIRED (услуга: {}, клиент: {})",
                    booking.getId(), serviceName, booking.getUser().getFullName());
        }

        log.info("BookingExpirationJob: обработано {} бронирований", overdueBookings.size());
    }
}