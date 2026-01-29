package org.example.toy_zhiri.service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.partner.entity.Partner;
import org.example.toy_zhiri.partner.repository.PartnerRepository;
import org.example.toy_zhiri.service.dto.CreateServiceRequest;
import org.example.toy_zhiri.service.dto.ServiceResponse;
import org.example.toy_zhiri.service.dto.UpdateServiceRequest;
import org.example.toy_zhiri.service.entity.Service;
import org.example.toy_zhiri.service.entity.ServiceCategory;
import org.example.toy_zhiri.service.entity.ServiceImage;
import org.example.toy_zhiri.service.enums.PriceType;
import org.example.toy_zhiri.service.repository.ServiceCategoryRepository;
import org.example.toy_zhiri.service.repository.ServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class PartnerServiceService {
    private final ServiceRepository serviceRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final PartnerRepository partnerRepository;
    private final ServiceService serviceService;

    @Transactional
    public ServiceResponse createService(UUID userId, CreateServiceRequest request) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Партнёр не найден"));

        ServiceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        String slug = generateSlug(request.getName());

        Service service = Service.builder()
                .partner(partner)
                .category(category)
                .name(request.getName())
                .slug(slug)
                .shortDescription(request.getShortDescription())
                .fullDescription(request.getFullDescription())
                .priceFrom(request.getPriceFrom())
                .priceTo(request.getPriceTo())
                .priceType(PriceType.valueOf(request.getPriceType().toUpperCase()))
                .city(request.getCity())
                .address(request.getAddress())
                .thumbnail(request.getThumbnail())
                .isActive(false)
                .isApproved(false)
                .rating(BigDecimal.ZERO)
                .reviewsCount(0)
                .viewsCount(0)
                .bookingsCount(0)
                .build();

        Service savedService = serviceRepository.save(service);

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            int order = 0;
            for (String url : request.getImageUrls()) {
                ServiceImage image = ServiceImage.builder()
                        .service(savedService)
                        .imageUrl(url)
                        .displayOrder(order++)
                        .isPrimary(order == 1)
                        .build();
                savedService.getImages().add(image);
            }
            serviceRepository.save(savedService);
        }

        return serviceService.getServiceById(savedService.getId(), userId);
    }

    @Transactional
    public ServiceResponse updateService(UUID userId, UUID serviceId, UpdateServiceRequest request) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Партнёр не найден"));

        if (!service.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("У вас нет прав на редактирование этой услуги");
        }

        if (request.getName() != null) {
            service.setName(request.getName());
            service.setSlug(generateSlug(request.getName()));
        }
        if (request.getShortDescription() != null) service.setShortDescription(request.getShortDescription());
        if (request.getFullDescription() != null) service.setFullDescription(request.getFullDescription());
        if (request.getPriceFrom() != null) service.setPriceFrom(request.getPriceFrom());
        if (request.getPriceTo() != null) service.setPriceTo(request.getPriceTo());
        if (request.getPriceType() != null) service.setPriceType(PriceType.valueOf(request.getPriceType().toUpperCase()));
        if (request.getCity() != null) service.setCity(request.getCity());
        if (request.getAddress() != null) service.setAddress(request.getAddress());
        if (request.getThumbnail() != null) service.setThumbnail(request.getThumbnail());
        if (request.getIsActive() != null) service.setIsActive(request.getIsActive());

        if (request.getImageUrls() != null) {
            service.getImages().clear();
            int order = 0;
            for (String url : request.getImageUrls()) {
                ServiceImage image = ServiceImage.builder()
                        .service(service)
                        .imageUrl(url)
                        .displayOrder(order++)
                        .isPrimary(order == 1)
                        .build();
                service.getImages().add(image);
            }
        }

        Service updated = serviceRepository.save(service);
        return serviceService.getServiceById(updated.getId(), userId);
    }

    @Transactional
    public MessageResponse deleteService(UUID userId, UUID serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Партнёр не найден"));

        if (!service.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("У вас нет прав на удаление этой услуги");
        }

        serviceRepository.delete(service);

        return MessageResponse.builder()
                .message("Услуга успешно удалена")
                .build();
    }

    public Page<ServiceResponse> getMyServices(UUID userId, Pageable pageable) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Партнёр не найден"));

        Page<Service> services = serviceRepository.findByPartnerIdAndIsActiveTrue(partner.getId(), pageable);
        return services.map(service -> serviceService.getServiceById(service.getId(), userId));
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9а-я\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}