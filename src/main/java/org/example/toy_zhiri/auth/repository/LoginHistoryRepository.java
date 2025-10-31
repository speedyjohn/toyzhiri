package org.example.toy_zhiri.auth.repository;

import org.example.toy_zhiri.auth.entity.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Репозиторий для работы с историей входов.
 */
@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, UUID> {
    /**
     * Находит историю входов пользователя с пагинацией.
     *
     * @param userId идентификатор пользователя
     * @param pageable параметры пагинации
     * @return Page<LoginHistory> страница с историей входов
     */
    Page<LoginHistory> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Подсчитывает общее количество входов пользователя.
     *
     * @param userId идентификатор пользователя
     * @param success только успешные входы
     * @return количество входов
     */
    long countByUserIdAndSuccess(UUID userId, Boolean success);

    /**
     * Находит последний успешный вход пользователя.
     *
     * @param userId идентификатор пользователя
     * @return последняя запись успешного входа
     */
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user.id = :userId " +
            "AND lh.loginType = 'LOGIN' AND lh.success = true " +
            "ORDER BY lh.createdAt DESC LIMIT 1")
    LoginHistory findLastSuccessfulLogin(UUID userId);
}