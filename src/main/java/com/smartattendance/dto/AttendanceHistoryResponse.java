package com.smartattendance.dto;

import java.util.List;

public class AttendanceHistoryResponse {
    private long totalDays;
    private long presentDays;
    private long pendingDays;
    private long rejectedDays;
    private double attendancePercentage;
    private List<AttendanceRecordDto> records;

    public AttendanceHistoryResponse() {
    }

    public AttendanceHistoryResponse(long totalDays, long presentDays, long pendingDays, long rejectedDays, double attendancePercentage, List<AttendanceRecordDto> records) {
        this.totalDays = totalDays;
        this.presentDays = presentDays;
        this.pendingDays = pendingDays;
        this.rejectedDays = rejectedDays;
        this.attendancePercentage = attendancePercentage;
        this.records = records;
    }

    public long getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(long totalDays) {
        this.totalDays = totalDays;
    }

    public long getPresentDays() {
        return presentDays;
    }

    public void setPresentDays(long presentDays) {
        this.presentDays = presentDays;
    }

    public long getPendingDays() {
        return pendingDays;
    }

    public void setPendingDays(long pendingDays) {
        this.pendingDays = pendingDays;
    }

    public long getRejectedDays() {
        return rejectedDays;
    }

    public void setRejectedDays(long rejectedDays) {
        this.rejectedDays = rejectedDays;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public List<AttendanceRecordDto> getRecords() {
        return records;
    }

    public void setRecords(List<AttendanceRecordDto> records) {
        this.records = records;
    }
}
