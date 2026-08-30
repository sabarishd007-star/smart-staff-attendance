package com.smartattendance.service;

import com.smartattendance.model.Attendance;
import com.smartattendance.repository.AttendanceRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExcelExportService {

    private static final String[] COLUMNS = {
            "Record ID", "Date", "Staff Name", "Department", "Designation",
            "Time Marked", "Status", "Latitude", "Longitude"
    };

    private final AttendanceRepository attendanceRepository;

    public ExcelExportService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public ByteArrayInputStream generateAttendanceReport(
            LocalDate startDate, LocalDate endDate, String department) throws IOException {
        String normalizedDepartment = department == null || department.isBlank() ? null : department.trim();
        List<Attendance> records = attendanceRepository.findAttendanceForReport(
                startDate, endDate, normalizedDepartment);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Attendance Report");
            CellStyle headerStyle = createHeaderStyle(workbook);
            Row headerRow = sheet.createRow(0);

            for (int index = 0; index < COLUMNS.length; index++) {
                Cell cell = headerRow.createCell(index);
                cell.setCellValue(COLUMNS[index]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (Attendance attendance : records) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(attendance.getAttendanceId());
                row.createCell(1).setCellValue(attendance.getDate().toString());
                row.createCell(2).setCellValue(attendance.getStaff().getFullName());
                row.createCell(3).setCellValue(attendance.getStaff().getDepartment());
                row.createCell(4).setCellValue(attendance.getStaff().getDesignation());
                row.createCell(5).setCellValue(attendance.getTimeMarked().toString());
                row.createCell(6).setCellValue(attendance.getStatus().name());
                row.createCell(7).setCellValue(attendance.getLatitude().doubleValue());
                row.createCell(8).setCellValue(attendance.getLongitude().doubleValue());
            }

            sheet.createFreezePane(0, 1);
            for (int index = 0; index < COLUMNS.length; index++) {
                sheet.autoSizeColumn(index);
            }

            workbook.write(output);
            return new ByteArrayInputStream(output.toByteArray());
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(headerFont);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}
