package org.example.toy_zhiri.service.repository;

import org.example.toy_zhiri.service.entity.ServiceVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с вариантами услуг.
 */
@Repository
public interface ServiceVariantRepository extends JpaRepository<ServiceVariant, UUID> {

    /**
     * Возвращает все варианты услуги, отсортированные по sortOrder.
     *
     * @param serviceId идентификатор услуги
     * @return список вариантов
     */
    List<ServiceVariant> findByServiceIdOrderBySortOrderAsc(UUID serviceId);

    /**
     * Возвращает только активные варианты услуги, отсортированные по sortOrder.
     *
     * @param serviceId идентификатор услуги
     * @return список активных вариантов
     */
    List<ServiceVariant> findByServiceIdAndIsActiveTrueOrderBySortOrderAsc(UUID serviceId);

    /**
     * Находит вариант по идентификатору и идентификатору родительской услуги.
     * Используется для проверки принадлежности варианта услуге.
     *
     * @param id        идентификатор варианта
     * @param serviceId идентификатор услуги
     * @return Optional с найденным вариантом
     */
    Optional<ServiceVariant> findByIdAndServiceId(UUID id, UUID serviceId);

    /**
     * Проверяет, существует ли хотя бы один вариант у услуги.
     *
     * @param serviceId идентификатор услуги
     * @return true, если у услуги есть варианты
     */
    boolean existsByServiceId(UUID serviceId);

    /**
     * Проверяет, используется ли вариант в активных бронированиях.
     * Активными считаются брони в статусах PENDING_CONFIRMATION и CONFIRMED,
     * при которых удаление варианта невозможно.
     *
     * @param variantId идентификатор варианта
     * @return true, если есть хотя бы одно активное бронирование
     */
    @Query("""
            SELECT COUNT(b) > 0 FROM Booking b
            WHERE b.variant.id = :variantId
              AND b.status IN (
                  org.example.toy_zhiri.booking.enums.BookingStatus.PENDING_CONFIRMATION,
                  org.example.toy_zhiri.booking.enums.BookingStatus.CONFIRMED
              )
            """)
    boolean hasActiveBookings(@Param("variantId") UUID variantId);

    /**
     * Проверяет, используется ли хотя бы один из вариантов услуги в активных бронированиях.
     *
     * @param serviceId идентификатор услуги
     * @return true, если есть активные брони на любой из вариантов
     */
    @Query("""
            SELECT COUNT(b) > 0 FROM Booking b
            WHERE b.variant.service.id = :serviceId
              AND b.status IN (
                  org.example.toy_zhiri.booking.enums.BookingStatus.PENDING_CONFIRMATION,
                  org.example.toy_zhiri.booking.enums.BookingStatus.CONFIRMED
              )
            """)
    boolean hasActiveBookingsForService(@Param("serviceId") UUID serviceId);

    /**
     * Возвращает активные варианты услуги, удовлетворяющие JSONB-фильтру.
     * Фильтр передаётся как JSONB и проверяется через оператор @>.
     * <p>
     * ВАЖНО: Для сложных стратегий (RANGE_CONTAINS, SINGLE_GTE, ARRAY_INTERSECTS)
     * этот метод не годится. Используется сервисный слой с построением условий
     * для каждого атрибута по его match_strategy.
     * <p>
     * Метод оставлен для простого случая точного совпадения.
     *
     * @param serviceId   идентификатор услуги
     * @param jsonbFilter фрагмент JSONB для точного совпадения
     * @return список подходящих активных вариантов
     */
    @Query(value = """
            SELECT * FROM service_variants sv
            WHERE sv.service_id = :serviceId
              AND sv.is_active = true
              AND sv.attributes @> CAST(:jsonbFilter AS jsonb)
            ORDER BY sv.sort_order ASC
            """, nativeQuery = true)
    List<ServiceVariant> findActiveByServiceIdAndAttributesContains(
            @Param("serviceId") UUID serviceId,
            @Param("jsonbFilter") String jsonbFilter);
}