package com.smartattendance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartattendance.dto.AuthDTOs.*;
import com.smartattendance.model.*;
import com.smartattendance.repository.AttendanceRepository;
import com.smartattendance.repository.AuditLogRepository;
import com.smartattendance.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AttendanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void cleanDatabase() {
        auditLogRepository.deleteAll();
        attendanceRepository.deleteAll();
        staffRepository.deleteAll();

        // Seed users programmatically for clean test execution
        Staff staffUser = Staff.builder()
                .email("john.doe@college.edu")
                .password(passwordEncoder.encode("password123"))
                .fullName("Dr. John Doe")
                .department("CSE")
                .designation("Professor")
                .role(Role.STAFF)
                .build();

        Staff adminUser = Staff.builder()
                .email("admin@college.edu")
                .password(passwordEncoder.encode("password123"))
                .fullName("System Administrator")
                .department("Administration")
                .designation("Head Admin")
                .role(Role.ADMIN)
                .build();

        staffRepository.save(staffUser);
        staffRepository.save(adminUser);
    }

    private String obtainAccessToken(String email, String password) throws Exception {
        LoginRequest request = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JwtResponse jwtResponse = objectMapper.readValue(responseBody, JwtResponse.class);
        return jwtResponse.getToken();
    }

    @Test
    public void testFullSmartAttendanceLifecycle() throws Exception {
        // 1. Log in as Staff member
        String staffToken = obtainAccessToken("john.doe@college.edu", "password123");
        assertNotNull(staffToken);

        // 2. Mark attendance (Staff)
        MarkAttendanceRequest markReq = new MarkAttendanceRequest(
                new BigDecimal("13.08270000"),
                new BigDecimal("80.27070000")
        );

        MvcResult markResult = mockMvc.perform(post("/api/v1/attendance/mark")
                .header("Authorization", "Bearer " + staffToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(markReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendanceId", notNullValue()))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andReturn();

        String markRespBody = markResult.getResponse().getContentAsString();
        Attendance attendanceRecord = objectMapper.readValue(markRespBody, Attendance.class);
        Long recordId = attendanceRecord.getAttendanceId();
        assertNotNull(recordId);

        // 3. Verify duplicate marking check (expected 409 Conflict)
        mockMvc.perform(post("/api/v1/attendance/mark")
                .header("Authorization", "Bearer " + staffToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(markReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Attendance has already been marked for today")));

        // 4. Log in as Admin
        String adminToken = obtainAccessToken("admin@college.edu", "password123");
        assertNotNull(adminToken);

        // 5. Fetch pending records (Admin)
        mockMvc.perform(get("/api/v1/admin/pending")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].attendanceId", is(recordId.intValue())));

        // 6. Approve attendance (Admin)
        VerificationRequest verifyReq = new VerificationRequest("APPROVED");
        mockMvc.perform(post("/api/v1/admin/verify/" + recordId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk());

        // 7. Verify DB update. Audit entries are generated by the MySQL trigger;
        // the H2 test profile intentionally does not install that MySQL-specific trigger.
        Attendance updatedRecord = attendanceRepository.findById(recordId).orElseThrow();
        assertEquals(AttendanceStatus.VERIFIED, updatedRecord.getStatus());
    }

    @Test
    public void testRejectAttendanceLifecycle() throws Exception {
        // Arrange
        String staffToken = obtainAccessToken("john.doe@college.edu", "password123");
        String adminToken = obtainAccessToken("admin@college.edu", "password123");

        MarkAttendanceRequest markReq = new MarkAttendanceRequest(
                new BigDecimal("13.08270000"),
                new BigDecimal("80.27070000")
        );

        MvcResult markResult = mockMvc.perform(post("/api/v1/attendance/mark")
                .header("Authorization", "Bearer " + staffToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(markReq)))
                .andExpect(status().isOk())
                .andReturn();

        Attendance attendanceRecord = objectMapper.readValue(markResult.getResponse().getContentAsString(), Attendance.class);
        Long recordId = attendanceRecord.getAttendanceId();

        // Act - Admin rejects record
        VerificationRequest verifyReq = new VerificationRequest("REJECTED");
        mockMvc.perform(post("/api/v1/admin/verify/" + recordId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk());

        // Assert
        Attendance updatedRecord = attendanceRepository.findById(recordId).orElseThrow();
        assertEquals(AttendanceStatus.REJECTED, updatedRecord.getStatus());
    }

    @Test
    public void testRoleRestrictions() throws Exception {
        String staffToken = obtainAccessToken("john.doe@college.edu", "password123");

        // Staff attempts to access admin endpoint -> expected 403 Forbidden
        mockMvc.perform(get("/api/v1/admin/pending")
                .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testLoginFailureWithBadCredentials() throws Exception {
        LoginRequest request = new LoginRequest("john.doe@college.edu", "wrong-password");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    public void testMarkAttendanceWithoutToken() throws Exception {
        MarkAttendanceRequest request = new MarkAttendanceRequest(
                new BigDecimal("13.08270000"), new BigDecimal("80.27070000"));

        mockMvc.perform(post("/api/v1/attendance/mark")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testMarkAttendanceRejectsMissingCoordinates() throws Exception {
        String staffToken = obtainAccessToken("john.doe@college.edu", "password123");

        mockMvc.perform(post("/api/v1/attendance/mark")
                .header("Authorization", "Bearer " + staffToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\":13.0827}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.validationErrors.longitude").exists());
    }
}
