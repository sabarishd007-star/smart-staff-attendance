package com.smartattendance.service;

import com.smartattendance.model.*;
import com.smartattendance.repository.AttendanceRepository;
import com.smartattendance.repository.StaffRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StaffRepository staffRepository;
    private final EntityManager entityManager;

    @Value("${app.audit.database-trigger-enabled:true}")
    private boolean databaseTriggerEnabled;

    public AdminAttendanceService(AttendanceRepository attendanceRepository, StaffRepository staffRepository,
                                  EntityManager entityManager) {
        this.attendanceRepository = attendanceRepository;
        this.staffRepository = staffRepository;
        this.entityManager = entityManager;
    }

    public List<Attendance> getPendingRecords() {
        return attendanceRepository.findByStatus(AttendanceStatus.PENDING);
    }

    @Transactional
    public void processVerification(Long attendanceId, String actionStr, String adminEmail) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance record not found."));

        Staff admin = staffRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin profile not found."));

        AuditAction action;
        try {
            action = AuditAction.valueOf(actionStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification action. Use APPROVED or REJECTED.");
        }
        if (action != AuditAction.APPROVED && action != AuditAction.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification action. Use APPROVED or REJECTED.");
        }

        if (!databaseTriggerEnabled) {
            attendance.setStatus(action == AuditAction.APPROVED
                    ? AttendanceStatus.VERIFIED : AttendanceStatus.REJECTED);
            attendanceRepository.save(attendance);
            entityManager.flush();
            return;
        }

        try {
            entityManager.createNativeQuery("SET @current_operator_staff_id = :operatorId")
                    .setParameter("operatorId", admin.getId())
                    .executeUpdate();
            attendance.setStatus(action == AuditAction.APPROVED
                    ? AttendanceStatus.VERIFIED : AttendanceStatus.REJECTED);
            attendanceRepository.save(attendance);
            // Execute the UPDATE while the connection-scoped operator context is set.
            entityManager.flush();
        } finally {
            // Connections are pooled; clear the session variable before it can be reused.
            entityManager.createNativeQuery("SET @current_operator_staff_id = NULL").executeUpdate();
        }
    }
}
