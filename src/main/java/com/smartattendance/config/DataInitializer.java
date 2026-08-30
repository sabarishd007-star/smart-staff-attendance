package com.smartattendance.config;

import com.smartattendance.model.Role;
import com.smartattendance.model.Staff;
import com.smartattendance.repository.StaffRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(StaffRepository staffRepository, PasswordEncoder passwordEncoder) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createIfNotExists("admin@college.edu", "password123", "System Administrator", "Administration", "Head Admin", Role.ADMIN);
        createIfNotExists("john.doe@college.edu", "password123", "Dr. John Doe", "Computer Science and Engineering (CSE)", "Professor", Role.STAFF);
        createIfNotExists("hod.cse@college.edu", "password123", "Dr. Sarah Smith", "Computer Science and Engineering (CSE)", "Head of Department (HOD)", Role.HOD);
        createIfNotExists("new.staff@college.edu", "password123", "Alex Turner", "Unassigned", "Unassigned", Role.STAFF);
        createIfNotExists("staff.ece@college.edu", "password123", "Prof. Anita Sharma", "Electronics and Communication Engineering (ECE)", "Associate Professor", Role.STAFF);
        createIfNotExists("staff.aids@college.edu", "password123", "Dr. Rajesh Kumar", "Artificial Intelligence & Data Science (AI&DS)", "Assistant Professor", Role.STAFF);
    }

    private void createIfNotExists(String email, String rawPassword, String name, String dept, String desig, Role role) {
        if (staffRepository.findByEmail(email).isEmpty()) {
            Staff staff = Staff.builder()
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .fullName(name)
                    .department(dept)
                    .designation(desig)
                    .role(role)
                    .build();
            staffRepository.save(staff);
        }
    }
}
