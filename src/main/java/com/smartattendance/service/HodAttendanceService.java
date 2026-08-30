package com.smartattendance.service;

import com.smartattendance.model.Attendance;
import com.smartattendance.model.AttendanceStatus;
import com.smartattendance.model.AuditAction;
import com.smartattendance.model.Role;
import com.smartattendance.model.Staff;
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
public class HodAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StaffRepository staffRepository;
    private final EntityManager entityManager;

    @Value("${app.audit.database-trigger-enabled:true}")
    private boolean databaseTriggerEnabled;

    public HodAttendanceService(AttendanceRepository attendanceRepository, StaffRepository staffRepository,
                                EntityManager entityManager) {
        this.attendanceRepository = attendanceRepository;
        this.staffRepository = staffRepository;
        this.entityManager = entityManager;
    }

    public List<Attendance> getDepartmentPendingRecords(String hodEmail) {
        Staff hod = findHod(hodEmail);
        return attendanceRepository.findByStatusAndStaffDepartment(AttendanceStatus.PENDING, hod.getDepartment());
    }

    @Transactional
    public void processDepartmentVerification(Long attendanceId, String actionString, String hodEmail) {
        Staff hod = findHod(hodEmail);
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance record not found."));

        if (hod.getRole() != Role.ADMIN
                && !attendance.getStaff().getDepartment().equalsIgnoreCase(hod.getDepartment())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only verify attendance for staff in your department.");
        }
        if (attendance.getStatus() != AttendanceStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Attendance has already been processed.");
        }

        AuditAction action;
        try {
            action = AuditAction.valueOf(actionString.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid action. Use APPROVED or REJECTED.");
        }
        if (action != AuditAction.APPROVED && action != AuditAction.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid action. Use APPROVED or REJECTED.");
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
                    .setParameter("operatorId", hod.getId())
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

    private Staff findHod(String email) {
        return staffRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "HOD profile not found."));
    }
}
