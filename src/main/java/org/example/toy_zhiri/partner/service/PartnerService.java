package org.example.toy_zhiri.partner.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

    /**
     * Регистрирует новую заявку на партнерство с полным профилем.
     *
     * @param userId идентификатор пользователя
     * @param request данные для регистрации партнера (включая обязательные поля)
     * @return PartnerResponse информация о созданной заявке
     * @throws RuntimeException если пользователь не найден, заявка уже существует или БИН уже используется
     */
    @Transactional
    public PartnerResponse registerPartner(UUID userId, PartnerRegistrationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (partnerRepository.existsByUserId(userId)) {
            throw new RuntimeException("Заявка на партнерство уже существует");
        }

        if (partnerRepository.existsByBin(request.getBin())) {
            throw new RuntimeException("Партнер с таким ИНН уже зарегистрирован");
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
     *
     * @param userId идентификатор пользователя
     * @return PartnerResponse информация о заявке партнера
     * @throws RuntimeException если заявка не найдена
     */
    public PartnerResponse getPartnerByUserId(UUID userId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Заявка на партнерство не найдена"));

        return mapToResponse(partner);
    }

    /**
     * Получает список всех заявок со статусом ожидания.
     *
     * @return List<PartnerResponse> список заявок на рассмотрении, отсортированных по дате создания
     */
    public List<PartnerResponse> getPendingPartners() {
        return partnerRepository.findAllByStatusOrderByCreatedAtDesc(PartnerStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получает список всех заявок с указанным статусом.
     *
     * @param status статус партнера для фильтрации
     * @return List<PartnerResponse> список заявок с запрошенным статусом
     */
    public List<PartnerResponse> getAllPartnersByStatus(PartnerStatus status) {
        return partnerRepository.findAllByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Обрабатывает заявку на партнерство (одобряет или отклоняет).
     *
     * @param partnerId идентификатор заявки партнера
     * @param adminId идентификатор администратора
     * @param request данные решения по заявке
     * @return PartnerResponse обновленная информация о заявке
     * @throws RuntimeException если заявка не найдена, уже обработана, администратор не найден или отсутствует причина отклонения
     */
    @Transactional
    public PartnerResponse approvePartner(UUID partnerId, UUID adminId, PartnerApprovalRequest request) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        if (partner.getStatus() != PartnerStatus.PENDING) {
            throw new RuntimeException("Заявка уже обработана");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Администратор не найден"));

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
                throw new RuntimeException("Причина отклонения обязательна");
            }

            partner.setStatus(PartnerStatus.REJECTED);
            partner.setRejectionReason(request.getRejectionReason());
            partner.setApprovedBy(admin);
        }

        Partner updatedPartner = partnerRepository.save(partner);
        return mapToResponse(updatedPartner);
    }

    /**
     * Преобразует сущность Partner в DTO PartnerResponse.
     *
     * @param partner сущность партнера
     * @return PartnerResponse DTO с информацией о партнере
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