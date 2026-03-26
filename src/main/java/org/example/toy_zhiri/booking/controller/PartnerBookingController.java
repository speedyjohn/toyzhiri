package org.example.toy_zhiri.booking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.booking.dto.BookingResponse;
import org.example.toy_zhiri.booking.dto.RejectBookingRequest;
import org.example.toy_zhiri.booking.enums.BookingStatus;
import org.example.toy_zhiri.booking.service.BookingService;
import org.example.toy_zhiri.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/v1/partner/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARTNER')")
@Tag(name = "Partner Bookings", description = "Управление бронированиями партнёра")
public class PartnerBookingController {
    private final BookingService bookingService;
    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Входящие бронирования",
            description = "Список всех бронирований партнёра с опциональной фильтрацией по статусу.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<BookingResponse>> getPartnerBookings(
            @Parameter(description = "Фильтр по статусу")
            @RequestParam(required = false) BookingStatus status,

            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getPartnerBookings(userId, status, pageable));
    }

    @GetMapping("/{bookingId}")
    @Operation(
            summary = "Детали бронирования",
            description = "Детальная информация о конкретном бронировании.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(bookingService.getPartnerBookingById(userId, bookingId));
    }

    @PatchMapping("/{bookingId}/confirm")
    @Operation(
            summary = "Подтвердить бронирование",
            description = "Партнёр подтверждает бронирование. Статус меняется на CONFIRMED.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(bookingService.confirmBooking(userId, bookingId));
    }

    @PatchMapping("/{bookingId}/reject")
    @Operation(
            summary = "Отклонить бронирование",
            description = "Партнёр отклоняет бронирование с опциональной причиной. Статус меняется на REJECTED.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<BookingResponse> rejectBooking(
            @PathVariable UUID bookingId,
            @RequestBody(required = false) RejectBookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        if (request == null) {
            request = new RejectBookingRequest();
        }
        return ResponseEntity.ok(bookingService.rejectBooking(userId, bookingId, request));
    }

    @PatchMapping("/{bookingId}/complete")
    @Operation(
            summary = "Подтвердить завершение сделки",
            description = "Партнёр подтверждает, что услуга была оказана. " +
                    "Доступно только для бронирований со статусом CONFIRMED. " +
                    "Когда оба участника (клиент и партнёр) подтвердят — статус меняется на COMPLETED.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<BookingResponse> confirmCompletion(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(bookingService.partnerConfirmCompletion(userId, bookingId));
    }

    @GetMapping("/calendar")
    @Operation(
            summary = "Календарь бронирований",
            description = "Бронирования партнёра за указанный период. " +
                    "Используется для отображения занятости в календаре.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<BookingResponse>> getCalendar(
            @Parameter(description = "Начало периода (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Конец периода (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(bookingService.getPartnerCalendar(userId, from, to));
    }
}