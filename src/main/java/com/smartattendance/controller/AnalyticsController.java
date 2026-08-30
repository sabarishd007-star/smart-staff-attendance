package com.smartattendance.controller;

import com.smartattendance.model.AttendanceStatus;
import com.smartattendance.repository.AttendanceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
public class AnalyticsController {

    private final AttendanceRepository attendanceRepository;

    public AnalyticsController(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getAnalyticsSummary() {
        Map<String, Object> data = new HashMap<>();

        long approvedCount = attendanceRepository.countByStatus(AttendanceStatus.VERIFIED);
        long pendingCount = attendanceRepository.countByStatus(AttendanceStatus.PENDING);
        long rejectedCount = attendanceRepository.countByStatus(AttendanceStatus.REJECTED);

        List<Map<String, Object>> statusBreakdown = List.of(
            Map.of("name", "Approved", "value", approvedCount),
            Map.of("name", "Pending", "value", pendingCount),
            Map.of("name", "Rejected", "value", rejectedCount)
        );

        data.put("statusBreakdown", statusBreakdown);
        data.put("approvedCount", approvedCount);
        data.put("pendingCount", pendingCount);
        data.put("rejectedCount", rejectedCount);
        return ResponseEntity.ok(data);
    }
}
