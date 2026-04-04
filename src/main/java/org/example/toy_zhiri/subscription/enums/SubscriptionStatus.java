package org.example.toy_zhiri.subscription.enums;

public enum SubscriptionStatus {
    PENDING,           // Ожидает оплаты
    ACTIVE,            // Подписка активна
    EXPIRING_7_DAYS,   // Истекает через 7 дней — первое предупреждение
    EXPIRING_3_DAYS,   // Истекает через 3 дня — второе предупреждение
    EXPIRING_SOON,     // Истекает через 24 часа — финальное предупреждение, можно продлить
    EXPIRED,           // Подписка истекла, услуга деактивирована
    CANCELLED          // Подписка отменена
}