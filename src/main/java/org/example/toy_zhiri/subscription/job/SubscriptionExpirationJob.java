package org.example.toy_zhiri.subscription.job;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.toy_zhiri.notification.enums.NotificationType;
import org.example.toy_zhiri.notification.enums.RelatedEntityType;
import org.example.toy_zhiri.notification.service.NotificationService;
import org.example.toy_zhiri.service.entity.Service;
import org.example.toy_zhiri.service.repository.ServiceRepository;
import org.example.toy_zhiri.subscription.entity.Subscription;
import org.example.toy_zhiri.subscription.enums.SubscriptionStatus;
import org.example.toy_zhiri.subscription.repository.SubscriptionRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Плановое задание для обработки истекающих и истекших подписок.
 *
 * Цепочка статусов:
 *   ACTIVE → EXPIRING_7_DAYS → EXPIRING_3_DAYS → EXPIRING_SOON → EXPIRED
 *
 * Каждый переход происходит ровно один раз — дублирования уведомлений нет.
 * На каждом шаге партнёр может оформить новую подписку заранее.
 * При истечении услуга деактивируется только если новой активной подписки нет.
 *
 * Запускается при старте сервера и затем каждые 30 минут.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpirationJob {

    private static final int WARN_7_DAYS  = 7;
    private static final int WARN_3_DAYS  = 3;
    private static final int WARN_1_DAY   = 1;

    private final SubscriptionRepository subscriptionRepository;
    private final ServiceRepository serviceRepository;
    private final NotificationService notificationService;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void processSubscriptions() {
        warnAt7Days();
        warnAt3Days();
        warnAt1Day();
        expireOverdue();
    }

    /**
     * ACTIVE → EXPIRING_7_DAYS.
     * Срабатывает когда до истечения осталось менее 7 дней.
     */
    private void warnAt7Days() {
        processWarning(
                SubscriptionStatus.ACTIVE,
                SubscriptionStatus.EXPIRING_7_DAYS,
                WARN_7_DAYS,
                "Подписка истекает через 7 дней",
                "Подписка на услугу «%s» истекает %s. " +
                        "У вас есть 7 дней, чтобы оформить продление."
        );
    }

    /**
     * EXPIRING_7_DAYS → EXPIRING_3_DAYS.
     * Срабатывает когда до истечения осталось менее 3 дней.
     */
    private void warnAt3Days() {
        processWarning(
                SubscriptionStatus.EXPIRING_7_DAYS,
                SubscriptionStatus.EXPIRING_3_DAYS,
                WARN_3_DAYS,
                "Подписка истекает через 3 дня",
                "Подписка на услугу «%s» истекает %s. " +
                        "Осталось 3 дня — оформите продление, чтобы объявление не пропало."
        );
    }

    /**
     * EXPIRING_3_DAYS → EXPIRING_SOON.
     * Срабатывает когда до истечения осталось менее 24 часов.
     */
    private void warnAt1Day() {
        processWarning(
                SubscriptionStatus.EXPIRING_3_DAYS,
                SubscriptionStatus.EXPIRING_SOON,
                WARN_1_DAY,
                "Подписка истекает через 24 часа",
                "Подписка на услугу «%s» истекает %s. " +
                        "Оформите новую подписку прямо сейчас, чтобы объявление оставалось активным."
        );
    }

    /**
     * EXPIRING_SOON → EXPIRED.
     * Деактивирует услугу если партнёр не успел оформить новую активную подписку.
     */
    private void expireOverdue() {
        List<Subscription> overdueSubscriptions = subscriptionRepository.findByStatusAndExpiresAtBefore(
                SubscriptionStatus.EXPIRING_SOON,
                LocalDateTime.now()
        );

        if (overdueSubscriptions.isEmpty()) {
            return;
        }

        log.info("SubscriptionExpirationJob: {} подписок переходят в EXPIRED", overdueSubscriptions.size());

        for (Subscription subscription : overdueSubscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);

            Service service = subscription.getService();

            boolean hasActiveSubscription = subscriptionRepository
                    .existsByServiceIdAndStatus(service.getId(), SubscriptionStatus.ACTIVE);

            if (!hasActiveSubscription) {
                service.setIsActive(false);
                serviceRepository.save(service);

                notificationService.send(
                        subscription.getPartner().getUser().getId(),
                        NotificationType.SYSTEM,
                        "Подписка истекла",
                        "Подписка на услугу «" + service.getName() + "» истекла. " +
                                "Услуга деактивирована — оформите новую подписку для возобновления публикации.",
                        RelatedEntityType.SUBSCRIPTION,
                        subscription.getId()
                );

                log.info("Подписка {} истекла, услуга {} деактивирована (партнёр: {})",
                        subscription.getId(),
                        service.getName(),
                        subscription.getPartner().getCompanyName());
            } else {
                log.info("Подписка {} истекла, услуга {} уже имеет новую активную подписку — деактивация пропущена",
                        subscription.getId(),
                        service.getName());
            }
        }
    }

    /**
     * Универсальный метод для фаз предупреждения.
     * Ищет подписки с fromStatus, у которых expiresAt попадает в диапазон [now, now + thresholdDays],
     * переводит их в toStatus и отправляет уведомление партнёру.
     */
    private void processWarning(SubscriptionStatus fromStatus,
                                SubscriptionStatus toStatus,
                                int thresholdDays,
                                String notificationTitle,
                                String notificationMessageTemplate) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(thresholdDays);

        List<Subscription> subscriptions = subscriptionRepository.findByStatusAndExpiresAtBetween(
                fromStatus,
                now,
                threshold
        );

        if (subscriptions.isEmpty()) {
            return;
        }

        log.info("SubscriptionExpirationJob: {} подписок переходят из {} в {}",
                subscriptions.size(), fromStatus, toStatus);

        for (Subscription subscription : subscriptions) {
            subscription.setStatus(toStatus);
            subscriptionRepository.save(subscription);

            String message = String.format(
                    notificationMessageTemplate,
                    subscription.getService().getName(),
                    subscription.getExpiresAt().toLocalDate()
            );

            notificationService.send(
                    subscription.getPartner().getUser().getId(),
                    NotificationType.SYSTEM,
                    notificationTitle,
                    message,
                    RelatedEntityType.SUBSCRIPTION,
                    subscription.getId()
            );

            log.info("Подписка {} переведена в {} (услуга: {}, истекает: {})",
                    subscription.getId(),
                    toStatus,
                    subscription.getService().getName(),
                    subscription.getExpiresAt());
        }
    }
}