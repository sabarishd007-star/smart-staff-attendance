package com.smartattendance.controller;

import com.smartattendance.dto.AdminReportDto;
import com.smartattendance.dto.AuthDTOs.VerificationRequest;
import com.smartattendance.model.Attendance;
import com.smartattendance.model.Staff;
import com.smartattendance.repository.AttendanceRepository;
import com.smartattendance.repository.StaffRepository;
import com.smartattendance.service.AdminAttendanceService;
import com.smartattendance.service.ExcelExportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminAttendanceService adminAttendanceService;
    private final ExcelExportService excelExportService;
    private final AttendanceRepository attendanceRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(AdminAttendanceService adminAttendanceService,
                           ExcelExportService excelExportService,
                           AttendanceRepository attendanceRepository,
                           StaffRepository staffRepository,
                           PasswordEncoder passwordEncoder) {
        this.adminAttendanceService = adminAttendanceService;
        this.excelExportService = excelExportService;
        this.attendanceRepository = attendanceRepository;
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Attendance>> getPending() {
        return ResponseEntity.ok(adminAttendanceService.getPendingRecords());
    }

    @PostMapping("/verify/{id}")
    public ResponseEntity<Void> verifyAttendance(
            @PathVariable Long id,
            @RequestBody VerificationRequest request,
            @AuthenticationPrincipal UserDetails adminDetails) {

        adminAttendanceService.processVerification(id, request.getAction(), adminDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/staff")
    public ResponseEntity<List<Staff>> getAllStaff() {
        return ResponseEntity.ok(staffRepository.findAll());
    }

    @PatchMapping("/staff/{id}/toggle-status")
    public ResponseEntity<?> toggleStaffStatus(@PathVariable Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found with ID: " + id));
        
        staff.setActive(!staff.isActive());
        staffRepository.save(staff);

        return ResponseEntity.ok(Map.of(
            "message", "Staff status updated successfully",
            "active", staff.isActive()
        ));
    }

    @PostMapping("/staff/{id}/reset-password")
    public ResponseEntity<?> resetStaffPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found with ID: " + id));

        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password cannot be empty");
        }

        staff.setPassword(passwordEncoder.encode(newPassword));
        staffRepository.save(staff);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully for " + staff.getEmail()));
    }

    @GetMapping("/reports/daily")
    public ResponseEntity<List<AdminReportDto>> getDailyReport(@RequestParam(required = false) String department) {
        List<Attendance> records = (department != null && !department.trim().isEmpty())
                ? attendanceRepository.findByStaffDepartmentOrderByDateDesc(department.trim())
                : attendanceRepository.findAllByOrderByDateDesc();

        List<AdminReportDto> report = records.stream().map(r -> new AdminReportDto(
                r.getStaff().getId(),
                r.getStaff().getFullName(),
                r.getStaff().getEmail(),
                r.getStaff().getDepartment(),
                r.getDate(),
                r.getStatus() != null ? r.getStatus().name() : "PENDING"
        )).collect(Collectors.toList());

        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/export/csv")
    public ResponseEntity<byte[]> exportCsvReport() {
        List<Attendance> records = attendanceRepository.findAllByOrderByDateDesc();
        StringBuilder csvContent = new StringBuilder("Staff ID,Name,Email,Department,Date,Status\n");

        for (Attendance r : records) {
            csvContent.append(r.getStaff().getId()).append(",")
                      .append(r.getStaff().getFullName() != null ? r.getStaff().getFullName().replace(",", " ") : "").append(",")
                      .append(r.getStaff().getEmail()).append(",")
                      .append(r.getStaff().getDepartment() != null ? r.getStaff().getDepartment().replace(",", " ") : "").append(",")
                      .append(r.getDate()).append(",")
                      .append(r.getStatus() != null ? r.getStatus().name() : "PENDING").append("\n");
        }

        byte[] body = csvContent.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportToExcel(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "department", required = false) String department) throws IOException {
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be on or after start date.");
        }

        ByteArrayInputStream report = excelExportService.generateAttendanceReport(startDate, endDate, department);
        String filename = "Attendance_Report_" + startDate + "_to_" + endDate + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(report));
    }
}
