package org.example.toy_zhiri.service.service;

import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.service.dto.ServiceResponse;
import org.example.toy_zhiri.service.entity.Service;
import org.example.toy_zhiri.service.entity.ServiceImage;
import org.example.toy_zhiri.service.repository.FavoriteRepository;
import org.example.toy_zhiri.service.repository.CartItemRepository;
import org.example.toy_zhiri.service.repository.ServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceService {
    private final ServiceRepository serviceRepository;
    private final FavoriteRepository favoriteRepository;
    private final CartItemRepository cartItemRepository;

    public Page<ServiceResponse> getAllServices(UUID categoryId, UUID userId, Pageable pageable) {
        Page<Service> services;

        if (categoryId != null) {
            services = serviceRepository.findByCategoryIdAndIsActiveTrueAndIsApprovedTrue(categoryId, pageable);
        } else {
            services = serviceRepository.findByIsActiveTrueAndIsApprovedTrue(pageable);
        }

        return services.map(service -> mapToResponse(service, userId));
    }

    public ServiceResponse getServiceById(UUID serviceId, UUID userId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

        // Увеличиваем счетчик просмотров
        Integer currentViews = service.getViewsCount() != null ? service.getViewsCount() : 0;
        service.setViewsCount(currentViews + 1);
        serviceRepository.save(service);

        return mapToResponse(service, userId);
    }

    private ServiceResponse mapToResponse(Service service, UUID userId) {
        boolean isFavorite = userId != null &&
                favoriteRepository.existsByUserIdAndServiceId(userId, service.getId());

        boolean inCart = userId != null &&
                cartItemRepository.findByUserIdAndServiceId(userId, service.getId()).isPresent();

        List<String> images = service.getImages() != null ?
                service.getImages().stream()
                        .map(ServiceImage::getImageUrl)
                        .collect(Collectors.toList()) : List.of();

        return ServiceResponse.builder()
                .id(service.getId())
                .partnerId(service.getPartner().getId())
                .partnerName(service.getPartner().getUser().getFullName())
                .categoryId(service.getCategory().getId())
                .categoryName(service.getCategory().getNameRu())
                .name(service.getName())
                .slug(service.getSlug())
                .shortDescription(service.getShortDescription())
                .fullDescription(service.getFullDescription())
                .priceFrom(service.getPriceFrom())
                .priceTo(service.getPriceTo())
                .priceType(service.getPriceType().name())
                .city(service.getCity())
                .address(service.getAddress())
                .rating(service.getRating())
                .reviewsCount(service.getReviewsCount())
                .viewsCount(service.getViewsCount())
                .thumbnail(service.getThumbnail())
                .images(images)
                .isFavorite(isFavorite)
                .inCart(inCart)
                .build();
    }
}