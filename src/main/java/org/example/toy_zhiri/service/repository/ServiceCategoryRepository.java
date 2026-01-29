package org.example.toy_zhiri.service.repository;

import org.example.toy_zhiri.service.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, UUID> {
    List<ServiceCategory> findByIsActiveTrueOrderByDisplayOrderAsc();
    Optional<ServiceCategory> findBySlug(String slug);
}