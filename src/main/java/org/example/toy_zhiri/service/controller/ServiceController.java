package org.example.toy_zhiri.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.service.dto.CategoryResponse;
import org.example.toy_zhiri.service.dto.ServiceFilterRequest;
import org.example.toy_zhiri.service.dto.ServicePageResponse;
import org.example.toy_zhiri.service.dto.ServiceResponse;
import org.example.toy_zhiri.service.enums.SortType;
import org.example.toy_zhiri.service.service.CategoryService;
import org.example.toy_zhiri.service.service.ServiceService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@Tag(name = "Services", description = "API каталога услуг")
public class ServiceController {
    private final ServiceService serviceService;
    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping("/categories")
    @Operation(
            summary = "Получить список категорий",
            description = "Возвращает все активные категории услуг"
    )
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping
    @Operation(
            summary = "Получить список услуг",
            description = "Получение каталога услуг с базовой фильтрацией и пагинацией. " +
                    "Сортировка по умолчанию — POPULARITY (по количеству бронирований и просмотров)."
    )
    public ResponseEntity<ServicePageResponse> getServices(
            @Parameter(description = "ID категории для фильтрации")
            @RequestParam(required = false) UUID categoryId,

            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userDetails != null ? userService.getIdByEmail(userDetails.getUsername()) : null;
        ServiceFilterRequest filter = ServiceFilterRequest.builder()
                .categoryId(categoryId)
                .sortType(SortType.POPULARITY)
                .build();

        return ResponseEntity.ok(serviceService.getFilteredServices(filter, userId, page, size));
    }

    @GetMapping("/filter")
    @Operation(
            summary = "Получить список услуг с расширенными фильтрами",
            description = "Получение каталога услуг с поддержкой комбинации всех фильтров и сортировки. " +
                    "Порядок обработки: фильтры → сортировка → пагинация. " +
                    "Доступные значения sortType: POPULARITY, PRICE_ASC, PRICE_DESC, RATING."
    )
    public ResponseEntity<ServicePageResponse> getFilteredServices(
            @ModelAttribute ServiceFilterRequest filter,

            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userDetails != null ? userService.getIdByEmail(userDetails.getUsername()) : null;
        return ResponseEntity.ok(serviceService.getFilteredServices(filter, userId, page, size));
    }

    @GetMapping("/{serviceId}")
    @Operation(
            summary = "Получить детали услуги",
            description = "Подробная информация об услуге"
    )
    public ResponseEntity<ServiceResponse> getServiceDetails(
            @PathVariable UUID serviceId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userDetails != null ? userService.getIdByEmail(userDetails.getUsername()) : null;
        return ResponseEntity.ok(serviceService.getServiceById(serviceId, userId));
    }
    @GetMapping("/{serviceId}/unavailable-dates")
    @Operation(
            summary = "Недоступные даты услуги",
            description = "Возвращает все даты, недоступные для бронирования за указанный период. " +
                    "Объединяет даты заблокированные партнёром и занятые активными бронированиями. " +
                    "Используется фронтендом для блокировки дат в календаре."
    )
    public ResponseEntity<UnavailableDatesResponse> getUnavailableDates(
            @PathVariable UUID serviceId,

            @Parameter(description = "Начало периода (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Конец периода (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(bookingService.getUnavailableDates(serviceId, from, to));
    }
}