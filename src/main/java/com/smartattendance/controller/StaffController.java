package com.smartattendance.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.smartattendance.model.Staff;
import com.smartattendance.repository.StaffRepository;

@RestController
@RequestMapping("/api/v1/staff")
public class StaffController {

    private final StaffRepository staffRepository;

    public StaffController(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<Staff> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Staff staff = staffRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff profile not found."));
        return ResponseEntity.ok(staff);
    }

    public static class UpdateProfileRequest {
        private String department;
        private String designation;

        public UpdateProfileRequest() {}

        public UpdateProfileRequest(String department, String designation) {
            this.department = department;
            this.designation = designation;
        }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }
    }

    @org.springframework.web.bind.annotation.PutMapping("/profile")
    public ResponseEntity<Staff> updateProfile(
            org.springframework.security.core.Authentication authentication,
            @RequestBody UpdateProfileRequest request) {

        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.");
        }

        String email = authentication.getName();
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff profile not found."));

        if (request.getDepartment() != null && !request.getDepartment().isBlank()) {
            staff.setDepartment(request.getDepartment().trim());
        }
        if (request.getDesignation() != null && !request.getDesignation().isBlank()) {
            staff.setDesignation(request.getDesignation().trim());
        }

        Staff savedStaff = staffRepository.save(staff);
        return ResponseEntity.ok(savedStaff);
    }

    @org.springframework.web.bind.annotation.PostMapping("/profile")
    public ResponseEntity<Staff> updateProfilePost(
            org.springframework.security.core.Authentication authentication,
            @RequestBody UpdateProfileRequest request) {
        return updateProfile(authentication, request);
    }
}