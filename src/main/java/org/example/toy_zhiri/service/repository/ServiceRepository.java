package org.example.toy_zhiri.service.repository;

import org.example.toy_zhiri.service.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID>, JpaSpecificationExecutor<Service> {
    Page<Service> findByIsActiveTrueAndIsApprovedTrue(Pageable pageable);
    Page<Service> findByCategoryIdAndIsActiveTrueAndIsApprovedTrue(UUID categoryId, Pageable pageable);
    Page<Service> findByPartnerIdAndIsActiveTrue(UUID partnerId, Pageable pageable);
    List<Service> findByPartnerId(UUID partnerId);
}