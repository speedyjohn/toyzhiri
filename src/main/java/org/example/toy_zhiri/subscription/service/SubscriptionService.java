package org.example.toy_zhiri.subscription.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.exception.*;
import org.example.toy_zhiri.partner.entity.Partner;
import org.example.toy_zhiri.partner.repository.PartnerRepository;
import org.example.toy_zhiri.service.entity.Service;
import org.example.toy_zhiri.service.repository.ServiceRepository;
import org.example.toy_zhiri.subscription.dto.SubscriptionPlanResponse;
import org.example.toy_zhiri.subscription.dto.SubscriptionResponse;
import org.example.toy_zhiri.subscription.entity.Subscription;
import org.example.toy_zhiri.subscription.entity.SubscriptionPlan;
import org.example.toy_zhiri.subscription.enums.SubscriptionPlanStatus;
import org.example.toy_zhiri.subscription.enums.SubscriptionStatus;
import org.example.toy_zhiri.subscription.repository.SubscriptionPlanRepository;
import org.example.toy_zhiri.subscription.repository.SubscriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сервис управления подписками.
 * Подписка привязывается к конкретной услуге (объявлению) партнёра.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final PartnerRepository partnerRepository;
    private final ServiceRepository serviceRepository;

    /**
     * Создаёт подписку на тарифный план для конкретной услуги.
     * Если тариф бесплатный — подписка активируется сразу.
     * Если платный — статус PENDING до оплаты.
     *
     * @param userId  ID пользователя-партнёра
     * @param serviceId ID услуги, на которую оформляется подписка
     * @param planId    ID тарифного плана
     */
    @Transactional
    public SubscriptionResponse subscribe(UUID userId, UUID serviceId, UUID planId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Партнёр не найден"));

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Услуга не найдена"));

        // Проверяем, что услуга принадлежит этому партнёру
        if (!service.getPartner().getId().equals(partner.getId())) {
            throw new AccessDeniedException("Услуга не принадлежит вам");
        }

        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Тариф не найден"));

        if (plan.getStatus() != SubscriptionPlanStatus.ACTIVE) {
            throw new BadRequestException("Тариф недоступен");
        }

        // Проверяем, нет ли уже активной подписки на эту услугу
        if (subscriptionRepository.existsByServiceIdAndStatus(serviceId, SubscriptionStatus.ACTIVE)) {
            throw new ConflictException("У этой услуги уже есть активная подписка");
        }

        Subscription subscription = Subscription.builder()
                .partner(partner)
                .service(service)
                .plan(plan)
                .build();

        if (plan.getIsFree()) {
            // Бесплатный тариф — активируем сразу
            LocalDateTime now = LocalDateTime.now();
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setStartsAt(now);
            subscription.setExpiresAt(now.plusDays(plan.getDurationDays()));
            subscription.setPaidAmount(plan.getPrice());
        } else {
            // Платный тариф — ожидаем оплату
            subscription.setStatus(SubscriptionStatus.PENDING);
        }

        return mapToResponse(subscriptionRepository.save(subscription));
    }

    /**
     * Получает активную подписку для конкретной услуги.
     */
    public SubscriptionResponse getActiveSubscriptionByService(UUID userId, UUID serviceId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Партнёр не найден"));

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Услуга не найдена"));

        if (!service.getPartner().getId().equals(partner.getId())) {
            throw new AccessDeniedException("Услуга не принадлежит вам");
        }

        Subscription subscription = subscriptionRepository
                .findByServiceIdAndStatus(serviceId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Активная подписка для этой услуги не найдена"));

        return mapToResponse(subscription);
    }

    /**
     * Получает историю всех подписок партнёра (по всем услугам).
     */
    public Page<SubscriptionResponse> getSubscriptionHistory(UUID userId, Pageable pageable) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Партнёр не найден"));

        return subscriptionRepository
                .findByPartnerIdOrderByCreatedAtDesc(partner.getId(), pageable)
                .map(this::mapToResponse);
    }

    /**
     * Активирует подписку после успешной оплаты (вызывается из PaymentService).
     */
    @Transactional
    public Subscription activateSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new NotFoundException("Подписка не найдена"));

        if (subscription.getStatus() != SubscriptionStatus.PENDING) {
            throw new InvalidStateException("Подписка не ожидает оплаты");
        }

        LocalDateTime now = LocalDateTime.now();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartsAt(now);
        subscription.setExpiresAt(now.plusDays(subscription.getPlan().getDurationDays()));
        subscription.setPaidAmount(subscription.getPlan().getPrice());

        return subscriptionRepository.save(subscription);
    }

    /**
     * Отменяет подписку в статусе PENDING.
     */
    @Transactional
    public SubscriptionResponse cancelSubscription(UUID userId, UUID subscriptionId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Партнёр не найден"));

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new NotFoundException("Подписка не найдена"));

        if (!subscription.getPartner().getId().equals(partner.getId())) {
            throw new AccessDeniedException("Подписка не принадлежит вам");
        }

        if (subscription.getStatus() != SubscriptionStatus.PENDING) {
            throw new InvalidStateException("Можно отменить только подписку в статусе ожидания");
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        return mapToResponse(subscriptionRepository.save(subscription));
    }

    private SubscriptionResponse mapToResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .partnerId(subscription.getPartner().getId())
                .partnerCompanyName(subscription.getPartner().getCompanyName())
                .serviceId(subscription.getService().getId())
                .serviceName(subscription.getService().getName())
                .plan(mapPlanToResponse(subscription.getPlan()))
                .status(subscription.getStatus().name())
                .startsAt(subscription.getStartsAt())
                .expiresAt(subscription.getExpiresAt())
                .paidAmount(subscription.getPaidAmount())
                .paymentMethod(subscription.getPaymentMethod() != null
                        ? subscription.getPaymentMethod().name() : null)
                .paymentId(subscription.getPaymentId())
                .createdAt(subscription.getCreatedAt())
                .build();
    }

    private SubscriptionPlanResponse mapPlanToResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .slug(plan.getSlug())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .durationDays(plan.getDurationDays())
                .isFree(plan.getIsFree())
                .status(plan.getStatus().name())
                .displayOrder(plan.getDisplayOrder())
                .build();
    }
}