package com.smartattendance.repository;

import com.smartattendance.model.SkinAnalysisRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkinAnalysisRepository extends JpaRepository<SkinAnalysisRecord, Long> {

    List<SkinAnalysisRecord> findByStaffIdOrderByCreatedAtDesc(Long staffId);

    List<SkinAnalysisRecord> findByStaffEmailOrderByCreatedAtDesc(String email);
}
