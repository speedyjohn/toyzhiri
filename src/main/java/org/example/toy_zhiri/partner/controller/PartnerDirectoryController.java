package org.example.toy_zhiri.partner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.partner.dto.PartnerProfileResponse;
import org.example.toy_zhiri.partner.dto.PartnerProfileUpdateRequest;
import org.example.toy_zhiri.partner.enums.PartnerStatus;
import org.example.toy_zhiri.partner.service.PartnerDirectoryService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Контроллер для работы со справочником партнеров.
 */
@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
@Tag(name = "Partner Directory", description = "API справочника партнеров")
public class PartnerDirectoryController {
    private final PartnerDirectoryService partnerDirectoryService;
    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Получить список партнеров",
            description = "Получение каталога партнеров с фильтрацией и пагинацией"
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
            summary = "Получить профиль партнера",
            description = "Детальная информация о партнере по ID"
    )
    public ResponseEntity<PartnerProfileResponse> getPartnerProfile(
            @PathVariable UUID partnerId) {

        PartnerProfileResponse profile = partnerDirectoryService.getPartnerProfile(partnerId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/my-profile")
    @PreAuthorize("hasRole('PARTNER')")
    @Operation(
            summary = "Получить свой профиль партнера",
            description = "Детальная информация о профиле текущего партнера",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PartnerProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        PartnerProfileResponse profile = partnerDirectoryService.getPartnerProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/my-profile")
    @PreAuthorize("hasRole('PARTNER')")
    @Operation(
            summary = "Обновить свой профиль партнера (частично)",
            description = "Частичное редактирование профиля - обновляются только переданные поля",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PartnerProfileResponse> updateMyProfile(
            @Valid @RequestBody PartnerProfileUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        PartnerProfileResponse profile = partnerDirectoryService.updatePartnerProfile(userId, request);
        return ResponseEntity.ok(profile);
    }
}