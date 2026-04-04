package org.example.toy_zhiri.subscription.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.exception.ConflictException;
import org.example.toy_zhiri.exception.NotFoundException;
import org.example.toy_zhiri.subscription.dto.CreateSubscriptionPlanRequest;
import org.example.toy_zhiri.subscription.dto.SubscriptionPlanResponse;
import org.example.toy_zhiri.subscription.dto.UpdateSubscriptionPlanRequest;
import org.example.toy_zhiri.subscription.entity.SubscriptionPlan;
import org.example.toy_zhiri.subscription.enums.SubscriptionPlanStatus;
import org.example.toy_zhiri.subscription.repository.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервис управления тарифными планами.
 * Используется администратором для создания/редактирования тарифов.
 */
@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {
    private final SubscriptionPlanRepository planRepository;

    /**
     * Получает список всех активных тарифов (для публичного каталога).
     */
    public List<SubscriptionPlanResponse> getActivePlans() {
        return planRepository.findByStatusOrderByDisplayOrderAsc(SubscriptionPlanStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получает все тарифы (для админ-панели).
     */
    public List<SubscriptionPlanResponse> getAllPlans() {
        return planRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получает тариф по ID.
     */
    public SubscriptionPlanResponse getPlanById(UUID planId) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Тариф не найден"));
        return mapToResponse(plan);
    }

    /**
     * Создаёт новый тарифный план.
     */
    @Transactional
    public SubscriptionPlanResponse createPlan(CreateSubscriptionPlanRequest request) {
        if (planRepository.existsBySlug(request.getSlug())) {
            throw new ConflictException("Тариф с таким slug уже существует");
        }

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .isFree(request.getIsFree() != null ? request.getIsFree() : false)
                .status(SubscriptionPlanStatus.ACTIVE)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        return mapToResponse(planRepository.save(plan));
    }

    /**
     * Обновляет существующий тарифный план.
     */
    @Transactional
    public SubscriptionPlanResponse updatePlan(UUID planId, UpdateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Тариф не найден"));

        if (request.getName() != null) {
            plan.setName(request.getName());
        }
        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }
        if (request.getDurationDays() != null) {
            plan.setDurationDays(request.getDurationDays());
        }
        if (request.getIsFree() != null) {
            plan.setIsFree(request.getIsFree());
        }
        if (request.getDisplayOrder() != null) {
            plan.setDisplayOrder(request.getDisplayOrder());
        }

        return mapToResponse(planRepository.save(plan));
    }

    /**
     * Деактивирует тариф (скрывает из каталога).
     */
    @Transactional
    public MessageResponse deactivatePlan(UUID planId) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Тариф не найден"));

        plan.setStatus(SubscriptionPlanStatus.INACTIVE);
        planRepository.save(plan);

        return MessageResponse.builder()
                .message("Тариф «" + plan.getName() + "» деактивирован")
                .build();
    }

    /**
     * Активирует тариф.
     */
    @Transactional
    public MessageResponse activatePlan(UUID planId) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Тариф не найден"));

        plan.setStatus(SubscriptionPlanStatus.ACTIVE);
        planRepository.save(plan);

        return MessageResponse.builder()
                .message("Тариф «" + plan.getName() + "» активирован")
                .build();
    }

    private SubscriptionPlanResponse mapToResponse(SubscriptionPlan plan) {
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