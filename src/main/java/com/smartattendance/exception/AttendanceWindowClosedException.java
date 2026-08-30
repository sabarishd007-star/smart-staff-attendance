package com.smartattendance.exception;

public class AttendanceWindowClosedException extends RuntimeException {
    public AttendanceWindowClosedException(String message) {
        super(message);
    }
}
