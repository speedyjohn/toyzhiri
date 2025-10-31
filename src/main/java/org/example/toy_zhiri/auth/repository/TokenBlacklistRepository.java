package org.example.toy_zhiri.auth.repository;

import org.example.toy_zhiri.auth.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Репозиторий для работы с blacklist токенов.
 */
@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, UUID> {
    /**
     * Проверяет, находится ли токен в черном списке.
     *
     * @param token JWT токен
     * @return true если токен заблокирован
     */
    boolean existsByToken(String token);

    /**
     * Удаляет истекшие токены из blacklist.
     * Вызывается периодически для очистки БД.
     *
     * @param now текущая дата и время
     * @return int количество удаленных записей
     */
    @Modifying
    @Query("DELETE FROM TokenBlacklist tb WHERE tb.expiryDate < :now")
    int deleteExpiredTokens(LocalDateTime now);
}