package org.example.toy_zhiri.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.service.dto.CategoryResponse;
import org.example.toy_zhiri.service.dto.ServiceFilterRequest;
import org.example.toy_zhiri.service.dto.ServiceResponse;
import org.example.toy_zhiri.service.service.CategoryService;
import org.example.toy_zhiri.service.service.ServiceService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
            summary = "Получить список услуг (базовая фильтрация)",
            description = "Получение каталога услуг с базовой фильтрацией и пагинацией"
    )
    public ResponseEntity<Page<ServiceResponse>> getServices(
            @Parameter(description = "ID категории для фильтрации")
            @RequestParam(required = false) UUID categoryId,

            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Поле сортировки")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Направление сортировки")
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userDetails != null ? userService.getIdByEmail(userDetails.getUsername()) : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(serviceService.getAllServices(categoryId, userId, pageable));
    }

    @GetMapping("/filter")
    @Operation(
        summary = "Получить список услуг с расширенными фильтрами",
        description = "Получение каталога услуг с поддержкой комбинации всех фильтров: " +
                "цена, рейтинг, город, тип услуги, поиск по ключевым словам"
    )
    public ResponseEntity<Page<ServiceResponse>> getFilteredServices(
            @ModelAttribute ServiceFilterRequest filter,

            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Поле сортировки")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Направление сортировки")
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userDetails != null ? userService.getIdByEmail(userDetails.getUsername()) : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(serviceService.getFilteredServices(filter, userId, pageable));
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
}