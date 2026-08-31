package com.smartattendance.repository;

import com.smartattendance.model.AttendanceCorrectionRequest;
import com.smartattendance.model.CorrectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceCorrectionRequestRepository extends JpaRepository<AttendanceCorrectionRequest, Long> {
    List<AttendanceCorrectionRequest> findByStaffEmailOrderBySubmittedAtDesc(String email);
    List<AttendanceCorrectionRequest> findByStatusOrderBySubmittedAtDesc(CorrectionStatus status);
    List<AttendanceCorrectionRequest> findByStatusAndStaffDepartmentOrderBySubmittedAtDesc(CorrectionStatus status, String department);
    List<AttendanceCorrectionRequest> findByStaffId(Long staffId);
}
