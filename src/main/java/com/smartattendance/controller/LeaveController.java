package com.smartattendance.controller;

import com.smartattendance.model.LeaveRequest;
import com.smartattendance.model.Staff;
import com.smartattendance.repository.LeaveRequestRepository;
import com.smartattendance.repository.StaffRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/leave")
public class LeaveController {

    private final LeaveRequestRepository leaveRepository;
    private final StaffRepository staffRepository;

    public LeaveController(LeaveRequestRepository leaveRepository, StaffRepository staffRepository) {
        this.leaveRepository = leaveRepository;
        this.staffRepository = staffRepository;
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyForLeave(@RequestBody LeaveRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String email = userDetails.getUsername();
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff profile not found"));

        request.setStaff(staff);
        request.setStatus("PENDING");
        leaveRepository.save(request);

        return ResponseEntity.ok(Map.of("message", "Leave application submitted successfully!"));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<LeaveRequest>> getMyLeaves(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        String email = userDetails.getUsername();
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff profile not found"));

        return ResponseEntity.ok(leaveRepository.findByStaffOrderByIdDesc(staff));
    }
}
