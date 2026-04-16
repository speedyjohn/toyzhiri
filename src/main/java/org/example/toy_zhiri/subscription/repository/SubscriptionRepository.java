package org.example.toy_zhiri.subscription.repository;

import org.example.toy_zhiri.subscription.entity.Subscription;
import org.example.toy_zhiri.subscription.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByServiceIdAndStatus(UUID serviceId, SubscriptionStatus status);

    Page<Subscription> findByPartnerIdOrderByCreatedAtDesc(UUID partnerId, Pageable pageable);

    List<Subscription> findByStatusAndExpiresAtBefore(SubscriptionStatus status, LocalDateTime now);

    // Подписки с заданным статусом, истекающие в указанный промежуток времени.
    // Используется джобой для поиска ACTIVE-подписок, до истечения которых остался 1 день.
    List<Subscription> findByStatusAndExpiresAtBetween(SubscriptionStatus status, LocalDateTime from, LocalDateTime to);

    boolean existsByServiceIdAndStatus(UUID serviceId, SubscriptionStatus status);

    Page<Subscription> findByServiceIdOrderByCreatedAtDesc(UUID serviceId, Pageable pageable);
}