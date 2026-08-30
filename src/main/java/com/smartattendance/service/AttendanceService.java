package com.smartattendance.service;

import com.smartattendance.config.AttendanceProperties;
import com.smartattendance.dto.AttendanceHistoryResponse;
import com.smartattendance.dto.AttendanceRecordDto;
import com.smartattendance.model.*;
import com.smartattendance.repository.AttendanceRepository;
import com.smartattendance.repository.StaffRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StaffRepository staffRepository;
    private final AttendanceProperties attendanceProperties;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             StaffRepository staffRepository,
                             AttendanceProperties attendanceProperties) {
        this.attendanceRepository = attendanceRepository;
        this.staffRepository = staffRepository;
        this.attendanceProperties = attendanceProperties;
    }

    // ── Haversine Geofence ───────────────────────────────────────────────────

    /**
     * Returns the great-circle distance in metres between the given point and the campus.
     */
    public long getDistanceFromCampus(double userLat, double userLng) {
        final double earthRadius = 6_371_000; // metres
        double dLat = Math.toRadians(userLat - attendanceProperties.getCampusLatitude());
        double dLng = Math.toRadians(userLng - attendanceProperties.getCampusLongitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(attendanceProperties.getCampusLatitude()))
                  * Math.cos(Math.toRadians(userLat))
                  * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(earthRadius * c);
    }

    /**
     * Returns true if the given coordinates fall within the configured allowed radius.
     */
    public boolean isWithinGeofence(double userLat, double userLng) {
        return getDistanceFromCampus(userLat, userLng) <= attendanceProperties.getAllowedRadiusMeters();
    }

    // ── Attendance Operations ────────────────────────────────────────────────

    public Attendance markAttendance(String email, BigDecimal latitude, BigDecimal longitude) {
        // 1. Time Window Validation
        LocalTime currentTime = LocalTime.now(attendanceProperties.getZoneId());
        if (attendanceProperties.isEnforceTimeWindow()) {
            if (currentTime.isBefore(attendanceProperties.getWindowStart())
                    || currentTime.isAfter(attendanceProperties.getWindowEnd())) {
                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Attendance window is closed. Operating hours are 05:00 AM to 05:30 PM IST."
                );
            }
        }

        // 2. Geofence Validation
        if (attendanceProperties.isEnforceGeofence()) {
            double userLat = latitude.doubleValue();
            double userLng = longitude.doubleValue();
            if (!isWithinGeofence(userLat, userLng)) {
                long distanceMeters = getDistanceFromCampus(userLat, userLng);
                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are " + distanceMeters + "m away from campus. Must be within "
                        + (long) attendanceProperties.getAllowedRadiusMeters() + "m to mark attendance."
                );
            }
        }

        // 3. Load Staff
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff profile not found."));

        LocalDate today = LocalDate.now(attendanceProperties.getZoneId());

        // 4. Duplicate Check
        if (attendanceRepository.existsByStaffIdAndDate(staff.getId(), today)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Attendance has already been marked for today.");
        }

        // 5. Construct Record
        Attendance attendance = Attendance.builder()
                .staff(staff)
                .date(today)
                .timeMarked(currentTime)
                .latitude(latitude)
                .longitude(longitude)
                .status(AttendanceStatus.PENDING)
                .build();

        // 6. Save with DB Unique Constraint Safety
        try {
            return attendanceRepository.save(attendance);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Attendance record already exists for today.");
        }
    }

    public AttendanceHistoryResponse getMyAttendanceHistory(String email) {
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff profile not found."));

        List<Attendance> records = attendanceRepository.findByStaffOrderByDateDesc(staff);

        long total   = records.size();
        long present = records.stream().filter(r -> r.getStatus() == AttendanceStatus.VERIFIED).count();
        long pending = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PENDING).count();
        long rejected = records.stream().filter(r -> r.getStatus() == AttendanceStatus.REJECTED).count();

        double percentage = total > 0 ? ((double) present / total) * 100 : 0.0;

        List<AttendanceRecordDto> recordDtos = records.stream()
                .map(AttendanceRecordDto::new)
                .collect(Collectors.toList());

        return new AttendanceHistoryResponse(
            total, present, pending, rejected,
            Math.round(percentage * 100.0) / 100.0,
            recordDtos
        );
    }
}