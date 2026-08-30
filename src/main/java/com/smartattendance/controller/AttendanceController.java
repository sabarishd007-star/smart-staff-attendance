package com.smartattendance.controller;

import com.smartattendance.dto.AttendanceHistoryResponse;
import com.smartattendance.dto.AuthDTOs.MarkAttendanceRequest;
import com.smartattendance.model.Attendance;
import com.smartattendance.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/mark")
    public ResponseEntity<Attendance> markAttendance(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MarkAttendanceRequest request) {

        Attendance record = attendanceService.markAttendance(
                userDetails.getUsername(),
                request.getLatitude(),
                request.getLongitude()
        );

        return ResponseEntity.ok(record);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getMyAttendanceHistory(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        AttendanceHistoryResponse response = attendanceService.getMyAttendanceHistory(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
