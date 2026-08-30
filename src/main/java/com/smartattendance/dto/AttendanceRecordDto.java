package com.smartattendance.dto;

import com.smartattendance.model.Attendance;
import com.smartattendance.model.AttendanceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceRecordDto {
    private Long attendanceId;
    private LocalDate date;
    private LocalTime timeMarked;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private AttendanceStatus status;

    public AttendanceRecordDto() {
    }

    public AttendanceRecordDto(Long attendanceId, LocalDate date, LocalTime timeMarked, BigDecimal latitude, BigDecimal longitude, AttendanceStatus status) {
        this.attendanceId = attendanceId;
        this.date = date;
        this.timeMarked = timeMarked;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }

    public AttendanceRecordDto(Attendance attendance) {
        if (attendance != null) {
            this.attendanceId = attendance.getAttendanceId();
            this.date = attendance.getDate();
            this.timeMarked = attendance.getTimeMarked();
            this.latitude = attendance.getLatitude();
            this.longitude = attendance.getLongitude();
            this.status = attendance.getStatus();
        }
    }

    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTimeMarked() {
        return timeMarked;
    }

    public void setTimeMarked(LocalTime timeMarked) {
        this.timeMarked = timeMarked;
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

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }
}
