package org.example.toy_zhiri.partner.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.exception.BadRequestException;
import org.example.toy_zhiri.exception.ConflictException;
import org.example.toy_zhiri.exception.InvalidStateException;
import org.example.toy_zhiri.exception.NotFoundException;
import org.example.toy_zhiri.notification.enums.NotificationType;
import org.example.toy_zhiri.notification.service.NotificationService;
import org.example.toy_zhiri.partner.dto.PartnerApprovalRequest;
import org.example.toy_zhiri.partner.dto.PartnerRegistrationRequest;
import org.example.toy_zhiri.partner.dto.PartnerResponse;
import org.example.toy_zhiri.partner.entity.Partner;
import org.example.toy_zhiri.partner.enums.PartnerStatus;
import org.example.toy_zhiri.partner.repository.PartnerRepository;
import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.enums.UserRole;
import org.example.toy_zhiri.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервис для управления партнерами и обработки партнерских заявок.
 */
@Service
@RequiredArgsConstructor
public class PartnerService {
    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Регистрирует новую заявку на партнерство с полным профилем.
     */
    @Transactional
    public PartnerResponse registerPartner(UUID userId, PartnerRegistrationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (partnerRepository.existsByUserId(userId)) {
            throw new ConflictException("Заявка на партнерство уже существует");
        }

        if (partnerRepository.existsByBin(request.getBin())) {
            throw new ConflictException("Партнер с таким ИНН уже зарегистрирован");
        }

        Partner partner = Partner.builder()
                .user(user)
                .bin(request.getBin())
                .companyName(request.getCompanyName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .region(request.getRegion())
                .phone(request.getPhone())
                .email(request.getEmail())
                .whatsapp(request.getWhatsapp())
                .telegram(request.getTelegram())
                .instagram(request.getInstagram())
                .website(request.getWebsite())
                .logoUrl(request.getLogoUrl())
                .status(PartnerStatus.PENDING)
                .build();

        Partner savedPartner = partnerRepository.save(partner);

        return mapToResponse(savedPartner);
    }

    /**
     * Получает заявку партнера по идентификатору пользователя.
     */
    public PartnerResponse getPartnerByUserId(UUID userId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Заявка на партнерство не найдена"));

        return mapToResponse(partner);
    }

    /**
     * Получает список всех заявок со статусом ожидания.
     */
    public List<PartnerResponse> getPendingPartners() {
        return partnerRepository.findAllByStatusOrderByCreatedAtDesc(PartnerStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получает список всех заявок с указанным статусом.
     */
    public List<PartnerResponse> getAllPartnersByStatus(PartnerStatus status) {
        return partnerRepository.findAllByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Обрабатывает заявку на партнерство (одобряет или отклоняет).
     */
    @Transactional
    public PartnerResponse approvePartner(UUID partnerId, UUID adminId, PartnerApprovalRequest request) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new NotFoundException("Заявка не найдена"));

        if (partner.getStatus() != PartnerStatus.PENDING) {
            throw new InvalidStateException("Заявка уже обработана");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Администратор не найден"));

        if (request.getApproved()) {
            partner.setStatus(PartnerStatus.APPROVED);
            partner.setApprovedAt(LocalDateTime.now());
            partner.setApprovedBy(admin);
            partner.setRejectionReason(null);

            User user = partner.getUser();
            user.setRole(UserRole.PARTNER);
            userRepository.save(user);

        } else {
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
                throw new BadRequestException("Причина отклонения обязательна");
            }

            partner.setStatus(PartnerStatus.REJECTED);
            partner.setRejectionReason(request.getRejectionReason());
            partner.setApprovedBy(admin);
        }

        Partner updatedPartner = partnerRepository.save(partner);

        // Уведомление пользователю о решении по заявке
        UUID userId = partner.getUser().getId();
        if (request.getApproved()) {
            notificationService.send(
                    userId,
                    NotificationType.SYSTEM,
                    "Заявка одобрена",
                    "Ваша заявка на партнёрство одобрена! " +
                            "Теперь вы можете добавлять услуги и принимать заказы"
            );
        } else {
            notificationService.send(
                    userId,
                    NotificationType.SYSTEM,
                    "Заявка отклонена",
                    "Заявка на партнёрство отклонена. Причина: " +
                            request.getRejectionReason()
            );
        }

        return mapToResponse(updatedPartner);
    }

    /**
     * Преобразует сущность Partner в DTO PartnerResponse.
     */
    private PartnerResponse mapToResponse(Partner partner) {
        return PartnerResponse.builder()
                .id(partner.getId())
                .userId(partner.getUser().getId())
                .userEmail(partner.getUser().getEmail())
                .userFullName(partner.getUser().getFullName())
                .bin(partner.getBin())
                .status(partner.getStatus().name())
                .rejectionReason(partner.getRejectionReason())
                .createdAt(partner.getCreatedAt())
                .approvedAt(partner.getApprovedAt())
                .approvedByEmail(partner.getApprovedBy() != null ?
                        partner.getApprovedBy().getEmail() : null)
                .build();
    }


}