package org.example.toy_zhiri.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.service.dto.AvailabilityResponse;
import org.example.toy_zhiri.service.dto.SetAvailabilityRequest;
import org.example.toy_zhiri.service.service.ServiceAvailabilityService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services/{serviceId}/availability")
@RequiredArgsConstructor
@Tag(name = "Service Availability", description = "Управление доступностью услуг")
public class ServiceAvailabilityController {
    private final ServiceAvailabilityService availabilityService;
    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Получить расписание доступности",
            description = "Возвращает список дат с их статусами за указанный период. " +
                    "Доступно всем пользователям — клиенты видят свободные даты при бронировании."
    )
    public ResponseEntity<List<AvailabilityResponse>> getAvailability(
            @PathVariable UUID serviceId,

            @Parameter(description = "Начало периода (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Конец периода (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(availabilityService.getAvailability(serviceId, from, to));
    }

    @PutMapping
    @PreAuthorize("hasRole('PARTNER')")
    @Operation(
            summary = "Установить доступность дат",
            description = "Партнёр устанавливает статус (AVAILABLE / BLOCKED) для одной или нескольких дат. " +
                    "Если запись на дату уже существует — обновляется.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<AvailabilityResponse>> setAvailability(
            @PathVariable UUID serviceId,
            @Valid @RequestBody SetAvailabilityRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(availabilityService.setAvailability(userId, serviceId, request));
    }

    @DeleteMapping("/{date}")
    @PreAuthorize("hasRole('PARTNER')")
    @Operation(
            summary = "Удалить запись доступности",
            description = "Партнёр удаляет запись расписания для конкретной даты.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Void> deleteAvailability(
            @PathVariable UUID serviceId,

            @Parameter(description = "Дата (YYYY-MM-DD)")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        availabilityService.deleteAvailability(userId, serviceId, date);
        return ResponseEntity.noContent().build();
    }
}