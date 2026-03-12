package org.example.toy_zhiri.service.service;

import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.service.dto.ServiceFilterRequest;
import org.example.toy_zhiri.service.dto.ServicePageResponse;
import org.example.toy_zhiri.service.dto.ServiceResponse;
import org.example.toy_zhiri.service.entity.Service;
import org.example.toy_zhiri.service.entity.ServiceImage;
import org.example.toy_zhiri.service.enums.SortType;
import org.example.toy_zhiri.service.repository.CartItemRepository;
import org.example.toy_zhiri.service.repository.FavoriteRepository;
import org.example.toy_zhiri.service.repository.ServiceRepository;
import org.example.toy_zhiri.service.specification.ServiceSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceService {
    private final ServiceRepository serviceRepository;
    private final FavoriteRepository favoriteRepository;
    private final CartItemRepository cartItemRepository;

    /**
     * Получение услуг с расширенной фильтрацией и сортировкой.
     * Порядок обработки: фильтры → сортировка → пагинация.
     *
     * @param filter объект с параметрами фильтрации и типом сортировки
     * @param userId ID текущего пользователя (для определения избранного и корзины)
     * @param page   номер страницы
     * @param size   размер страницы
     * @return страница с отфильтрованными и отсортированными услугами
     */
    public ServicePageResponse getFilteredServices(
            ServiceFilterRequest filter,
            UUID userId,
            int page,
            int size) {

        // Определяем тип сортировки, при null или некорректном значении — POPULARITY
        SortType sortType = filter.getSortType() != null ? filter.getSortType() : SortType.POPULARITY;

        Pageable pageable = PageRequest.of(page, size, buildSort(sortType));

        Specification<Service> spec = ServiceSpecification.createSpecification(filter);
        Page<Service> services = serviceRepository.findAll(spec, pageable);

        return buildPageResponse(services, userId, sortType);
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

    /**
     * Строит объект Sort на основе типа сортировки.
     *
     * POPULARITY — bookings_count DESC, затем views_count DESC.
     * PRICE_ASC  — price_from ASC.
     * PRICE_DESC — price_from DESC.
     * RATING     — rating DESC, затем reviews_count DESC (при равном рейтинге).
     *
     * @param sortType тип сортировки
     * @return объект Sort для Pageable
     */
    private Sort buildSort(SortType sortType) {
        return switch (sortType) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "priceFrom");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "priceFrom");
            case RATING -> Sort.by(
                    Sort.Order.desc("rating"),
                    Sort.Order.desc("reviewsCount")
            );
            default -> Sort.by(  // POPULARITY
                    Sort.Order.desc("bookingsCount"),
                    Sort.Order.desc("viewsCount")
            );
        };
    }

    /**
     * Собирает ServicePageResponse из страницы JPA и применённого типа сортировки.
     */
    private ServicePageResponse buildPageResponse(Page<Service> page, UUID userId, SortType sortType) {
        List<ServiceResponse> content = page.getContent()
                .stream()
                .map(service -> mapToResponse(service, userId))
                .collect(Collectors.toList());

        return ServicePageResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .appliedSortType(sortType)
                .build();
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
                .bookingsCount(service.getBookingsCount())
                .thumbnail(service.getThumbnail())
                .images(images)
                .isFavorite(isFavorite)
                .inCart(inCart)
                .build();
    }
}