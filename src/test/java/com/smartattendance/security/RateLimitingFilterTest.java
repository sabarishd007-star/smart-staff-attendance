package com.smartattendance.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartattendance.dto.AuthDTOs.LoginRequest;
import com.smartattendance.model.Role;
import com.smartattendance.model.Staff;
import com.smartattendance.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = {"app.rate-limit.capacity=5", "app.rate-limit.tokens-per-minute=5"})
public class RateLimitingFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void cleanDatabase() {
        staffRepository.deleteAll();

        Staff staffUser = Staff.builder()
                .email("john.doe@college.edu")
                .password(passwordEncoder.encode("password123"))
                .fullName("Dr. John Doe")
                .department("CSE")
                .designation("Professor")
                .role(Role.STAFF)
                .build();
        staffRepository.save(staffUser);
    }

    @Test
    public void testRateLimitingOnLoginEndpoint() throws Exception {
        LoginRequest request = new LoginRequest("john.doe@college.edu", "password123");
        String content = objectMapper.writeValueAsString(request);

        // First 5 requests should succeed (or return 200 OK or auth failure, but not 429)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content))
                    .andExpect(status().isOk());
        }

        // 6th request must trigger rate limit -> HTTP 429 Too Many Requests
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message").value("Rate limit exceeded. Maximum 5 requests per minute allowed."));
    }
}
