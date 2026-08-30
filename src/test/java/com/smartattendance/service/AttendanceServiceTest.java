package com.smartattendance.service;

import com.smartattendance.config.AttendanceProperties;
import com.smartattendance.model.*;
import com.smartattendance.repository.AttendanceRepository;
import com.smartattendance.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private StaffRepository staffRepository;

    private AttendanceProperties attendanceProperties;
    private AttendanceService attendanceService;

    private Staff testStaff;
    private final String testEmail = "john.doe@college.edu";
    private final BigDecimal latitude = new BigDecimal("13.0827");
    private final BigDecimal longitude = new BigDecimal("80.2707");

    @BeforeEach
    public void setUp() {
        attendanceProperties = new AttendanceProperties();
        attendanceProperties.setTimezone("Asia/Kolkata");
        attendanceProperties.setEnforceTimeWindow(false);
        attendanceProperties.setEnforceGeofence(false);
        attendanceService = new AttendanceService(attendanceRepository, staffRepository, attendanceProperties);

        testStaff = Staff.builder()
                .id(1L)
                .email(testEmail)
                .fullName("Dr. John Doe")
                .role(Role.STAFF)
                .build();
    }

    @Test
    public void testMarkAttendance_Success() {
        // Arrange
        attendanceProperties.setEnforceTimeWindow(false);
        when(staffRepository.findByEmail(testEmail)).thenReturn(Optional.of(testStaff));
        when(attendanceRepository.existsByStaffIdAndDate(eq(1L), any(LocalDate.class))).thenReturn(false);
        
        Attendance expectedRecord = Attendance.builder()
                .attendanceId(10L)
                .staff(testStaff)
                .date(LocalDate.now(ZoneId.of("Asia/Kolkata")))
                .timeMarked(LocalTime.now(ZoneId.of("Asia/Kolkata")))
                .latitude(latitude)
                .longitude(longitude)
                .status(AttendanceStatus.PENDING)
                .build();
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(expectedRecord);

        // Act
        Attendance result = attendanceService.markAttendance(testEmail, latitude, longitude);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getAttendanceId());
        assertEquals(AttendanceStatus.PENDING, result.getStatus());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    public void testMarkAttendance_WindowClosed() {
        // Arrange
        attendanceProperties.setEnforceTimeWindow(true);
        attendanceProperties.setStartHour(5);
        attendanceProperties.setStartMinute(0);
        attendanceProperties.setEndHour(5);
        attendanceProperties.setEndMinute(1); // Closed window

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            attendanceService.markAttendance(testEmail, latitude, longitude);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Attendance window is closed"));
        verifyNoInteractions(staffRepository, attendanceRepository);
    }

    @Test
    public void testMarkAttendance_DuplicatePreCheck() {
        // Arrange
        attendanceProperties.setEnforceTimeWindow(false);
        when(staffRepository.findByEmail(testEmail)).thenReturn(Optional.of(testStaff));
        when(attendanceRepository.existsByStaffIdAndDate(eq(1L), any(LocalDate.class))).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            attendanceService.markAttendance(testEmail, latitude, longitude);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Attendance has already been marked for today.", exception.getReason());
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    public void testMarkAttendance_DuplicateDataIntegrityException() {
        // Arrange
        attendanceProperties.setEnforceTimeWindow(false);
        when(staffRepository.findByEmail(testEmail)).thenReturn(Optional.of(testStaff));
        when(attendanceRepository.existsByStaffIdAndDate(eq(1L), any(LocalDate.class))).thenReturn(false);
        when(attendanceRepository.save(any(Attendance.class))).thenThrow(new DataIntegrityViolationException("Duplicate key"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            attendanceService.markAttendance(testEmail, latitude, longitude);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Attendance record already exists for today.", exception.getReason());
    }

    @Test
    public void testGetMyAttendanceHistory_Success() {
        // Arrange
        when(staffRepository.findByEmail(testEmail)).thenReturn(Optional.of(testStaff));
        
        Attendance record1 = Attendance.builder()
                .attendanceId(1L)
                .staff(testStaff)
                .date(LocalDate.of(2026, 3, 1))
                .timeMarked(LocalTime.of(9, 0))
                .latitude(latitude)
                .longitude(longitude)
                .status(AttendanceStatus.VERIFIED)
                .build();
        Attendance record2 = Attendance.builder()
                .attendanceId(2L)
                .staff(testStaff)
                .date(LocalDate.of(2026, 3, 2))
                .timeMarked(LocalTime.of(9, 5))
                .latitude(latitude)
                .longitude(longitude)
                .status(AttendanceStatus.PENDING)
                .build();
        Attendance record3 = Attendance.builder()
                .attendanceId(3L)
                .staff(testStaff)
                .date(LocalDate.of(2026, 3, 3))
                .timeMarked(LocalTime.of(9, 10))
                .latitude(latitude)
                .longitude(longitude)
                .status(AttendanceStatus.REJECTED)
                .build();
        Attendance record4 = Attendance.builder()
                .attendanceId(4L)
                .staff(testStaff)
                .date(LocalDate.of(2026, 3, 4))
                .timeMarked(LocalTime.of(9, 15))
                .latitude(latitude)
                .longitude(longitude)
                .status(AttendanceStatus.VERIFIED)
                .build();

        when(attendanceRepository.findByStaffOrderByDateDesc(testStaff))
                .thenReturn(List.of(record4, record3, record2, record1));

        // Act
        com.smartattendance.dto.AttendanceHistoryResponse history = attendanceService.getMyAttendanceHistory(testEmail);

        // Assert
        assertNotNull(history);
        assertEquals(4, history.getTotalDays());
        assertEquals(2, history.getPresentDays());
        assertEquals(1, history.getPendingDays());
        assertEquals(1, history.getRejectedDays());
        assertEquals(50.0, history.getAttendancePercentage());
        assertEquals(4, history.getRecords().size());
        assertEquals(4L, history.getRecords().get(0).getAttendanceId());
    }

    @Test
    public void testGetMyAttendanceHistory_Empty() {
        // Arrange
        when(staffRepository.findByEmail(testEmail)).thenReturn(Optional.of(testStaff));
        when(attendanceRepository.findByStaffOrderByDateDesc(testStaff)).thenReturn(Collections.emptyList());

        // Act
        com.smartattendance.dto.AttendanceHistoryResponse history = attendanceService.getMyAttendanceHistory(testEmail);

        // Assert
        assertNotNull(history);
        assertEquals(0, history.getTotalDays());
        assertEquals(0, history.getPresentDays());
        assertEquals(0, history.getPendingDays());
        assertEquals(0, history.getRejectedDays());
        assertEquals(0.0, history.getAttendancePercentage());
        assertTrue(history.getRecords().isEmpty());
    }

    @Test
    public void testGetMyAttendanceHistory_StaffNotFound() {
        // Arrange
        when(staffRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            attendanceService.getMyAttendanceHistory(testEmail);
        });
    }
}
