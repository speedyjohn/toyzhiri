package org.example.toy_zhiri.booking.enums;

/**
 * Статусы бронирования.
 *
 * Жизненный цикл:
 * PENDING_CONFIRMATION → CONFIRMED → PAID → COMPLETED
 * PENDING_CONFIRMATION → REJECTED
 * PENDING_CONFIRMATION / CONFIRMED → CANCELLED
 * PENDING_CONFIRMATION → EXPIRED (партнёр не ответил за 24 часа)
 */
public enum BookingStatus {
    PENDING_CONFIRMATION, // Ожидает подтверждения партнёра
    CONFIRMED,            // Подтверждено партнёром
    REJECTED,             // Отклонено партнёром
    PAID,                 // Оплачено клиентом (резерв для будущей реализации)
    COMPLETED,            // Завершено (резерв для будущей реализации)
    CANCELLED,            // Отменено клиентом
    EXPIRED               // Истекло — партнёр не ответил вовремя
}