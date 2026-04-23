package org.example.toy_zhiri.attribute.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.attribute.dto.AttributeDefinitionResponse;
import org.example.toy_zhiri.attribute.dto.CreateAttributeDefinitionRequest;
import org.example.toy_zhiri.attribute.dto.UpdateAttributeDefinitionRequest;
import org.example.toy_zhiri.attribute.service.AttributeDefinitionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Контроллер административного управления определениями атрибутов.
 */
@RestController
@RequestMapping("/api/v1/admin/attributes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Attributes", description = "API администратора для управления атрибутами услуг")
public class AdminAttributeDefinitionController {

    private final AttributeDefinitionService service;

    /**
     * Возвращает список определений атрибутов с пагинацией и поиском.
     *
     * @param search   подстрока для поиска по ключу или русскому лейблу
     * @param pageable параметры пагинации
     * @return ResponseEntity<Page<AttributeDefinitionResponse>> страница атрибутов
     */
    @GetMapping
    @Operation(
            summary = "Список атрибутов",
            description = "Возвращает список всех определений атрибутов с пагинацией и поиском",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<AttributeDefinitionResponse>> list(
            @Parameter(description = "Подстрока для поиска")
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(service.list(search, pageable));
    }

    /**
     * Возвращает определение атрибута по идентификатору.
     *
     * @param id идентификатор атрибута
     * @return ResponseEntity<AttributeDefinitionResponse> определение атрибута
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Получить атрибут",
            description = "Возвращает определение атрибута по идентификатору",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<AttributeDefinitionResponse> getById(
            @Parameter(description = "ID атрибута")
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * Создаёт новое определение атрибута.
     *
     * @param request данные для создания
     * @return ResponseEntity<AttributeDefinitionResponse> созданный атрибут
     */
    @PostMapping
    @Operation(
            summary = "Создать атрибут",
            description = "Создаёт новое определение атрибута",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<AttributeDefinitionResponse> create(
            @Valid @RequestBody CreateAttributeDefinitionRequest request) {
        return ResponseEntity.status(201).body(service.create(request));
    }

    /**
     * Обновляет определение атрибута.
     * Изменяются только "мягкие" поля: лейблы, единица измерения, правила валидации.
     * Ключ, тип, стратегия и ключи хранения неизменяемы.
     *
     * @param id      идентификатор атрибута
     * @param request данные для обновления
     * @return ResponseEntity<AttributeDefinitionResponse> обновлённый атрибут
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить атрибут",
            description = "Обновляет мягкие поля определения атрибута",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<AttributeDefinitionResponse> update(
            @Parameter(description = "ID атрибута")
            @PathVariable UUID id,

            @Valid @RequestBody UpdateAttributeDefinitionRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    /**
     * Удаляет определение атрибута.
     * Запрещено, если атрибут привязан к категории или используется в вариантах услуг.
     *
     * @param id идентификатор атрибута
     * @return ResponseEntity<Void> пустой ответ
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить атрибут",
            description = "Удаляет определение атрибута. Запрещено, если атрибут используется",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID атрибута")
            @PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}