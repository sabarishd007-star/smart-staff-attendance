package com.smartattendance.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private AuditAction action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private Staff performedBy;

    @Column(name = "performed_at", insertable = false, updatable = false)
    private LocalDateTime performedAt;

    public AuditLog() {
    }

    public AuditLog(Long auditId, Attendance attendance, AuditAction action, Staff performedBy, LocalDateTime performedAt) {
        this.auditId = auditId;
        this.attendance = attendance;
        this.action = action;
        this.performedBy = performedBy;
        this.performedAt = performedAt;
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public Attendance getAttendance() {
        return attendance;
    }

    public void setAttendance(Attendance attendance) {
        this.attendance = attendance;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public Staff getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(Staff performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(LocalDateTime performedAt) {
        this.performedAt = performedAt;
    }

    public static class AuditLogBuilder {
        private Long auditId;
        private Attendance attendance;
        private AuditAction action;
        private Staff performedBy;
        private LocalDateTime performedAt;

        public AuditLogBuilder auditId(Long auditId) {
            this.auditId = auditId;
            return this;
        }

        public AuditLogBuilder attendance(Attendance attendance) {
            this.attendance = attendance;
            return this;
        }

        public AuditLogBuilder action(AuditAction action) {
            this.action = action;
            return this;
        }

        public AuditLogBuilder performedBy(Staff performedBy) {
            this.performedBy = performedBy;
            return this;
        }

        public AuditLogBuilder performedAt(LocalDateTime performedAt) {
            this.performedAt = performedAt;
            return this;
        }

        public AuditLog build() {
            return new AuditLog(auditId, attendance, action, performedBy, performedAt);
        }
    }

    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }
}
