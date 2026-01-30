package org.example.toy_zhiri.service.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.example.toy_zhiri.service.dto.ServiceFilterRequest;
import org.example.toy_zhiri.service.entity.Service;
import org.example.toy_zhiri.service.entity.ServiceCategory;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Спецификация для динамической фильтрации услуг.
 * Поддерживает комбинацию всех доступных фильтров.
 */
public class ServiceSpecification {

    /**
     * Создаёт спецификацию на основе переданных фильтров.
     * Все условия объединяются через AND.
     *
     * @param filter объект с параметрами фильтрации
     * @return спецификация для JPA запроса
     */
    public static Specification<Service> createSpecification(ServiceFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Базовые условия: услуга активна и одобрена
            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));
            predicates.add(criteriaBuilder.isTrue(root.get("isApproved")));

            // Фильтр по категории
            if (filter.getCategoryId() != null) {
                Join<Service, ServiceCategory> categoryJoin = root.join("category");
                predicates.add(criteriaBuilder.equal(categoryJoin.get("id"), filter.getCategoryId()));
            }

            // Фильтр по минимальной цене
            if (filter.getPriceMin() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("priceFrom"),
                                filter.getPriceMin()
                        )
                );
            }

            // Фильтр по максимальной цене
            if (filter.getPriceMax() != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("priceFrom"),
                                filter.getPriceMax()
                        )
                );
            }

            // Фильтр по минимальному рейтингу
            if (filter.getRatingMin() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("rating"),
                                filter.getRatingMin()
                        )
                );
            }

            // Фильтр по одному городу
            if (filter.getCity() != null && !filter.getCity().isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("city")),
                                "%" + filter.getCity().toLowerCase() + "%"
                        )
                );
            }

            // Фильтр по нескольким городам
            if (filter.getCities() != null && !filter.getCities().isEmpty()) {
                List<Predicate> cityPredicates = new ArrayList<>();
                for (String city : filter.getCities()) {
                    cityPredicates.add(
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get("city")),
                                    "%" + city.toLowerCase() + "%"
                            )
                    );
                }
                predicates.add(criteriaBuilder.or(cityPredicates.toArray(new Predicate[0])));
            }

            // Фильтр по типу услуги (поиск в описании)
            if (filter.getServiceType() != null && !filter.getServiceType().isBlank()) {
                Predicate inShortDesc = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("shortDescription")),
                        "%" + filter.getServiceType().toLowerCase() + "%"
                );
                Predicate inFullDesc = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("fullDescription")),
                        "%" + filter.getServiceType().toLowerCase() + "%"
                );
                Predicate inName = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + filter.getServiceType().toLowerCase() + "%"
                );
                predicates.add(criteriaBuilder.or(inShortDesc, inFullDesc, inName));
            }

            // Фильтр по поисковому запросу (в названии и описаниях)
            if (filter.getSearchQuery() != null && !filter.getSearchQuery().isBlank()) {
                String searchPattern = "%" + filter.getSearchQuery().toLowerCase() + "%";
                Predicate nameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        searchPattern
                );
                Predicate shortDescMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("shortDescription")),
                        searchPattern
                );
                Predicate fullDescMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("fullDescription")),
                        searchPattern
                );
                predicates.add(criteriaBuilder.or(nameMatch, shortDescMatch, fullDescMatch));
            }

            // Фильтр по наличию изображений
            if (filter.getHasImages() != null && filter.getHasImages()) {
                // Используем exist subquery для проверки наличия изображений
                predicates.add(
                        criteriaBuilder.isNotEmpty(root.get("images"))
                );
            }

            // Фильтр по минимальному количеству отзывов
            if (filter.getMinReviews() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("reviewsCount"),
                                filter.getMinReviews()
                        )
                );
            }

            // TODO: Фильтр по доступным датам
            // Для этого потребуется создать отдельную таблицу calendar/availability
            // или хранить JSON с занятыми датами
            // Пока оставляем как комментарий для будущей реализации
            /*
            if (filter.getAvailableDate() != null) {
                // Проверка доступности конкретной даты
                // Здесь должна быть логика проверки календаря партнёра
            }

            if (filter.getAvailableDates() != null && !filter.getAvailableDates().isEmpty()) {
                // Проверка доступности хотя бы одной из дат
            }
            */

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Спецификация для поиска только активных и одобренных услуг.
     */
    public static Specification<Service> isActiveAndApproved() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.isTrue(root.get("isActive")),
                criteriaBuilder.isTrue(root.get("isApproved"))
        );
    }

    /**
     * Спецификация для фильтрации по категории.
     */
    public static Specification<Service> hasCategory(java.util.UUID categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Service, ServiceCategory> categoryJoin = root.join("category");
            return criteriaBuilder.equal(categoryJoin.get("id"), categoryId);
        };
    }

    /**
     * Спецификация для фильтрации по диапазону цен.
     */
    public static Specification<Service> hasPriceBetween(
        java.math.BigDecimal minPrice,
        java.math.BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("priceFrom"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("priceFrom"), maxPrice));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Спецификация для фильтрации по минимальному рейтингу.
     */
    public static Specification<Service> hasMinRating(java.math.BigDecimal minRating) {
        return (root, query, criteriaBuilder) -> {
            if (minRating == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating);
        };
    }
}