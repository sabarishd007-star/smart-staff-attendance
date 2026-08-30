package com.smartattendance.controller;

import com.smartattendance.dto.AuthDTOs.VerificationRequest;
import com.smartattendance.model.Attendance;
import com.smartattendance.model.AttendanceStatus;
import com.smartattendance.model.Staff;
import com.smartattendance.repository.AttendanceRepository;
import com.smartattendance.repository.StaffRepository;
import com.smartattendance.service.HodAttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/hod")
@PreAuthorize("hasAnyRole('HOD', 'ADMIN')")
public class HodController {

    private final HodAttendanceService hodAttendanceService;
    private final StaffRepository staffRepository;
    private final AttendanceRepository attendanceRepository;

    public HodController(HodAttendanceService hodAttendanceService, StaffRepository staffRepository, AttendanceRepository attendanceRepository) {
        this.hodAttendanceService = hodAttendanceService;
        this.staffRepository = staffRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Attendance>> getDepartmentPending(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(hodAttendanceService.getDepartmentPendingRecords(userDetails.getUsername()));
    }

    @PostMapping("/verify/{id}")
    public ResponseEntity<Void> verifyDepartmentAttendance(
            @PathVariable Long id,
            @RequestBody VerificationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        hodAttendanceService.processDepartmentVerification(id, request.getAction(), userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getHodDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : "";
        Staff hod = staffRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "HOD profile not found"));

        String department = hod.getDepartment();

        List<Staff> deptStaff = staffRepository.findByDepartment(department);
        List<Attendance> deptAttendance = attendanceRepository.findByStaffDepartmentOrderByDateDesc(department);

        long totalStaff = deptStaff.size();
        long presentCount = deptAttendance.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.VERIFIED)
                .count();
        long pendingCount = deptAttendance.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PENDING)
                .count();
        long rejectedCount = deptAttendance.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.REJECTED)
                .count();

        Map<String, Object> response = new HashMap<>();
        response.put("department", department);
        response.put("totalStaff", totalStaff);
        response.put("presentCount", presentCount);
        response.put("pendingCount", pendingCount);
        response.put("rejectedCount", rejectedCount);
        response.put("recentLogs", deptAttendance);

        return ResponseEntity.ok(response);
    }
}
