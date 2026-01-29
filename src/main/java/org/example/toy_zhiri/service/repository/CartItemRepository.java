package org.example.toy_zhiri.service.repository;

import org.example.toy_zhiri.service.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findByUserId(UUID userId);
    Optional<CartItem> findByUserIdAndServiceId(UUID userId, UUID serviceId);
    void deleteByUserId(UUID userId);
    void deleteByUserIdAndServiceId(UUID userId, UUID serviceId);
}