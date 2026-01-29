package org.example.toy_zhiri.service.repository;

import org.example.toy_zhiri.service.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    Page<Favorite> findByUserId(UUID userId, Pageable pageable);
    Optional<Favorite> findByUserIdAndServiceId(UUID userId, UUID serviceId);
    boolean existsByUserIdAndServiceId(UUID userId, UUID serviceId);
    void deleteByUserIdAndServiceId(UUID userId, UUID serviceId);
}