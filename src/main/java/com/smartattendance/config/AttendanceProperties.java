package com.smartattendance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;
import java.time.ZoneId;

@Configuration
public class AttendanceProperties {

    @Value("${app.attendance.timezone:Asia/Kolkata}")
    private String timezone;

    @Value("${app.attendance.window-start-hour:5}")
    private int startHour;

    @Value("${app.attendance.window-start-minute:0}")
    private int startMinute;

    @Value("${app.attendance.window-end-hour:16}")
    private int endHour;

    @Value("${app.attendance.window-end-minute:0}")
    private int endMinute;

    @Value("${app.attendance.enforce-time-window:true}")
    private boolean enforceTimeWindow;

    @Value("${app.attendance.campus-latitude:13.0827}")
    private double campusLatitude;

    @Value("${app.attendance.campus-longitude:80.2707}")
    private double campusLongitude;

    @Value("${app.attendance.allowed-radius-meters:500.0}")
    private double allowedRadiusMeters;

    @Value("${app.attendance.enforce-geofence:true}")
    private boolean enforceGeofence;

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public void setEnforceTimeWindow(boolean enforceTimeWindow) {
        this.enforceTimeWindow = enforceTimeWindow;
    }

    public void setCampusLatitude(double campusLatitude) {
        this.campusLatitude = campusLatitude;
    }

    public void setCampusLongitude(double campusLongitude) {
        this.campusLongitude = campusLongitude;
    }

    public void setAllowedRadiusMeters(double allowedRadiusMeters) {
        this.allowedRadiusMeters = allowedRadiusMeters;
    }

    public void setEnforceGeofence(boolean enforceGeofence) {
        this.enforceGeofence = enforceGeofence;
    }

    public ZoneId getZoneId() {
        return ZoneId.of(timezone != null ? timezone : "Asia/Kolkata");
    }

    public LocalTime getWindowStart() {
        return LocalTime.of(startHour, startMinute);
    }

    public LocalTime getWindowEnd() {
        return LocalTime.of(endHour, endMinute);
    }

    public boolean isEnforceTimeWindow() {
        return enforceTimeWindow;
    }

    public double getCampusLatitude() {
        return campusLatitude;
    }

    public double getCampusLongitude() {
        return campusLongitude;
    }

    public double getAllowedRadiusMeters() {
        return allowedRadiusMeters;
    }

    public boolean isEnforceGeofence() {
        return enforceGeofence;
    }
}
