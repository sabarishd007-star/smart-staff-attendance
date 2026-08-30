package com.smartattendance.repository;

import com.smartattendance.model.LeaveRequest;
import com.smartattendance.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByStaffOrderByIdDesc(Staff staff);
}
