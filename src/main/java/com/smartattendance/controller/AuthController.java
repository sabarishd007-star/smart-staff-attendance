package com.smartattendance.controller;

import com.smartattendance.dto.AuthDTOs.*;
import com.smartattendance.model.Staff;
import com.smartattendance.repository.StaffRepository;
import com.smartattendance.security.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final StaffRepository staffRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, StaffRepository staffRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.staffRepository = staffRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Staff staff = staffRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtUtils.generateToken(staff.getEmail(), staff.getRole().name());

        boolean profileCompleted = staff.getDepartment() != null && !staff.getDepartment().isBlank() &&
                                   !staff.getDepartment().equalsIgnoreCase("Unassigned") &&
                                   staff.getDesignation() != null && !staff.getDesignation().isBlank() &&
                                   !staff.getDesignation().equalsIgnoreCase("Unassigned");

        return ResponseEntity.ok(new JwtResponse(
                staff.getId(),
                token,
                staff.getEmail(),
                staff.getRole().name(),
                staff.getFullName(),
                staff.getDepartment(),
                staff.getDesignation(),
                profileCompleted
        ));
    }
}