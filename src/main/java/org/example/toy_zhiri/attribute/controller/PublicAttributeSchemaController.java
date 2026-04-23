package org.example.toy_zhiri.attribute.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.attribute.dto.AttributeSchemaItemResponse;
import org.example.toy_zhiri.attribute.service.AttributeSchemaService;
import org.example.toy_zhiri.exception.NotFoundException;
import org.example.toy_zhiri.service.entity.ServiceCategory;
import org.example.toy_zhiri.service.repository.ServiceCategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Публичный контроллер для получения схемы атрибутов категории.
 * Используется фронтом для динамической генерации:
 * - формы создания/редактирования варианта услуги (партнёр);
 * - чекбоксов в форме бронирования и фильтров каталога (клиент).
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category Attribute Schema", description = "Публичное API схемы атрибутов категорий")
public class PublicAttributeSchemaController {

    private final AttributeSchemaService attributeSchemaService;
    private final ServiceCategoryRepository categoryRepository;

    /**
     * Возвращает полную схему атрибутов категории для партнёра.
     * Включает все атрибуты (и обязательные, и опциональные) —
     * партнёр заполняет их при создании варианта услуги.
     *
     * @param slug slug категории (например, "restaurants")
     * @return ResponseEntity<List<AttributeSchemaItemResponse>> полная схема
     */
    @GetMapping("/{slug}/attribute-schema")
    @Operation(
            summary = "Схема атрибутов категории (для партнёра)",
            description = "Возвращает полную схему атрибутов категории для построения формы создания " +
                    "варианта услуги. Включает обязательные и опциональные атрибуты"
    )
    public ResponseEntity<List<AttributeSchemaItemResponse>> getSchema(
            @Parameter(description = "Slug категории")
            @PathVariable String slug) {
        ServiceCategory category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException(
                        "Категория с slug '" + slug + "' не найдена"));

        return ResponseEntity.ok(attributeSchemaService.getFullSchema(category.getId()));
    }

    /**
     * Возвращает клиент-видимую схему атрибутов категории.
     * Используется фронтом для построения чекбоксов в форме бронирования
     * и фильтров в каталоге. Возвращает только атрибуты с is_filterable=true.
     *
     * @param slug slug категории
     * @return ResponseEntity<List<AttributeSchemaItemResponse>> клиент-видимая схема
     */
    @GetMapping("/{slug}/client-attribute-schema")
    @Operation(
            summary = "Клиентская схема атрибутов категории",
            description = "Возвращает только клиент-видимые атрибуты для построения чекбоксов/фильтров"
    )
    public ResponseEntity<List<AttributeSchemaItemResponse>> getClientSchema(
            @Parameter(description = "Slug категории")
            @PathVariable String slug) {
        ServiceCategory category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException(
                        "Категория с slug '" + slug + "' не найдена"));

        return ResponseEntity.ok(attributeSchemaService.getClientSchema(category.getId()));
    }
}