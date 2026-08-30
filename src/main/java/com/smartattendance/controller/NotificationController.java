package com.smartattendance.controller;

import com.smartattendance.model.Attendance;
import com.smartattendance.model.AttendanceStatus;
import com.smartattendance.model.Staff;
import com.smartattendance.repository.AttendanceRepository;
import com.smartattendance.repository.StaffRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final StaffRepository staffRepository;
    private final AttendanceRepository attendanceRepository;

    public NotificationController(StaffRepository staffRepository, AttendanceRepository attendanceRepository) {
        this.staffRepository = staffRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @GetMapping("/my-alerts")
    public ResponseEntity<?> getMyNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String email = userDetails.getUsername();
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff profile not found"));

        List<Attendance> recentLogs = attendanceRepository.findByStaffOrderByDateDesc(staff);
        List<Map<String, String>> notifications = new ArrayList<>();

        if (!recentLogs.isEmpty()) {
            Attendance latest = recentLogs.get(0);
            if (latest.getStatus() == AttendanceStatus.VERIFIED) {
                notifications.add(Map.of(
                    "type", "SUCCESS",
                    "title", "Attendance Approved",
                    "message", "Your attendance for " + latest.getDate() + " was approved."
                ));
            } else if (latest.getStatus() == AttendanceStatus.REJECTED) {
                notifications.add(Map.of(
                    "type", "DANGER",
                    "title", "Attendance Rejected",
                    "message", "Your attendance for " + latest.getDate() + " was rejected by Admin/HOD."
                ));
            } else if (latest.getStatus() == AttendanceStatus.PENDING) {
                notifications.add(Map.of(
                    "type", "INFO",
                    "title", "Verification Pending",
                    "message", "Your attendance for " + latest.getDate() + " is currently pending approval."
                ));
            }
        }

        return ResponseEntity.ok(notifications);
    }
}
