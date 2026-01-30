package org.example.toy_zhiri.partner.repository;

import org.example.toy_zhiri.partner.entity.Partner;
import org.example.toy_zhiri.partner.enums.PartnerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностями Partner.
 */
@Repository
public interface PartnerRepository extends JpaRepository<Partner, UUID>, JpaSpecificationExecutor<Partner> {
    /**
     * Находит партнера по идентификатору пользователя.
     *
     * @param userId идентификатор пользователя
     * @return Optional<Partner> партнер, если найден
     */
    Optional<Partner> findByUserId(UUID userId);

    /**
     * Проверяет существование партнера по идентификатору пользователя.
     *
     * @param userId идентификатор пользователя
     * @return true если партнер существует
     */
    boolean existsByUserId(UUID userId);

    /**
     * Проверяет существование партнера по БИН.
     *
     * @param inn БИН для проверки
     * @return true если партнер с таким БИН существует
     */

    boolean existsByBin(String inn);

    /**
     * Находит всех партнеров с указанным статусом.
     *
     * @param status статус для фильтрации
     * @return список партнеров с заданным статусом
     */
    List<Partner> findAllByStatus(PartnerStatus status);

    /**
     * Находит всех партнеров с указанным статусом, отсортированных по дате создания.
     *
     * @param status статус для фильтрации
     * @return список партнеров, отсортированный по дате создания (новые первыми)
     */
    List<Partner> findAllByStatusOrderByCreatedAtDesc(PartnerStatus status);
}