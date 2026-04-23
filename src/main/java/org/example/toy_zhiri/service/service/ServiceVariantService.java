package org.example.toy_zhiri.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.toy_zhiri.attribute.entity.CategoryAttribute;
import org.example.toy_zhiri.attribute.repository.CategoryAttributeRepository;
import org.example.toy_zhiri.attribute.service.AttributeSchemaService;
import org.example.toy_zhiri.exception.AccessDeniedException;
import org.example.toy_zhiri.exception.InvalidStateException;
import org.example.toy_zhiri.exception.NotFoundException;
import org.example.toy_zhiri.partner.entity.Partner;
import org.example.toy_zhiri.partner.repository.PartnerRepository;
import org.example.toy_zhiri.service.dto.CreateServiceVariantRequest;
import org.example.toy_zhiri.service.dto.ServiceVariantResponse;
import org.example.toy_zhiri.service.dto.UpdateServiceVariantRequest;
import org.example.toy_zhiri.service.entity.Service;
import org.example.toy_zhiri.service.entity.ServiceVariant;
import org.example.toy_zhiri.service.entity.ServiceVariantImage;
import org.example.toy_zhiri.service.repository.ServiceRepository;
import org.example.toy_zhiri.service.repository.ServiceVariantRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис управления вариантами услуг (залы ресторана, модели авто, пакеты и т.п.).
 * Партнёр создаёт и редактирует варианты; клиент запрашивает подходящие варианты
 * с учётом своих чекбоксов при бронировании.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceVariantService {

    private final ServiceVariantRepository variantRepository;
    private final ServiceRepository serviceRepository;
    private final PartnerRepository partnerRepository;
    private final AttributeSchemaService attributeSchemaService;
    private final CategoryAttributeRepository categoryAttributeRepository;

    /**
     * Возвращает все варианты услуги для партнёра-владельца (включая неактивные).
     */
    public List<ServiceVariantResponse> listForPartner(UUID userId, UUID serviceId) {
        Service service = findServiceOwnedByUser(userId, serviceId);
        return variantRepository.findByServiceIdOrderBySortOrderAsc(service.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Возвращает активные варианты услуги для клиента, опционально отфильтрованные
     * по клиентским чекбоксам.
     *
     * @param serviceId     идентификатор услуги
     * @param clientFilters фильтры клиента (ключ — логический ключ атрибута); может быть null
     * @return список подходящих активных вариантов
     */
    public List<ServiceVariantResponse> listForClient(UUID serviceId, Map<String, Object> clientFilters) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Услуга не найдена"));

        List<ServiceVariant> variants = variantRepository
                .findByServiceIdAndIsActiveTrueOrderBySortOrderAsc(service.getId());

        if (clientFilters == null || clientFilters.isEmpty()) {
            return variants.stream().map(this::mapToResponse).toList();
        }

        List<CategoryAttribute> schema = categoryAttributeRepository
                .findByCategoryIdOrderBySortOrderAsc(service.getCategory().getId());

        return variants.stream()
                .filter(v -> attributeSchemaService.matchesFilters(
                        v.getAttributes(), clientFilters, schema))
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Создаёт вариант услуги.
     * Валидирует значения атрибутов по схеме категории.
     */
    @Transactional
    public ServiceVariantResponse create(UUID userId, UUID serviceId, CreateServiceVariantRequest request) {
        Service service = findServiceOwnedByUser(userId, serviceId);

        attributeSchemaService.validateVariantAttributes(
                service.getCategory().getId(),
                request.getAttributes()
        );

        ServiceVariant variant = ServiceVariant.builder()
                .service(service)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .attributes(request.getAttributes())
                .isActive(true)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .images(new ArrayList<>())
                .build();

        ServiceVariant saved = variantRepository.save(variant);
        applyImages(saved, request.getImageUrls());

        log.info("Партнёр {} создал вариант {} для услуги {}",
                userId, saved.getId(), serviceId);
        return mapToResponse(saved);
    }

    /**
     * Обновляет вариант услуги.
     * При обновлении атрибутов повторно валидирует по схеме.
     */
    @Transactional
    public ServiceVariantResponse update(UUID userId,
                                         UUID serviceId,
                                         UUID variantId,
                                         UpdateServiceVariantRequest request) {
        Service service = findServiceOwnedByUser(userId, serviceId);
        ServiceVariant variant = findVariantOfService(service.getId(), variantId);

        if (request.getName() != null) {
            variant.setName(request.getName());
        }
        if (request.getDescription() != null) {
            variant.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            variant.setPrice(request.getPrice());
        }
        if (request.getSortOrder() != null) {
            variant.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            variant.setIsActive(request.getIsActive());
        }
        if (request.getAttributes() != null) {
            attributeSchemaService.validateVariantAttributes(
                    service.getCategory().getId(),
                    request.getAttributes()
            );
            variant.setAttributes(request.getAttributes());
        }

        if (request.getImageUrls() != null) {
            variant.getImages().clear();
            applyImages(variant, request.getImageUrls());
        }

        ServiceVariant saved = variantRepository.save(variant);
        log.info("Партнёр {} обновил вариант {} услуги {}", userId, variantId, serviceId);
        return mapToResponse(saved);
    }

    /**
     * Удаляет вариант услуги.
     * Если на вариант есть активные бронирования — удаление запрещено,
     * партнёр может только деактивировать вариант через update(isActive=false).
     */
    @Transactional
    public void delete(UUID userId, UUID serviceId, UUID variantId) {
        Service service = findServiceOwnedByUser(userId, serviceId);
        ServiceVariant variant = findVariantOfService(service.getId(), variantId);

        if (variantRepository.hasActiveBookings(variantId)) {
            throw new InvalidStateException(
                    "Нельзя удалить вариант с активными бронированиями. " +
                            "Деактивируйте вариант вместо удаления");
        }

        variantRepository.delete(variant);
        log.info("Партнёр {} удалил вариант {} услуги {}", userId, variantId, serviceId);
    }

    /**
     * Возвращает вариант по ID для внутреннего использования (например, в BookingService).
     */
    public ServiceVariant findByIdOrThrow(UUID variantId) {
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException(
                        "Вариант услуги с ID " + variantId + " не найден"));
    }

    /**
     * Проверяет, существуют ли вообще варианты у услуги (в т.ч. неактивные).
     */
    public boolean serviceHasVariants(UUID serviceId) {
        return variantRepository.existsByServiceId(serviceId);
    }

    private Service findServiceOwnedByUser(UUID userId, UUID serviceId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Профиль партнёра не найден"));

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Услуга не найдена"));

        if (!service.getPartner().getId().equals(partner.getId())) {
            throw new AccessDeniedException("У вас нет доступа к этой услуге");
        }

        return service;
    }

    private ServiceVariant findVariantOfService(UUID serviceId, UUID variantId) {
        return variantRepository.findByIdAndServiceId(variantId, serviceId)
                .orElseThrow(() -> new NotFoundException(
                        "Вариант услуги не найден или не принадлежит услуге"));
    }

    /**
     * Применяет список URL-ов изображений к варианту.
     * Первое изображение помечается как is_primary, порядок берётся из позиции в списке.
     */
    private void applyImages(ServiceVariant variant, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        for (int i = 0; i < imageUrls.size(); i++) {
            ServiceVariantImage image = ServiceVariantImage.builder()
                    .variant(variant)
                    .imageUrl(imageUrls.get(i))
                    .displayOrder(i)
                    .isPrimary(i == 0)
                    .build();
            variant.getImages().add(image);
        }
    }

    private ServiceVariantResponse mapToResponse(ServiceVariant variant) {
        List<String> imageUrls = variant.getImages() == null
                ? Collections.emptyList()
                : variant.getImages().stream()
                .sorted(Comparator.comparingInt(img -> img.getDisplayOrder() != null
                        ? img.getDisplayOrder() : 0))
                .map(ServiceVariantImage::getImageUrl)
                .toList();

        return ServiceVariantResponse.builder()
                .id(variant.getId())
                .serviceId(variant.getService().getId())
                .name(variant.getName())
                .description(variant.getDescription())
                .price(variant.getPrice())
                .attributes(variant.getAttributes())
                .imageUrls(imageUrls)
                .isActive(variant.getIsActive())
                .sortOrder(variant.getSortOrder())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }
}