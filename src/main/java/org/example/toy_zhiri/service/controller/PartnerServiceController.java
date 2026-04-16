package org.example.toy_zhiri.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.service.dto.CreateServiceRequest;
import org.example.toy_zhiri.service.dto.ServiceResponse;
import org.example.toy_zhiri.service.dto.UpdateServiceRequest;
import org.example.toy_zhiri.service.service.PartnerServiceService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/partner/services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARTNER')")
@Tag(name = "Partner Services", description = "Управление услугами партнёра")
public class PartnerServiceController {
    private final PartnerServiceService partnerServiceService;
    private final UserService userService;

    @PostMapping
    @Operation(
            summary = "Создать услугу",
            description = "Партнёр создаёт новую услугу (требует модерации админа)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ServiceResponse> createService(
            @Valid @RequestBody CreateServiceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.status(201).body(partnerServiceService.createService(userId, request));
    }

    @PutMapping("/{serviceId}")
    @Operation(
            summary = "Редактировать услугу",
            description = "Обновить информацию об услуге",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable UUID serviceId,
            @Valid @RequestBody UpdateServiceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(partnerServiceService.updateService(userId, serviceId, request));
    }

    @DeleteMapping("/{serviceId}")
    @Operation(
            summary = "Удалить услугу",
            description = "Удалить свою услугу",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> deleteService(
            @PathVariable UUID serviceId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(partnerServiceService.deleteService(userId, serviceId));
    }

    @GetMapping
    @Operation(
            summary = "Мои услуги",
            description = "Получить список всех своих услуг",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<ServiceResponse>> getMyServices(
            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(partnerServiceService.getMyServices(userId, pageable));
    }
}