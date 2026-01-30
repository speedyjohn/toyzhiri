package org.example.toy_zhiri.partner.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.partner.dto.PartnerProfileResponse;
import org.example.toy_zhiri.partner.dto.PartnerProfileUpdateRequest;
import org.example.toy_zhiri.partner.entity.Partner;
import org.example.toy_zhiri.partner.enums.PartnerStatus;
import org.example.toy_zhiri.partner.repository.PartnerRepository;
import org.example.toy_zhiri.service.entity.Service;
import org.example.toy_zhiri.service.repository.ServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервис для работы со справочником партнеров.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class PartnerDirectoryService {
    private final PartnerRepository partnerRepository;
    private final ServiceRepository serviceRepository;

    /**
     * Получить полный профиль партнера по ID.
     *
     * @param partnerId идентификатор партнера
     * @return детальная информация о партнере
     */
    public PartnerProfileResponse getPartnerProfile(UUID partnerId) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Партнер не найден"));

        return mapToProfileResponse(partner);
    }

    /**
     * Получить профиль партнера по userId.
     *
     * @param userId идентификатор пользователя
     * @return детальная информация о партнере
     */
    public PartnerProfileResponse getPartnerProfileByUserId(UUID userId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Партнер не найден"));

        return mapToProfileResponse(partner);
    }

    /**
     * Получить список всех партнеров с фильтрацией.
     *
     * @param pageable параметры пагинации
     * @param city фильтр по городу
     * @param status фильтр по статусу
     * @param search поиск по названию компании
     * @return страница с партнерами
     */
    public Page<PartnerProfileResponse> getAllPartners(
            Pageable pageable,
            String city,
            PartnerStatus status,
            String search) {

        Specification<Partner> spec = Specification.where(null);

        // Фильтр по городу
        if (city != null && !city.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("city")), city.toLowerCase()));
        }

        // Фильтр по статусу
        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        // Поиск по названию компании
        if (search != null && !search.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("companyName")), "%" + search.toLowerCase() + "%"));
        }

        Page<Partner> partners = partnerRepository.findAll(spec, pageable);
        return partners.map(this::mapToProfileResponse);
    }

    /**
     * Обновить профиль партнера (частичное обновление).
     *
     * @param userId идентификатор пользователя
     * @param request данные для обновления
     * @return обновленная информация о партнере
     */
    @Transactional
    public PartnerProfileResponse updatePartnerProfile(UUID userId, PartnerProfileUpdateRequest request) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Партнер не найден"));

        // Обновляем только переданные поля
        if (request.getCompanyName() != null) {
            partner.setCompanyName(request.getCompanyName());
        }

        if (request.getDescription() != null) {
            partner.setDescription(request.getDescription());
        }

        if (request.getAddress() != null) {
            partner.setAddress(request.getAddress());
        }

        if (request.getCity() != null) {
            partner.setCity(request.getCity());
        }

        if (request.getRegion() != null) {
            partner.setRegion(request.getRegion());
        }

        if (request.getPhone() != null) {
            partner.setPhone(request.getPhone());
        }

        if (request.getEmail() != null) {
            partner.setEmail(request.getEmail());
        }

        if (request.getWhatsapp() != null) {
            partner.setWhatsapp(request.getWhatsapp());
        }

        if (request.getTelegram() != null) {
            partner.setTelegram(request.getTelegram());
        }

        if (request.getInstagram() != null) {
            partner.setInstagram(request.getInstagram());
        }

        if (request.getWebsite() != null) {
            partner.setWebsite(request.getWebsite());
        }

        if (request.getLogoUrl() != null) {
            partner.setLogoUrl(request.getLogoUrl());
        }

        Partner updated = partnerRepository.save(partner);
        return mapToProfileResponse(updated);
    }

    /**
     * Проверить, заполнен ли профиль партнера полностью.
     *
     * @param userId идентификатор пользователя
     * @return true если все обязательные поля заполнены
     */
    public boolean isProfileComplete(UUID userId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Партнер не найден"));

        return partner.getCompanyName() != null &&
                partner.getCity() != null &&
                partner.getPhone() != null &&
                partner.getEmail() != null;
    }

    /**
     * Преобразует сущность Partner в детальный DTO.
     */
    private PartnerProfileResponse mapToProfileResponse(Partner partner) {
        // Получаем все услуги партнера
        List<Service> services = serviceRepository.findByPartnerId(partner.getId());

        // Вычисляем средний рейтинг
        BigDecimal averageRating = services.stream()
                .filter(s -> s.getRating() != null)
                .map(Service::getRating)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!services.isEmpty()) {
            averageRating = averageRating.divide(
                    BigDecimal.valueOf(services.size()),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        // Подсчитываем общее количество отзывов
        Integer totalReviews = services.stream()
                .filter(s -> s.getReviewsCount() != null)
                .mapToInt(Service::getReviewsCount)
                .sum();

        // Получаем количество активных услуг
        long activeServices = services.stream()
                .filter(s -> s.getIsActive() && s.getIsApproved())
                .count();

        // Получаем уникальные категории бизнеса
        List<String> businessCategories = services.stream()
                .map(s -> s.getCategory().getNameRu())
                .distinct()
                .collect(Collectors.toList());

        return PartnerProfileResponse.builder()
                .id(partner.getId())
                .userId(partner.getUser().getId())
                .bin(partner.getBin())
                .companyName(partner.getCompanyName())
                .description(partner.getDescription())
                .address(partner.getAddress())
                .city(partner.getCity())
                .region(partner.getRegion())
                .phone(partner.getPhone())
                .email(partner.getEmail())
                .whatsapp(partner.getWhatsapp())
                .telegram(partner.getTelegram())
                .instagram(partner.getInstagram())
                .website(partner.getWebsite())
                .logoUrl(partner.getLogoUrl())
                .status(partner.getStatus().name())
                .rejectionReason(partner.getRejectionReason())
                .createdAt(partner.getCreatedAt())
                .approvedAt(partner.getApprovedAt())
                .approvedByEmail(partner.getApprovedBy() != null ?
                        partner.getApprovedBy().getEmail() : null)
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .totalServices(services.size())
                .activeServices((int) activeServices)
                .businessCategories(businessCategories)
                .ownerFullName(partner.getUser().getFullName())
                .ownerEmail(partner.getUser().getEmail())
                .ownerPhone(partner.getUser().getPhone())
                .build();
    }
}