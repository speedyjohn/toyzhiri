package org.example.toy_zhiri.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.partner.dto.PartnerProfileResponse;
import org.example.toy_zhiri.partner.enums.PartnerStatus;
import org.example.toy_zhiri.partner.service.PartnerDirectoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Контроллер для управления справочником партнеров администратором.
 */
@RestController
@RequestMapping("/api/v1/admin/partner-directory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Partner Directory", description = "Управление справочником партнеров (только для админов)")
public class AdminPartnerDirectoryController {
    private final PartnerDirectoryService partnerDirectoryService;

    @GetMapping
    @Operation(
            summary = "Получить список всех партнеров",
            description = "Получение полного каталога партнеров с фильтрацией (включая неактивных)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<PartnerProfileResponse>> getAllPartners(
            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Поле сортировки")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Направление сортировки")
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection,

            @Parameter(description = "Фильтр по городу")
            @RequestParam(required = false) String city,

            @Parameter(description = "Фильтр по статусу")
            @RequestParam(required = false) PartnerStatus status,

            @Parameter(description = "Поиск по названию компании")
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<PartnerProfileResponse> partners = partnerDirectoryService.getAllPartners(
                pageable, city, status, search
        );

        return ResponseEntity.ok(partners);
    }

    @GetMapping("/{partnerId}")
    @Operation(
            summary = "Получить профиль партнера по ID",
            description = "Детальная информация о партнере",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PartnerProfileResponse> getPartnerProfile(
            @PathVariable UUID partnerId) {

        PartnerProfileResponse profile = partnerDirectoryService.getPartnerProfile(partnerId);
        return ResponseEntity.ok(profile);
    }
}