package com.smartattendance.repository;

import com.smartattendance.model.Attendance;
import com.smartattendance.model.AttendanceStatus;
import com.smartattendance.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByStaffIdAndDate(Long staffId, LocalDate date);
    List<Attendance> findByStatus(AttendanceStatus status);
    boolean existsByStaffIdAndDate(Long staffId, LocalDate date);
    List<Attendance> findByStaffOrderByDateDesc(Staff staff);
    long countByStatus(AttendanceStatus status);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.staff s ORDER BY a.date DESC")
    List<Attendance> findAllByOrderByDateDesc();

    @Query("SELECT a FROM Attendance a JOIN FETCH a.staff s WHERE s.department = :department ORDER BY a.date DESC")
    List<Attendance> findByStaffDepartmentOrderByDateDesc(@Param("department") String department);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.staff s " +
           "WHERE a.date BETWEEN :startDate AND :endDate " +
           "AND (:department IS NULL OR s.department = :department) " +
           "ORDER BY a.date DESC, a.timeMarked ASC")
    List<Attendance> findAttendanceForReport(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("department") String department);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.staff s " +
           "WHERE a.status = :status AND s.department = :department " +
           "ORDER BY a.date DESC, a.timeMarked ASC")
    List<Attendance> findByStatusAndStaffDepartment(
            @Param("status") AttendanceStatus status,
            @Param("department") String department);
}
