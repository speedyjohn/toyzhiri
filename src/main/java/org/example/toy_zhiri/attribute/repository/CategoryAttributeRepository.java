package org.example.toy_zhiri.attribute.repository;

import org.example.toy_zhiri.attribute.entity.CategoryAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с привязками атрибутов к категориям.
 */
@Repository
public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, UUID> {

    /**
     * Возвращает все привязки атрибутов к категории, отсортированные по sortOrder.
     *
     * @param categoryId идентификатор категории
     * @return список привязок
     */
    List<CategoryAttribute> findByCategoryIdOrderBySortOrderAsc(UUID categoryId);

    /**
     * Возвращает только клиент-видимые привязки атрибутов к категории (is_filterable=true),
     * отсортированные по sortOrder.
     *
     * @param categoryId идентификатор категории
     * @return список привязок
     */
    List<CategoryAttribute> findByCategoryIdAndIsFilterableTrueOrderBySortOrderAsc(UUID categoryId);

    /**
     * Проверяет существование привязки атрибута к категории.
     *
     * @param categoryId  идентификатор категории
     * @param attributeId идентификатор определения атрибута
     * @return true, если привязка существует
     */
    boolean existsByCategoryIdAndAttributeId(UUID categoryId, UUID attributeId);

    /**
     * Находит привязку по паре (категория, атрибут).
     *
     * @param categoryId  идентификатор категории
     * @param attributeId идентификатор определения атрибута
     * @return Optional с найденной привязкой
     */
    Optional<CategoryAttribute> findByCategoryIdAndAttributeId(UUID categoryId, UUID attributeId);

    /**
     * Проверяет, используется ли атрибут хотя бы в одной категории
     * (без учёта переданной привязки — используется при удалении).
     *
     * @param attributeId идентификатор определения атрибута
     * @return true, если существует хотя бы одна привязка
     */
    @Query("SELECT COUNT(ca) > 0 FROM CategoryAttribute ca WHERE ca.attribute.id = :attributeId")
    boolean existsByAttributeId(@Param("attributeId") UUID attributeId);
}