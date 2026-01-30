package org.example.toy_zhiri.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.AdminChangeServiceActiveStatusRequest;
import org.example.toy_zhiri.admin.dto.AdminChangeServiceApprovalStatusRequest;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.service.dto.ServiceResponse;
import org.example.toy_zhiri.service.service.AdminServiceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Services", description = "Управление услугами администратором")
public class AdminServiceController {
    private final AdminServiceService adminServiceService;

    @GetMapping
    @Operation(
            summary = "Получить все услуги",
            description = "Получение списка всех услуг (включая неодобренные и неактивные) с пагинацией",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<ServiceResponse>> getAllServices(
            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Поле сортировки")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Направление сортировки")
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(adminServiceService.getAllServices(pageable));
    }

    @GetMapping("/pending")
    @Operation(
            summary = "Получить услуги на модерации",
            description = "Список всех услуг, ожидающих одобрения (isApproved = false)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<ServiceResponse>> getPendingServices() {
        return ResponseEntity.ok(adminServiceService.getPendingServices());
    }

    @GetMapping("/{serviceId}")
    @Operation(
            summary = "Получить услугу по ID",
            description = "Детальная информация об услуге",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ServiceResponse> getServiceById(
            @Parameter(description = "ID услуги")
            @PathVariable UUID serviceId) {
        return ResponseEntity.ok(adminServiceService.getServiceById(serviceId));
    }

    @PatchMapping("/{serviceId}/active-status")
    @Operation(
            summary = "Изменить статус активности услуги",
            description = "Активировать или деактивировать услугу",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ServiceResponse> changeActiveStatus(
            @Parameter(description = "ID услуги")
            @PathVariable UUID serviceId,

            @Valid @RequestBody AdminChangeServiceActiveStatusRequest request) {
        return ResponseEntity.ok(adminServiceService.changeActiveStatus(serviceId, request));
    }

    @PatchMapping("/{serviceId}/approval-status")
    @Operation(
            summary = "Одобрить или отклонить услугу",
            description = "Изменить статус одобрения услуги. При одобрении услуга автоматически становится активной",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ServiceResponse> changeApprovalStatus(
            @Parameter(description = "ID услуги")
            @PathVariable UUID serviceId,

            @Valid @RequestBody AdminChangeServiceApprovalStatusRequest request) {
        return ResponseEntity.ok(adminServiceService.changeApprovalStatus(serviceId, request));
    }

    @DeleteMapping("/{serviceId}")
    @Operation(
            summary = "Удалить услугу",
            description = "Полное удаление услуги из системы (только для администратора)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> deleteService(
            @Parameter(description = "ID услуги")
            @PathVariable UUID serviceId) {
        return ResponseEntity.ok(adminServiceService.deleteService(serviceId));
    }
}