package org.example.toy_zhiri.notification.enums;

/**
 * Типы уведомлений в системе.
 *
 * Группы:
 * - BOOKING_*     — события, связанные с бронированием
 * - SERVICE_*     — модерация услуг
 * - REVIEW_*      — отзывы
 * - PAYMENT_*     — оплата
 * - PROMOTION     — акции и спецпредложения
 * - EVENT_REMINDER — напоминание о предстоящем мероприятии
 * - SYSTEM        — системные уведомления (смена пароля, деактивация и т.д.)
 */
public enum NotificationType {
    BOOKING_CREATED,                // Новое бронирование (для партнёра)
    BOOKING_CONFIRMED,              // Бронирование подтверждено (для клиента)
    BOOKING_REJECTED,               // Бронирование отклонено (для клиента)
    BOOKING_CANCELLED,              // Бронирование отменено (для партнёра)
    BOOKING_COMPLETED,              // Бронирование завершено (для обоих)
    BOOKING_EXPIRED,                // Бронирование истекло (для клиента)
    BOOKING_COMPLETION_CONFIRMED,   // Одна сторона подтвердила завершение (для другой стороны)
    SERVICE_APPROVED,               // Услуга одобрена модератором (для партнёра)
    SERVICE_REJECTED,               // Услуга отклонена модератором (для партнёра)
    REVIEW_RECEIVED,                // Получен новый отзыв (для партнёра)
    PASSWORD_CHANGED,               // Пароль изменён (для пользователя)
    ACCOUNT_DEACTIVATED,            // Аккаунт деактивирован (для пользователя)
    PAYMENT_SUCCESS,                // Оплата прошла успешно (для партнёра)
    PROMOTION,                      // Акции и спецпредложения
    EVENT_REMINDER,                 // Напоминание о мероприятии (для клиента)
    SYSTEM,                         // Системные уведомления
    NEW_MESSAGE                     // Новое сообщение в чате
}