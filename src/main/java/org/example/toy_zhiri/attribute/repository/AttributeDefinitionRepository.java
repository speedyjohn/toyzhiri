package org.example.toy_zhiri.attribute.repository;

import org.example.toy_zhiri.attribute.entity.AttributeDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с определениями атрибутов.
 */
@Repository
public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, UUID> {

    /**
     * Находит определение атрибута по логическому ключу.
     *
     * @param key логический ключ атрибута
     * @return Optional с найденным определением
     */
    Optional<AttributeDefinition> findByKey(String key);

    /**
     * Проверяет существование атрибута с данным ключом.
     *
     * @param key логический ключ
     * @return true, если атрибут существует
     */
    boolean existsByKey(String key);

    /**
     * Постраничный поиск атрибутов по подстроке в ключе или русском лейбле.
     *
     * @param search   подстрока для поиска (может быть null)
     * @param pageable параметры пагинации
     * @return страница определений атрибутов
     */
    @Query("""
            SELECT ad FROM AttributeDefinition ad
            WHERE :search IS NULL
               OR LOWER(ad.key) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(ad.labelRu) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<AttributeDefinition> search(@Param("search") String search, Pageable pageable);

    /**
     * Проверяет, используется ли атрибут хотя бы в одной привязке к категории.
     *
     * @param attributeId идентификатор определения атрибута
     * @return true, если существует хотя бы одна привязка
     */
    @Query("SELECT COUNT(ca) > 0 FROM CategoryAttribute ca WHERE ca.attribute.id = :attributeId")
    boolean isUsedInCategoryBindings(@Param("attributeId") UUID attributeId);

    /**
     * Проверяет, используется ли атрибут хотя бы в одном варианте услуги.
     * Проверка выполняется нативно через функцию jsonb_exists (эквивалент оператора ?),
     * потому что прямое использование оператора ? конфликтует с плейсхолдерами JPA.
     *
     * @param storageKey ключ значения в JSONB (из storage_keys определения)
     * @return true, если существует хотя бы один вариант со значением этого ключа
     */
    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM service_variants sv
                WHERE jsonb_exists(sv.attributes, :storageKey)
            )
            """, nativeQuery = true)
    boolean isUsedInVariants(@Param("storageKey") String storageKey);
}