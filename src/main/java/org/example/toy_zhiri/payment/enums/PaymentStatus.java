package org.example.toy_zhiri.payment.enums;

public enum PaymentStatus {
    PENDING,    // Ожидает обработки
    SUCCESS,    // Оплата прошла успешно
    FAILED,     // Ошибка оплаты
    REFUNDED    // Возврат средств
}