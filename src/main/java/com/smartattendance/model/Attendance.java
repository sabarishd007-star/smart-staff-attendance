package com.smartattendance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "attendance",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "unique_staff_daily_attendance",
            columnNames = {"staff_id", "date"}
        )
    }
)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long attendanceId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "time_marked", nullable = false)
    private LocalTime timeMarked;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private AttendanceStatus status = AttendanceStatus.PENDING;

    public Attendance() {
    }

    public Attendance(Long attendanceId, Staff staff, LocalDate date, LocalTime timeMarked, BigDecimal latitude, BigDecimal longitude, AttendanceStatus status) {
        this.attendanceId = attendanceId;
        this.staff = staff;
        this.date = date;
        this.timeMarked = timeMarked;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status != null ? status : AttendanceStatus.PENDING;
    }

    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
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

    public static class AttendanceBuilder {
        private Long attendanceId;
        private Staff staff;
        private LocalDate date;
        private LocalTime timeMarked;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private AttendanceStatus status = AttendanceStatus.PENDING;

        public AttendanceBuilder attendanceId(Long attendanceId) {
            this.attendanceId = attendanceId;
            return this;
        }

        public AttendanceBuilder staff(Staff staff) {
            this.staff = staff;
            return this;
        }

        public AttendanceBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public AttendanceBuilder timeMarked(LocalTime timeMarked) {
            this.timeMarked = timeMarked;
            return this;
        }

        public AttendanceBuilder latitude(BigDecimal latitude) {
            this.latitude = latitude;
            return this;
        }

        public AttendanceBuilder longitude(BigDecimal longitude) {
            this.longitude = longitude;
            return this;
        }

        public AttendanceBuilder status(AttendanceStatus status) {
            this.status = status;
            return this;
        }

        public Attendance build() {
            return new Attendance(attendanceId, staff, date, timeMarked, latitude, longitude, status);
        }
    }

    public static AttendanceBuilder builder() {
        return new AttendanceBuilder();
    }
}
