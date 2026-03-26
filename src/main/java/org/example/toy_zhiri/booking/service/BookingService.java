package org.example.toy_zhiri.booking.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.booking.dto.BookingHistoryFilter;
import org.example.toy_zhiri.booking.dto.BookingResponse;
import org.example.toy_zhiri.booking.dto.CreateBookingRequest;
import org.example.toy_zhiri.booking.dto.RejectBookingRequest;
import org.example.toy_zhiri.booking.entity.Booking;
import org.example.toy_zhiri.booking.enums.BookingStatus;
import org.example.toy_zhiri.booking.repository.BookingRepository;
import org.example.toy_zhiri.partner.entity.Partner;
import org.example.toy_zhiri.partner.repository.PartnerRepository;
import org.example.toy_zhiri.service.dto.UnavailableDatesResponse;
import org.example.toy_zhiri.service.entity.Service;
import org.example.toy_zhiri.service.entity.ServiceAvailability;
import org.example.toy_zhiri.service.enums.AvailabilityStatus;
import org.example.toy_zhiri.service.repository.ServiceAvailabilityRepository;
import org.example.toy_zhiri.service.repository.ServiceRepository;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class BookingService {
    private static final int BOOKING_EXPIRY_HOURS = 24;

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final PartnerRepository partnerRepository;
    private final ServiceAvailabilityRepository availabilityRepository;

    /**
     * Создаёт новое бронирование.
     * После создания статус — PENDING_CONFIRMATION.
     * Партнёр должен ответить в течение 24 часов, иначе статус станет EXPIRED.
     */
    @Transactional
    public BookingResponse createBooking(UUID userId, CreateBookingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

        if (!service.getIsActive() || !service.getIsApproved()) {
            throw new RuntimeException("Услуга недоступна для бронирования");
        }

        if (bookingRepository.existsActiveBookingForServiceOnDate(service.getId(), request.getEventDate())) {
            throw new RuntimeException("Выбранная дата уже занята. Пожалуйста, выберите другую дату.");
        }

        boolean isBlockedByPartner = availabilityRepository
                .findByServiceIdAndDate(service.getId(), request.getEventDate())
                .map(a -> a.getStatus() == AvailabilityStatus.BLOCKED)
                .orElse(false);

        if (isBlockedByPartner) {
            throw new RuntimeException("Выбранная дата недоступна. Партнёр заблокировал эту дату.");
        }

        Partner partner = service.getPartner();

        Booking booking = Booking.builder()
                .user(user)
                .service(service)
                .partner(partner)
                .eventDate(request.getEventDate())
                .eventTime(request.getEventTime())
                .guestsCount(request.getGuestsCount())
                .notes(request.getNotes())
                .extraParams(request.getExtraParams())
                .totalPrice(service.getPriceFrom())
                .status(BookingStatus.PENDING_CONFIRMATION)
                .clientConfirmed(false)
                .partnerConfirmed(false)
                .expiresAt(LocalDateTime.now().plusHours(BOOKING_EXPIRY_HOURS))
                .build();

        Booking saved = bookingRepository.save(booking);

        service.setBookingsCount(service.getBookingsCount() + 1);
        serviceRepository.save(service);

        return mapToResponse(saved);
    }

    /**
     * Возвращает список бронирований клиента с опциональной фильтрацией по статусу.
     */
    public Page<BookingResponse> getMyBookings(UUID userId, BookingStatus status, Pageable pageable) {
        Page<Booking> bookings = status != null
                ? bookingRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable)
                : bookingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return bookings.map(this::mapToResponse);
    }

    /**
     * История бронирований клиента с расширенной фильтрацией.
     * Поддерживает фильтрацию по статусу, категории, диапазону дат создания и мероприятия.
     */
    public Page<BookingResponse> getMyBookingHistory(
            UUID userId,
            BookingHistoryFilter filter,
            Pageable pageable) {

        Page<Booking> bookings = bookingRepository.findByUserIdWithFilters(
                userId,
                filter.getStatus(),
                filter.getCategoryId(),
                filter.getCreatedFrom(),
                filter.getCreatedTo(),
                filter.getEventFrom(),
                filter.getEventTo(),
                pageable
        );

        return bookings.map(this::mapToResponse);
    }

    /**
     * Возвращает детали конкретного бронирования.
     * Доступно только клиенту-владельцу.
     */
    public BookingResponse getBookingById(UUID userId, UUID bookingId) {
        Booking booking = findBookingOrThrow(bookingId);

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("У вас нет доступа к этому бронированию");
        }

        return mapToResponse(booking);
    }

    /**
     * Отменяет бронирование клиентом.
     * Отменить можно только бронирования со статусом PENDING_CONFIRMATION или CONFIRMED.
     */
    @Transactional
    public BookingResponse cancelBooking(UUID userId, UUID bookingId) {
        Booking booking = findBookingOrThrow(bookingId);

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("У вас нет доступа к этому бронированию");
        }

        if (booking.getStatus() != BookingStatus.PENDING_CONFIRMATION
                && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException(
                    "Отменить можно только бронирование со статусом PENDING_CONFIRMATION или CONFIRMED. " +
                            "Текущий статус: " + booking.getStatus()
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());

        return mapToResponse(bookingRepository.save(booking));
    }

    /**
     * Клиент подтверждает, что услуга оказана.
     * Доступно только для бронирований со статусом CONFIRMED.
     * Если партнёр уже подтвердил — статус меняется на COMPLETED.
     */
    @Transactional
    public BookingResponse clientConfirmCompletion(UUID userId, UUID bookingId) {
        Booking booking = findBookingOrThrow(bookingId);

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("У вас нет доступа к этому бронированию");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException(
                    "Подтвердить завершение можно только для бронирования со статусом CONFIRMED. " +
                            "Текущий статус: " + booking.getStatus()
            );
        }

        if (Boolean.TRUE.equals(booking.getClientConfirmed())) {
            throw new RuntimeException("Вы уже подтвердили завершение сделки");
        }

        booking.setClientConfirmed(true);
        booking.setClientConfirmedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(booking.getPartnerConfirmed())) {
            completeBooking(booking);
        }

        return mapToResponse(bookingRepository.save(booking));
    }

    /**
     * Возвращает список бронирований партнёра с опциональной фильтрацией по статусу.
     */
    public Page<BookingResponse> getPartnerBookings(UUID userId, BookingStatus status, Pageable pageable) {
        Partner partner = findPartnerOrThrow(userId);

        Page<Booking> bookings = status != null
                ? bookingRepository.findByPartnerIdAndStatusOrderByCreatedAtDesc(partner.getId(), status, pageable)
                : bookingRepository.findByPartnerIdOrderByCreatedAtDesc(partner.getId(), pageable);

        return bookings.map(this::mapToResponse);
    }

    /**
     * Возвращает детали бронирования.
     * Доступно только партнёру-владельцу услуги.
     */
    public BookingResponse getPartnerBookingById(UUID userId, UUID bookingId) {
        Partner partner = findPartnerOrThrow(userId);
        Booking booking = findBookingOrThrow(bookingId);

        if (!booking.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("У вас нет доступа к этому бронированию");
        }

        return mapToResponse(booking);
    }

    /**
     * Партнёр подтверждает бронирование.
     * Можно подтвердить только бронирование со статусом PENDING_CONFIRMATION.
     */
    @Transactional
    public BookingResponse confirmBooking(UUID userId, UUID bookingId) {
        Partner partner = findPartnerOrThrow(userId);
        Booking booking = findBookingOrThrow(bookingId);

        if (!booking.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("У вас нет доступа к этому бронированию");
        }

        if (booking.getStatus() != BookingStatus.PENDING_CONFIRMATION) {
            throw new RuntimeException(
                    "Подтвердить можно только бронирование со статусом PENDING_CONFIRMATION. " +
                            "Текущий статус: " + booking.getStatus()
            );
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmedAt(LocalDateTime.now());

        return mapToResponse(bookingRepository.save(booking));
    }

    /**
     * Партнёр отклоняет бронирование.
     * Можно отклонить только бронирование со статусом PENDING_CONFIRMATION.
     */
    @Transactional
    public BookingResponse rejectBooking(UUID userId, UUID bookingId, RejectBookingRequest request) {
        Partner partner = findPartnerOrThrow(userId);
        Booking booking = findBookingOrThrow(bookingId);

        if (!booking.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("У вас нет доступа к этому бронированию");
        }

        if (booking.getStatus() != BookingStatus.PENDING_CONFIRMATION) {
            throw new RuntimeException(
                    "Отклонить можно только бронирование со статусом PENDING_CONFIRMATION. " +
                            "Текущий статус: " + booking.getStatus()
            );
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setRejectionReason(request.getRejectionReason());
        booking.setRejectedAt(LocalDateTime.now());

        return mapToResponse(bookingRepository.save(booking));
    }

    /**
     * Партнёр подтверждает, что услуга оказана.
     * Доступно только для бронирований со статусом CONFIRMED.
     * Если клиент уже подтвердил — статус меняется на COMPLETED.
     */
    @Transactional
    public BookingResponse partnerConfirmCompletion(UUID userId, UUID bookingId) {
        Partner partner = findPartnerOrThrow(userId);
        Booking booking = findBookingOrThrow(bookingId);

        if (!booking.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("У вас нет доступа к этому бронированию");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException(
                    "Подтвердить завершение можно только для бронирования со статусом CONFIRMED. " +
                            "Текущий статус: " + booking.getStatus()
            );
        }

        if (Boolean.TRUE.equals(booking.getPartnerConfirmed())) {
            throw new RuntimeException("Вы уже подтвердили завершение сделки");
        }

        booking.setPartnerConfirmed(true);
        booking.setPartnerConfirmedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(booking.getClientConfirmed())) {
            completeBooking(booking);
        }

        return mapToResponse(bookingRepository.save(booking));
    }

    /**
     * Возвращает бронирования партнёра за период (для календаря).
     */
    public List<BookingResponse> getPartnerCalendar(UUID userId, LocalDate from, LocalDate to) {
        Partner partner = findPartnerOrThrow(userId);

        return bookingRepository
                .findByPartnerIdAndEventDateBetweenOrderByEventDateAsc(partner.getId(), from, to)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // ПУБЛИЧНЫЕ МЕТОДЫ (доступны всем)
    // =========================================================

    /**
     * Возвращает недоступные даты для услуги за указанный период.
     * Объединяет даты, заблокированные партнёром, и даты с активными бронированиями.
     */
    public UnavailableDatesResponse getUnavailableDates(UUID serviceId, LocalDate from, LocalDate to) {
        serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

        List<LocalDate> blockedByPartner = availabilityRepository
                .findByServiceIdAndDateBetweenOrderByDateAsc(serviceId, from, to)
                .stream()
                .filter(a -> a.getStatus() == AvailabilityStatus.BLOCKED)
                .map(ServiceAvailability::getDate)
                .collect(Collectors.toList());

        List<LocalDate> bookedDates = bookingRepository
                .findBookedDatesByServiceIdAndPeriod(serviceId, from, to);

        List<LocalDate> allUnavailable = Stream
                .concat(blockedByPartner.stream(), bookedDates.stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return UnavailableDatesResponse.builder()
                .blockedByPartner(blockedByPartner)
                .bookedDates(bookedDates)
                .allUnavailableDates(allUnavailable)
                .build();
    }

    // =========================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // =========================================================

    /**
     * Переводит бронирование в статус COMPLETED.
     * Вызывается когда оба участника подтвердили завершение сделки.
     */
    private void completeBooking(Booking booking) {
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedAt(LocalDateTime.now());
    }

    private Booking findBookingOrThrow(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));
    }

    private Partner findPartnerOrThrow(UUID userId) {
        return partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Партнёр не найден"));
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .userFullName(booking.getUser().getFullName())
                .userPhone(booking.getUser().getPhone())
                .userEmail(booking.getUser().getEmail())
                .serviceId(booking.getService().getId())
                .serviceName(booking.getService().getName())
                .serviceCategory(booking.getService().getCategory().getNameRu())
                .serviceThumbnail(booking.getService().getThumbnail())
                .partnerId(booking.getPartner().getId())
                .partnerCompanyName(booking.getPartner().getCompanyName())
                .partnerPhone(booking.getPartner().getPhone())
                .eventDate(booking.getEventDate())
                .eventTime(booking.getEventTime())
                .status(booking.getStatus().name())
                .notes(booking.getNotes())
                .guestsCount(booking.getGuestsCount())
                .totalPrice(booking.getTotalPrice())
                .rejectionReason(booking.getRejectionReason())
                .extraParams(booking.getExtraParams())
                .clientConfirmed(booking.getClientConfirmed())
                .partnerConfirmed(booking.getPartnerConfirmed())
                .serviceUrl("/services/" + booking.getService().getSlug())
                // TODO: заменить на реальный chatId после реализации модуля чата
                .chatUrl(null)
                .expiresAt(booking.getExpiresAt())
                .confirmedAt(booking.getConfirmedAt())
                .rejectedAt(booking.getRejectedAt())
                .cancelledAt(booking.getCancelledAt())
                .clientConfirmedAt(booking.getClientConfirmedAt())
                .partnerConfirmedAt(booking.getPartnerConfirmedAt())
                .completedAt(booking.getCompletedAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}