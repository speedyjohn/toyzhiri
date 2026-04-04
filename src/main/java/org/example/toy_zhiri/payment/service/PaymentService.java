package org.example.toy_zhiri.payment.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.toy_zhiri.exception.AccessDeniedException;
import org.example.toy_zhiri.exception.InvalidStateException;
import org.example.toy_zhiri.exception.NotFoundException;
import org.example.toy_zhiri.notification.enums.NotificationType;
import org.example.toy_zhiri.notification.enums.RelatedEntityType;
import org.example.toy_zhiri.notification.service.NotificationService;
import org.example.toy_zhiri.payment.dto.PaymentRequest;
import org.example.toy_zhiri.payment.dto.PaymentResponse;
import org.example.toy_zhiri.payment.enums.PaymentMethod;
import org.example.toy_zhiri.payment.enums.PaymentStatus;
import org.example.toy_zhiri.partner.entity.Partner;
import org.example.toy_zhiri.partner.repository.PartnerRepository;
import org.example.toy_zhiri.subscription.entity.Subscription;
import org.example.toy_zhiri.subscription.enums.SubscriptionStatus;
import org.example.toy_zhiri.subscription.repository.SubscriptionRepository;
import org.example.toy_zhiri.subscription.service.SubscriptionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сервис обработки платежей.
 * Содержит заглушки для трёх способов оплаты (Kaspi, банковская карта, Google/Apple Pay).
 * В будущем каждый метод будет интегрирован с реальным платёжным шлюзом.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final PartnerRepository partnerRepository;
    private final NotificationService notificationService;

    /**
     * Обрабатывает оплату подписки.
     * Маршрутизирует на соответствующий метод оплаты.
     */
    @Transactional
    public PaymentResponse processPayment(UUID userId, PaymentRequest request) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Партнёр не найден"));

        Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new NotFoundException("Подписка не найдена"));

        if (!subscription.getPartner().getId().equals(partner.getId())) {
            throw new AccessDeniedException("Подписка не принадлежит вам");
        }

        if (subscription.getStatus() != SubscriptionStatus.PENDING) {
            throw new InvalidStateException("Подписка не ожидает оплаты");
        }

        PaymentResponse response = switch (request.getPaymentMethod()) {
            case KASPI -> processKaspiPayment(subscription);
            case BANK_CARD -> processBankCardPayment(subscription);
            case GOOGLE_PAY, APPLE_PAY -> processDigitalWalletPayment(subscription, request.getPaymentMethod());
        };

        // Уведомление партнёру об успешной оплате
        Subscription activated = subscriptionRepository.findById(subscription.getId()).orElse(subscription);
        notificationService.send(
                userId,
                NotificationType.PAYMENT_SUCCESS,
                "Оплата прошла успешно",
                "Оплата тарифа «" + subscription.getPlan().getName() + "» прошла успешно. " +
                        "Подписка активна до " + activated.getExpiresAt().toLocalDate(),
                RelatedEntityType.SUBSCRIPTION,
                subscription.getId()
        );

        return response;
    }

    /**
     * Заглушка: оплата через Kaspi Pay.
     * TODO: Интегрировать с Kaspi Pay API (QR, Kaspi Gold/Red).
     */
    private PaymentResponse processKaspiPayment(Subscription subscription) {
        log.info("Kaspi Pay — обработка оплаты для подписки {}", subscription.getId());

        String paymentId = "KASPI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        subscription.setPaymentMethod(PaymentMethod.KASPI);
        subscription.setPaymentId(paymentId);
        subscriptionRepository.save(subscription);

        subscriptionService.activateSubscription(subscription.getId());

        return PaymentResponse.builder()
                .paymentId(UUID.randomUUID())
                .subscriptionId(subscription.getId())
                .paymentMethod(PaymentMethod.KASPI.name())
                .paymentStatus(PaymentStatus.SUCCESS.name())
                .amount(subscription.getPlan().getPrice())
                .message("Оплата через Kaspi Pay прошла успешно")
                .processedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Заглушка: оплата банковской картой (Visa, MasterCard, UnionPay).
     * TODO: Интегрировать с платёжным шлюзом (Epay, PayBox и т.д.).
     */
    private PaymentResponse processBankCardPayment(Subscription subscription) {
        log.info("Bank Card — обработка оплаты для подписки {}", subscription.getId());

        String paymentId = "CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        subscription.setPaymentMethod(PaymentMethod.BANK_CARD);
        subscription.setPaymentId(paymentId);
        subscriptionRepository.save(subscription);

        subscriptionService.activateSubscription(subscription.getId());

        return PaymentResponse.builder()
                .paymentId(UUID.randomUUID())
                .subscriptionId(subscription.getId())
                .paymentMethod(PaymentMethod.BANK_CARD.name())
                .paymentStatus(PaymentStatus.SUCCESS.name())
                .amount(subscription.getPlan().getPrice())
                .message("Оплата банковской картой прошла успешно")
                .processedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Заглушка: оплата через Google Pay / Apple Pay.
     * TODO: Интегрировать с Google Pay API и Apple Pay API.
     */
    private PaymentResponse processDigitalWalletPayment(Subscription subscription, PaymentMethod method) {
        String methodName = method == PaymentMethod.GOOGLE_PAY ? "Google Pay" : "Apple Pay";
        log.info("{} — обработка оплаты для подписки {}", methodName, subscription.getId());

        String prefix = method == PaymentMethod.GOOGLE_PAY ? "GPAY-" : "APAY-";
        String paymentId = prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        subscription.setPaymentMethod(method);
        subscription.setPaymentId(paymentId);
        subscriptionRepository.save(subscription);

        subscriptionService.activateSubscription(subscription.getId());

        return PaymentResponse.builder()
                .paymentId(UUID.randomUUID())
                .subscriptionId(subscription.getId())
                .paymentMethod(method.name())
                .paymentStatus(PaymentStatus.SUCCESS.name())
                .amount(subscription.getPlan().getPrice())
                .message("Оплата через " + methodName + " прошла успешно")
                .processedAt(LocalDateTime.now())
                .build();
    }
}