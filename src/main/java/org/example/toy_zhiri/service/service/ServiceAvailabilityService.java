package org.example.toy_zhiri.service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.service.dto.AvailabilityResponse;
import org.example.toy_zhiri.service.dto.SetAvailabilityRequest;
import org.example.toy_zhiri.service.entity.Service;
import org.example.toy_zhiri.service.entity.ServiceAvailability;
import org.example.toy_zhiri.service.enums.AvailabilityStatus;
import org.example.toy_zhiri.service.repository.ServiceAvailabilityRepository;
import org.example.toy_zhiri.service.repository.ServiceRepository;
import org.example.toy_zhiri.partner.entity.Partner;
import org.example.toy_zhiri.partner.repository.PartnerRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceAvailabilityService {
    private final ServiceAvailabilityRepository availabilityRepository;
    private final ServiceRepository serviceRepository;
    private final PartnerRepository partnerRepository;

    /**
     * Возвращает расписание доступности услуги за указанный период.
     * Публичный метод — доступен всем (клиентам, чтобы видеть свободные даты).
     *
     * @param serviceId ID услуги
     * @param from      начало периода
     * @param to        конец периода
     * @return список записей доступности
     */
    public List<AvailabilityResponse> getAvailability(UUID serviceId, LocalDate from, LocalDate to) {
        serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

        return availabilityRepository
                .findByServiceIdAndDateBetweenOrderByDateAsc(serviceId, from, to)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Устанавливает статус доступности для списка дат.
     * Если запись на дату уже есть — обновляет, иначе — создаёт.
     * Только партнёр-владелец услуги может управлять доступностью.
     *
     * @param userId  ID текущего пользователя (партнёра)
     * @param serviceId ID услуги
     * @param request   статус и список дат
     * @return обновлённые записи доступности
     */
    @Transactional
    public List<AvailabilityResponse> setAvailability(
            UUID userId,
            UUID serviceId,
            SetAvailabilityRequest request) {

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Партнёр не найден"));

        if (!service.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("У вас нет прав на управление этой услугой");
        }

        AvailabilityStatus status;
        try {
            status = AvailabilityStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Некорректный статус: " + request.getStatus() +
                    ". Допустимые значения: AVAILABLE, BLOCKED");
        }

        List<ServiceAvailability> saved = request.getDates().stream()
                .map(date -> upsertAvailability(service, date, status, request.getNote()))
                .collect(Collectors.toList());

        return saved.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Удаляет запись доступности для конкретной даты.
     * Используется когда партнёр хочет убрать дату из расписания полностью.
     *
     * @param userId    ID текущего пользователя (партнёра)
     * @param serviceId ID услуги
     * @param date      дата для удаления
     */
    @Transactional
    public void deleteAvailability(UUID userId, UUID serviceId, LocalDate date) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Партнёр не найден"));

        if (!service.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("У вас нет прав на управление этой услугой");
        }

        ServiceAvailability availability = availabilityRepository
                .findByServiceIdAndDate(serviceId, date)
                .orElseThrow(() -> new RuntimeException("Запись на дату " + date + " не найдена"));

        availabilityRepository.delete(availability);
    }

    /**
     * Создаёт или обновляет запись доступности для одной даты.
     */
    private ServiceAvailability upsertAvailability(
            Service service,
            LocalDate date,
            AvailabilityStatus status,
            String note) {

        ServiceAvailability availability = availabilityRepository
                .findByServiceIdAndDate(service.getId(), date)
                .orElse(ServiceAvailability.builder()
                        .service(service)
                        .date(date)
                        .build());

        availability.setStatus(status);
        availability.setNote(note);
        return availabilityRepository.save(availability);
    }

    private AvailabilityResponse mapToResponse(ServiceAvailability availability) {
        return AvailabilityResponse.builder()
                .id(availability.getId())
                .serviceId(availability.getService().getId())
                .date(availability.getDate())
                .status(availability.getStatus().name())
                .note(availability.getNote())
                .build();
    }
}