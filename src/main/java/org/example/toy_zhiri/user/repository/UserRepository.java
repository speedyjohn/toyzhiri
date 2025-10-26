package org.example.toy_zhiri.user.repository;

import org.example.toy_zhiri.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностями User.
 */
public interface UserRepository extends JpaRepository<User, UUID> {
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
}
