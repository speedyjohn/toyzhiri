package org.example.toy_zhiri.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.service.dto.SearchVariantsRequest;
import org.example.toy_zhiri.service.dto.ServiceVariantResponse;
import org.example.toy_zhiri.service.service.ServiceVariantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Публичный контроллер для просмотра вариантов услуги клиентом.
 * Возвращает только активные варианты, опционально отфильтрованные
 * по клиентским чекбоксам.
 */
@RestController
@RequestMapping("/api/v1/services/{serviceId}/variants")
@RequiredArgsConstructor
@Tag(name = "Service Variants", description = "Публичное API вариантов услуг")
public class PublicServiceVariantController {

    private final ServiceVariantService variantService;

    /**
     * Возвращает все активные варианты услуги.
     *
     * @param serviceId идентификатор услуги
     * @return ResponseEntity<List<ServiceVariantResponse>> список активных вариантов
     */
    @GetMapping
    @Operation(
            summary = "Список вариантов услуги",
            description = "Возвращает все активные варианты услуги (например, залы ресторана)"
    )
    public ResponseEntity<List<ServiceVariantResponse>> list(
            @Parameter(description = "ID услуги")
            @PathVariable UUID serviceId) {
        return ResponseEntity.ok(variantService.listForClient(serviceId, null));
    }

    /**
     * Подбирает варианты услуги, удовлетворяющие клиентским фильтрам.
     * Используется в форме бронирования: клиент отмечает чекбоксы и вводит значения,
     * система показывает только подходящие варианты.
     * <p>
     * Ключи в filters — логические ключи атрибутов из схемы категории.
     * Значения зависят от match_strategy атрибута:
     * - RANGE_CONTAINS: одно число (попадание в диапазон [min;max] варианта);
     * - SINGLE_GTE: число (вариант должен быть >= значения);
     * - SINGLE_LTE: число (вариант должен быть <= значения);
     * - SINGLE_EQ: строка/число (точное совпадение);
     * - BOOLEAN_MATCH: true/false;
     * - ARRAY_INTERSECTS / ARRAY_CONTAINS: массив строк.
     *
     * @param serviceId идентификатор услуги
     * @param request   клиентские фильтры
     * @return ResponseEntity<List<ServiceVariantResponse>> подходящие варианты
     */
    @PostMapping("/search")
    @Operation(
            summary = "Подбор вариантов по фильтрам",
            description = "Возвращает варианты услуги, удовлетворяющие клиентским чекбоксам. " +
                    "Пустой объект filters возвращает все активные варианты"
    )
    public ResponseEntity<List<ServiceVariantResponse>> search(
            @Parameter(description = "ID услуги")
            @PathVariable UUID serviceId,

            @RequestBody SearchVariantsRequest request) {
        return ResponseEntity.ok(
                variantService.listForClient(serviceId, request.getFilters())
        );
    }
}