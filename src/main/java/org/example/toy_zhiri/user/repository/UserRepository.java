package org.example.toy_zhiri.user.repository;

import org.example.toy_zhiri.user.entity.User;
import org.example.toy_zhiri.user.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностями User.
 * Расширен интерфейсом JpaSpecificationExecutor для поддержки динамических запросов.
 */
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    /**
     * Находит пользователя по email.
     *
     * @param email email пользователя
     * @return Optional<User> пользователь, если найден
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверяет существование пользователя по email.
     *
     * @param email email для проверки
     * @return true если пользователь существует
     */
    boolean existsByEmail(String email);

    /**
     * Подсчитывает количество пользователей с определенной ролью.
     *
     * @param role роль для подсчета
     * @return количество пользователей с этой ролью
     */
    long countByRole(UserRole role);

    /**
     * Подсчитывает количество пользователей по статусу верификации email.
     *
     * @param emailVerified статус верификации
     * @return количество пользователей с этим статусом
     */
    long countByEmailVerified(Boolean emailVerified);

    /**
     * Подсчитывает количество пользователей по статусу активности.
     *
     * @param isActive статус активности
     * @return количество пользователей
     */
    long countByIsActive(Boolean isActive);
}