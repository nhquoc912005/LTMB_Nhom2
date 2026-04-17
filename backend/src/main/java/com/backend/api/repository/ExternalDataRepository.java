package com.backend.api.repository;

import com.backend.api.entity.ExternalData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExternalDataRepository extends JpaRepository<ExternalData, Long> {
    Page<ExternalData> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
