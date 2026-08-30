package com.smartattendance.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long id;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @JsonIgnore
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "department", nullable = false, length = 150)
    private String department;

    @Column(name = "designation", nullable = false, length = 150)
    private String designation;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Staff() {
    }

    public Staff(Long id, String email, String password, String fullName, String department, String designation, Role role, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.department = department;
        this.designation = designation;
        this.role = role;
        this.createdAt = createdAt;
        this.active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static class StaffBuilder {
        private Long id;
        private String email;
        private String password;
        private String fullName;
        private String department;
        private String designation;
        private Role role;
        private LocalDateTime createdAt;

        public StaffBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public StaffBuilder email(String email) {
            this.email = email;
            return this;
        }

        public StaffBuilder password(String password) {
            this.password = password;
            return this;
        }

        public StaffBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public StaffBuilder department(String department) {
            this.department = department;
            return this;
        }

        public StaffBuilder designation(String designation) {
            this.designation = designation;
            return this;
        }

        public StaffBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public StaffBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Staff build() {
            return new Staff(id, email, password, fullName, department, designation, role, createdAt);
        }
    }

    public static StaffBuilder builder() {
        return new StaffBuilder();
    }
}
