package com.smartattendance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AuthDTOs {

    public static class LoginRequest {
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        private String email;

        @NotBlank(message = "Password cannot be blank")
        private String password;

        public LoginRequest() {
        }

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class JwtResponse {
        private Long id;
        private String token;
        private String email;
        private String role;
        private String fullName;
        private String department;
        private String designation;
        private boolean profileCompleted;

        public JwtResponse() {
        }

        public JwtResponse(Long id, String token, String email, String role,
                           String fullName, String department, String designation,
                           boolean profileCompleted) {
            this.id = id;
            this.token = token;
            this.email = email;
            this.role = role;
            this.fullName = fullName;
            this.department = department;
            this.designation = designation;
            this.profileCompleted = profileCompleted;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public String getDesignation() {
            return designation;
        }

        public void setDesignation(String designation) {
            this.designation = designation;
        }

        public boolean isProfileCompleted() {
            return profileCompleted;
        }

        public void setProfileCompleted(boolean profileCompleted) {
            this.profileCompleted = profileCompleted;
        }
    }

    public static class MarkAttendanceRequest {
        @NotNull(message = "Latitude is required")
        private BigDecimal latitude;

        @NotNull(message = "Longitude is required")
        private BigDecimal longitude;

        public MarkAttendanceRequest() {
        }

        public MarkAttendanceRequest(BigDecimal latitude, BigDecimal longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public BigDecimal getLatitude() {
            return latitude;
        }

        public void setLatitude(BigDecimal latitude) {
            this.latitude = latitude;
        }

        public BigDecimal getLongitude() {
            return longitude;
        }

        public void setLongitude(BigDecimal longitude) {
            this.longitude = longitude;
        }
    }

    public static class VerificationRequest {
        private String action; // "APPROVED" or "REJECTED"

        public VerificationRequest() {
        }

        public VerificationRequest(String action) {
            this.action = action;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }
}
