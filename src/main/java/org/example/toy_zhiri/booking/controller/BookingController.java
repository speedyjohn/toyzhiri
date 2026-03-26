package org.example.toy_zhiri.booking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.booking.dto.BookingHistoryFilter;
import org.example.toy_zhiri.booking.dto.BookingResponse;
import org.example.toy_zhiri.booking.dto.CreateBookingRequest;
import org.example.toy_zhiri.booking.enums.BookingStatus;
import org.example.toy_zhiri.booking.service.BookingService;
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
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "Bookings", description = "Бронирование услуг клиентом")
public class BookingController {
    private final BookingService bookingService;
    private final UserService userService;

    @PostMapping
    @Operation(
            summary = "Создать бронирование",
            description = "Клиент создаёт бронирование услуги. Статус — PENDING_CONFIRMATION. " +
                    "Партнёр должен подтвердить или отклонить в течение 24 часов.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.status(201).body(bookingService.createBooking(userId, request));
    }

    @GetMapping
    @Operation(
            summary = "Мои бронирования",
            description = "Список всех бронирований клиента с опциональной фильтрацией по статусу.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<BookingResponse>> getMyBookings(
            @Parameter(description = "Фильтр по статусу")
            @RequestParam(required = false) BookingStatus status,

            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getMyBookings(userId, status, pageable));
    }

    @GetMapping("/{bookingId}")
    @Operation(
            summary = "Детали бронирования",
            description = "Детальная информация о конкретном бронировании клиента.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(bookingService.getBookingById(userId, bookingId));
    }

    @GetMapping("/history")
    @Operation(
            summary = "История бронирований",
            description = "Полная история бронирований клиента с расширенной фильтрацией: " +
                    "по статусу, категории услуги, дате создания заказа и дате мероприятия.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<BookingResponse>> getMyBookingHistory(
            @ModelAttribute BookingHistoryFilter filter,

            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getMyBookingHistory(userId, filter, pageable));
    }

    @PatchMapping("/{bookingId}/cancel")
    @Operation(
            summary = "Отменить бронирование",
            description = "Клиент отменяет бронирование. " +
                    "Допустимые статусы для отмены: PENDING_CONFIRMATION, CONFIRMED.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(bookingService.cancelBooking(userId, bookingId));
    }

    @PatchMapping("/{bookingId}/complete")
    @Operation(
            summary = "Подтвердить завершение сделки",
            description = "Клиент подтверждает, что услуга была оказана. " +
                    "Доступно только для бронирований со статусом CONFIRMED. " +
                    "Когда оба участника (клиент и партнёр) подтвердят — статус меняется на COMPLETED.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<BookingResponse> confirmCompletion(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userService.getIdByEmail(userDetails.getUsername());
        return ResponseEntity.ok(bookingService.clientConfirmCompletion(userId, bookingId));
    }
}