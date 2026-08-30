-- Execute once against the MySQL smart_attendance database.
-- This script is intentionally not loaded by Spring's schema.sql initializer:
-- MySQL trigger bodies require a custom statement delimiter.

-- staff.staff_id is INT in this project, so the foreign-key column must remain INT.
ALTER TABLE audit_log MODIFY COLUMN performed_by INT NULL;
ALTER TABLE audit_log MODIFY COLUMN action
    ENUM('APPROVED', 'REJECTED', 'STATUS_CHANGED_TO_PENDING') NOT NULL;

DELIMITER //

DROP TRIGGER IF EXISTS trg_after_attendance_status_update //

CREATE TRIGGER trg_after_attendance_status_update
AFTER UPDATE ON attendance
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO audit_log (attendance_id, action, performed_by, performed_at)
        VALUES (
            NEW.attendance_id,
            CASE
                WHEN NEW.status = 'VERIFIED' THEN 'APPROVED'
                WHEN NEW.status = 'REJECTED' THEN 'REJECTED'
                WHEN NEW.status = 'PENDING' THEN 'STATUS_CHANGED_TO_PENDING'
            END,
            @current_operator_staff_id,
            NOW()
        );
    END IF;
END //

DELIMITER ;
