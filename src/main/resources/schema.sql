-- Smart Attendance System Schema DDL
-- Database 'smart_attendance' is already created and configured in application.yml

-- 1. Staff Master Table
CREATE TABLE IF NOT EXISTS staff (
    staff_id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- BCrypt Hash
    full_name VARCHAR(100) NOT NULL,
    department VARCHAR(150) NOT NULL,
    designation VARCHAR(150) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'STAFF',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Attendance Records
CREATE TABLE IF NOT EXISTS attendance (
    attendance_id INT PRIMARY KEY AUTO_INCREMENT,
    staff_id INT NOT NULL,
    date DATE NOT NULL,
    time_marked TIME NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id) ON DELETE CASCADE,
    UNIQUE KEY unique_staff_daily_attendance (staff_id, date) -- Prevents duplicate daily checks
);

-- 3. Staff correction workflow for missed/GPS-failed attendance
CREATE TABLE IF NOT EXISTS attendance_correction_requests (
    correction_request_id INT PRIMARY KEY AUTO_INCREMENT,
    staff_id INT NOT NULL,
    attendance_date DATE NOT NULL,
    reason VARCHAR(40) NOT NULL,
    note VARCHAR(1000),
    status VARCHAR(25) NOT NULL DEFAULT 'PENDING_APPROVAL',
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    reviewed_by INT NULL,
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES staff(staff_id) ON DELETE SET NULL
);

-- 4. Immutable Audit Log
CREATE TABLE IF NOT EXISTS audit_log (
    audit_id INT PRIMARY KEY AUTO_INCREMENT,
    attendance_id INT NOT NULL,
    action VARCHAR(50) NOT NULL,
    performed_by INT NULL,
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (attendance_id) REFERENCES attendance(attendance_id),
    FOREIGN KEY (performed_by) REFERENCES staff(staff_id)
);
