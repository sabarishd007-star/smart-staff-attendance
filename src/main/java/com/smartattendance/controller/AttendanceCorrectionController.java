package com.smartattendance.controller;

import com.smartattendance.dto.CorrectionRequestDto;
import com.smartattendance.model.*;
import com.smartattendance.repository.AttendanceCorrectionRequestRepository;
import com.smartattendance.repository.StaffRepository;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/corrections")
public class AttendanceCorrectionController {
    private final AttendanceCorrectionRequestRepository corrections;
    private final StaffRepository staff;
    public AttendanceCorrectionController(AttendanceCorrectionRequestRepository corrections, StaffRepository staff) {
        this.corrections = corrections; this.staff = staff;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'HOD', 'ADMIN')")
    public ResponseEntity<AttendanceCorrectionRequest> create(@Valid @RequestBody CorrectionRequestDto request,
            @AuthenticationPrincipal UserDetails principal) {
        Staff requester = current(principal);
        AttendanceCorrectionRequest correction = new AttendanceCorrectionRequest();
        correction.setStaff(requester);
        correction.setAttendanceDate(request.getDate());
        correction.setReason(request.getReason().trim());
        correction.setNote(request.getNote() == null ? null : request.getNote().trim());
        correction.setStatus(CorrectionStatus.PENDING_APPROVAL);
        return ResponseEntity.status(HttpStatus.CREATED).body(corrections.save(correction));
    }

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public List<AttendanceCorrectionRequest> mine(@AuthenticationPrincipal UserDetails principal) {
        return corrections.findByStaffEmailOrderBySubmittedAtDesc(principal.getUsername());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('HOD', 'ADMIN')")
    public List<AttendanceCorrectionRequest> pending(@AuthenticationPrincipal UserDetails principal) {
        Staff reviewer = current(principal);
        return reviewer.getRole() == Role.ADMIN
                ? corrections.findByStatusOrderBySubmittedAtDesc(CorrectionStatus.PENDING_APPROVAL)
                : corrections.findByStatusAndStaffDepartmentOrderBySubmittedAtDesc(CorrectionStatus.PENDING_APPROVAL, reviewer.getDepartment());
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('HOD', 'ADMIN')")
    public AttendanceCorrectionRequest review(@PathVariable Long id, @RequestBody java.util.Map<String, String> request,
            @AuthenticationPrincipal UserDetails principal) {
        Staff reviewer = current(principal);
        AttendanceCorrectionRequest correction = corrections.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Correction request not found."));
        if (reviewer.getRole() != Role.ADMIN && !reviewer.getDepartment().equalsIgnoreCase(correction.getStaff().getDepartment()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only review requests in your department.");
        if (correction.getStatus() != CorrectionStatus.PENDING_APPROVAL)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This correction request has already been reviewed.");
        String action = request.getOrDefault("action", "").toUpperCase();
        if (!action.equals("APPROVED") && !action.equals("REJECTED"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action must be APPROVED or REJECTED.");
        correction.setStatus(CorrectionStatus.valueOf(action));
        correction.setReviewedBy(reviewer);
        correction.setReviewedAt(LocalDateTime.now());
        return corrections.save(correction);
    }

    private Staff current(UserDetails principal) {
        return staff.findByEmail(principal.getUsername()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff profile not found."));
    }
}
