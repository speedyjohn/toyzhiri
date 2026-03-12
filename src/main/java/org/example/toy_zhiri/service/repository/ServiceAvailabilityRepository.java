package org.example.toy_zhiri.service.repository;

import org.example.toy_zhiri.service.entity.ServiceAvailability;
import org.example.toy_zhiri.service.enums.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceAvailabilityRepository extends JpaRepository<ServiceAvailability, UUID> {

    List<ServiceAvailability> findByServiceIdOrderByDateAsc(UUID serviceId);

    List<ServiceAvailability> findByServiceIdAndDateBetweenOrderByDateAsc(
            UUID serviceId,
            LocalDate from,
            LocalDate to
    );

    Optional<ServiceAvailability> findByServiceIdAndDate(UUID serviceId, LocalDate date);

    boolean existsByServiceIdAndDate(UUID serviceId, LocalDate date);

    /**
     * Проверяет, доступна ли конкретная дата для услуги.
     * Дата считается доступной, если для неё есть запись со статусом AVAILABLE.
     */
    @Query("SELECT COUNT(a) > 0 FROM ServiceAvailability a " +
            "WHERE a.service.id = :serviceId AND a.date = :date AND a.status = :status")
    boolean existsByServiceIdAndDateAndStatus(
            @Param("serviceId") UUID serviceId,
            @Param("date") LocalDate date,
            @Param("status") AvailabilityStatus status
    );

    /**
     * Возвращает ID услуг, у которых указанная дата помечена как AVAILABLE.
     * Используется при фильтрации каталога по дате.
     */
    @Query("SELECT a.service.id FROM ServiceAvailability a " +
            "WHERE a.date = :date AND a.status = 'AVAILABLE'")
    List<UUID> findServiceIdsByAvailableDate(@Param("date") LocalDate date);

    /**
     * Возвращает ID услуг, у которых хотя бы одна из указанных дат помечена как AVAILABLE.
     */
    @Query("SELECT DISTINCT a.service.id FROM ServiceAvailability a " +
            "WHERE a.date IN :dates AND a.status = 'AVAILABLE'")
    List<UUID> findServiceIdsByAvailableDates(@Param("dates") List<LocalDate> dates);
}