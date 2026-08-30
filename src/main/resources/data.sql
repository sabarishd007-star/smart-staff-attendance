-- Seed Admin (Password: password123)
INSERT INTO staff (email, password, full_name, department, designation, role)
SELECT 'admin@college.edu', '$2a$10$jDIN4FYA/ED0FbHeZT/rIOTpWYO/EM0oK.MYOXf5ib8YopLIZkIwS', 'System Administrator', 'Administration', 'Head Admin', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email = 'admin@college.edu');

-- Seed Staff Member (Password: password123)
INSERT INTO staff (email, password, full_name, department, designation, role)
SELECT 'john.doe@college.edu', '$2a$10$jDIN4FYA/ED0FbHeZT/rIOTpWYO/EM0oK.MYOXf5ib8YopLIZkIwS', 'Dr. John Doe', 'CSE', 'Professor', 'STAFF'
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email = 'john.doe@college.edu');

-- Seed HOD (Password: password123)
INSERT INTO staff (email, password, full_name, department, designation, role)
SELECT 'hod.cse@college.edu', '$2a$10$jDIN4FYA/ED0FbHeZT/rIOTpWYO/EM0oK.MYOXf5ib8YopLIZkIwS', 'Dr. Sarah Smith', 'CSE', 'Head of Department', 'HOD'
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email = 'hod.cse@college.edu');

-- Seed Unassigned Staff for Onboarding Setup Flow (Password: password123)
INSERT INTO staff (email, password, full_name, department, designation, role)
SELECT 'new.staff@college.edu', '$2a$10$jDIN4FYA/ED0FbHeZT/rIOTpWYO/EM0oK.MYOXf5ib8YopLIZkIwS', 'Alex Turner', 'Unassigned', 'Unassigned', 'STAFF'
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email = 'new.staff@college.edu');
