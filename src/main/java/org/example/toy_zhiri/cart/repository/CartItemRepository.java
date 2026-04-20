package org.example.toy_zhiri.cart.repository;

import org.example.toy_zhiri.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с элементами корзины пользователей.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    /**
     * Возвращает все элементы корзины пользователя.
     *
     * @param userId идентификатор пользователя
     * @return List<CartItem> список элементов корзины
     */
    List<CartItem> findByUserId(UUID userId);

    /**
     * Находит элемент корзины по паре (user, service).
     *
     * @param userId    идентификатор пользователя
     * @param serviceId идентификатор услуги
     * @return Optional с найденным элементом корзины
     */
    Optional<CartItem> findByUserIdAndServiceId(UUID userId, UUID serviceId);

    /**
     * Удаляет все элементы корзины пользователя.
     *
     * @param userId идентификатор пользователя
     */
    void deleteByUserId(UUID userId);

    /**
     * Удаляет элемент корзины по паре (user, service).
     *
     * @param userId    идентификатор пользователя
     * @param serviceId идентификатор услуги
     */
    void deleteByUserIdAndServiceId(UUID userId, UUID serviceId);
}