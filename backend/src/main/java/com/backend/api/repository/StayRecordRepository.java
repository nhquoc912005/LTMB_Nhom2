package com.backend.api.repository;

import com.backend.api.entity.StayRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StayRecordRepository extends JpaRepository<StayRecord, Integer> {
}
