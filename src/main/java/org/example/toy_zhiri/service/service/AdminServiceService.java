package org.example.toy_zhiri.service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.toy_zhiri.admin.dto.AdminChangeServiceActiveStatusRequest;
import org.example.toy_zhiri.admin.dto.AdminChangeServiceApprovalStatusRequest;
import org.example.toy_zhiri.admin.dto.MessageResponse;
import org.example.toy_zhiri.exception.NotFoundException;
import org.example.toy_zhiri.notification.enums.NotificationType;
import org.example.toy_zhiri.notification.enums.RelatedEntityType;
import org.example.toy_zhiri.notification.service.NotificationService;
import org.example.toy_zhiri.service.dto.ServiceResponse;
import org.example.toy_zhiri.service.entity.Service;
import org.example.toy_zhiri.service.repository.ServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AdminServiceService {
    private final ServiceRepository serviceRepository;
    private final ServiceService serviceService;
    private final NotificationService notificationService;

    /**
     * Получает список всех услуг (включая неодобренные и неактивные).
     *
     * @param pageable параметры пагинации
     * @return страница с услугами
     */
    public Page<ServiceResponse> getAllServices(Pageable pageable) {
        Page<Service> services = serviceRepository.findAll(pageable);
        return services.map(service -> serviceService.getServiceById(service.getId(), null));
    }

    /**
     * Получает список услуг, ожидающих одобрения.
     *
     * @return список услуг со статусом isApproved = false
     */
    public List<ServiceResponse> getPendingServices() {
        List<Service> services = serviceRepository.findByIsApprovedFalse();
        return services.stream()
                .map(service -> serviceService.getServiceById(service.getId(), null))
                .toList();
    }

    /**
     * Изменяет статус активности услуги.
     *
     * @param serviceId идентификатор услуги
     * @param request статус активности
     * @return обновленная информация об услуге
     * @throws NotFoundException если услуга не найдена
     */
    @Transactional
    public ServiceResponse changeActiveStatus(UUID serviceId, AdminChangeServiceActiveStatusRequest request) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Услуга с ID " + serviceId + " не найдена"));

        service.setIsActive(request.getIsActive());
        serviceRepository.save(service);

        return serviceService.getServiceById(serviceId, null);
    }

    /**
     * Изменяет статус одобрения услуги.
     *
     * @param serviceId идентификатор услуги
     * @param request статус одобрения и причина отказа (если есть)
     * @return обновленная информация об услуге
     * @throws NotFoundException если услуга не найдена
     */
    @Transactional
    public ServiceResponse changeApprovalStatus(UUID serviceId, AdminChangeServiceApprovalStatusRequest request) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Услуга с ID " + serviceId + " не найдена"));

        service.setIsApproved(request.getIsApproved());

        // Если услуга одобрена, делаем её активной
        if (request.getIsApproved()) {
            service.setIsActive(true);
        }

        serviceRepository.save(service);

        // Уведомление партнёру об одобрении/отклонении услуги
        UUID partnerUserId = service.getPartner().getUser().getId();
        if (request.getIsApproved()) {
            notificationService.send(
                    partnerUserId,
                    NotificationType.SERVICE_APPROVED,
                    "Услуга одобрена",
                    "Ваша услуга «" + service.getName() + "» одобрена и опубликована",
                    RelatedEntityType.SERVICE,
                    service.getId()
            );
        } else {
            notificationService.send(
                    partnerUserId,
                    NotificationType.SERVICE_REJECTED,
                    "Услуга отклонена",
                    "Ваша услуга «" + service.getName() + "» отклонена модератором",
                    RelatedEntityType.SERVICE,
                    service.getId()
            );
        }

        return serviceService.getServiceById(serviceId, null);
    }

    /**
     * Удаляет услугу (только для администратора).
     *
     * @param serviceId идентификатор услуги
     * @return сообщение об успешном удалении
     * @throws NotFoundException если услуга не найдена
     */
    @Transactional
    public MessageResponse deleteService(UUID serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Услуга с ID " + serviceId + " не найдена"));

        String serviceName = service.getName();
        serviceRepository.delete(service);

        return MessageResponse.builder()
                .message("Услуга \"" + serviceName + "\" успешно удалена")
                .build();
    }

    /**
     * Получает детальную информацию об услуге.
     *
     * @param serviceId идентификатор услуги
     * @return информация об услуге
     * @throws NotFoundException если услуга не найдена
     */
    public ServiceResponse getServiceById(UUID serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Услуга с ID " + serviceId + " не найдена"));

        return serviceService.getServiceById(serviceId, null);
    }
}