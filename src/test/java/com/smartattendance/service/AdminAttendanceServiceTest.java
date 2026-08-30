package com.smartattendance.service;

import com.smartattendance.model.*;
import com.smartattendance.repository.AttendanceRepository;
import com.smartattendance.repository.AuditLogRepository;
import com.smartattendance.repository.StaffRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminAttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private jakarta.persistence.Query query;

    @InjectMocks
    private AdminAttendanceService adminAttendanceService;

    private Staff adminStaff;
    private Staff regularStaff;
    private Attendance pendingAttendance;
    private final String adminEmail = "admin@college.edu";

    @BeforeEach
    public void setUp() {
        adminStaff = Staff.builder()
                .id(2L)
                .email(adminEmail)
                .fullName("System Admin")
                .role(Role.ADMIN)
                .build();

        regularStaff = Staff.builder()
                .id(1L)
                .email("john.doe@college.edu")
                .fullName("Dr. John Doe")
                .role(Role.STAFF)
                .build();

        pendingAttendance = Attendance.builder()
                .attendanceId(10L)
                .staff(regularStaff)
                .date(LocalDate.now())
                .timeMarked(LocalTime.of(9, 0))
                .latitude(new BigDecimal("13.0827"))
                .longitude(new BigDecimal("80.2707"))
                .status(AttendanceStatus.PENDING)
                .build();
    }

    @Test
    public void testGetPendingRecords() {
        // Arrange
        when(attendanceRepository.findByStatus(AttendanceStatus.PENDING))
                .thenReturn(Collections.singletonList(pendingAttendance));

        // Act
        List<Attendance> result = adminAttendanceService.getPendingRecords();

        // Assert
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getAttendanceId());
        verify(attendanceRepository, times(1)).findByStatus(AttendanceStatus.PENDING);
    }

    @Test
    public void testProcessVerification_ApproveSuccess() {
        // Arrange
        when(attendanceRepository.findById(10L)).thenReturn(Optional.of(pendingAttendance));
        when(staffRepository.findByEmail(adminEmail)).thenReturn(Optional.of(adminStaff));

        // Act
        adminAttendanceService.processVerification(10L, "APPROVED", adminEmail);

        // Assert
        assertEquals(AttendanceStatus.VERIFIED, pendingAttendance.getStatus());
        verify(attendanceRepository, times(1)).save(pendingAttendance);
        verify(entityManager, times(1)).flush();
    }

    @Test
    public void testProcessVerification_RejectSuccess() {
        // Arrange
        when(attendanceRepository.findById(10L)).thenReturn(Optional.of(pendingAttendance));
        when(staffRepository.findByEmail(adminEmail)).thenReturn(Optional.of(adminStaff));

        // Act
        adminAttendanceService.processVerification(10L, "REJECTED", adminEmail);

        // Assert
        assertEquals(AttendanceStatus.REJECTED, pendingAttendance.getStatus());
        verify(attendanceRepository, times(1)).save(pendingAttendance);
        verify(entityManager, times(1)).flush();
    }

    @Test
    public void testProcessVerification_RecordNotFound() {
        // Arrange
        when(attendanceRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            adminAttendanceService.processVerification(99L, "APPROVED", adminEmail);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Attendance record not found.", exception.getReason());
        verify(attendanceRepository, never()).save(any(Attendance.class));
        verifyNoInteractions(auditLogRepository);
    }

    @Test
    public void testProcessVerification_InvalidAction() {
        // Arrange
        when(attendanceRepository.findById(10L)).thenReturn(Optional.of(pendingAttendance));
        when(staffRepository.findByEmail(adminEmail)).thenReturn(Optional.of(adminStaff));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            adminAttendanceService.processVerification(10L, "INVALID_ACTION", adminEmail);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid verification action. Use APPROVED or REJECTED.", exception.getReason());
        verify(attendanceRepository, never()).save(any(Attendance.class));
        verifyNoInteractions(auditLogRepository);
    }
}
