package org.example.toy_zhiri.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Заглушка для SMS-уведомлений.
 * Вместо реальной отправки логирует сообщение в консоль.
 * В будущем — интеграция с SMS-шлюзом (Mobizon, SMS.kz, Twilio и т.д.).
 */
@Service
@Slf4j
public class SmsNotificationService {

    /**
     * «Отправляет» SMS — логирует в консоль.
     *
     * @param phone   номер телефона получателя
     * @param title   заголовок уведомления
     * @param message текст сообщения
     */
    public void send(String phone, String title, String message) {
        log.info("=== SMS УВЕДОМЛЕНИЕ ===");
        log.info("Кому: +{}", phone);
        log.info("Тема: {}", title);
        log.info("Текст: {}", message);
        log.info("=======================");
    }
}