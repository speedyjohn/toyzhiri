package org.example.toy_zhiri.subscription.repository;

import org.example.toy_zhiri.subscription.entity.SubscriptionPlan;
import org.example.toy_zhiri.subscription.enums.SubscriptionPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
    List<SubscriptionPlan> findByStatusOrderByDisplayOrderAsc(SubscriptionPlanStatus status);
    Optional<SubscriptionPlan> findBySlug(String slug);
    boolean existsBySlug(String slug);
}