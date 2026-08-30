package com.smartattendance.dto;

import java.time.LocalDate;

public class AdminReportDto {
    private Long staffId;
    private String staffName;
    private String email;
    private String department;
    private LocalDate date;
    private String status;

    public AdminReportDto() {
    }

    public AdminReportDto(Long staffId, String staffName, String email, String department, LocalDate date, String status) {
        this.staffId = staffId;
        this.staffName = staffName;
        this.email = email;
        this.department = department;
        this.date = date;
        this.status = status;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
