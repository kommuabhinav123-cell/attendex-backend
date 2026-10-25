package com.attendex.service;

import com.attendex.entity.AttendanceRecord;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * Service for generating CSV reports from attendance data.
 */
@Service
public class ReportService {

    /**
     * Generate a CSV file from attendance records.
     * Returns raw CSV bytes for download.
     */
    public byte[] generateCsv(List<AttendanceRecord> records) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        // CSV header
        writer.println("Roll Number,Student Name,Subject,Date,Period,Status,Remarks");

        for (AttendanceRecord record : records) {
            writer.printf("%s,%s,%s,%s,%d,%s,%s%n",
                    escapeCsv(record.getStudent().getRollNumber()),
                    escapeCsv(record.getStudent().getUser().getFullName()),
                    escapeCsv(record.getAttendanceSession().getSubject().getName()),
                    record.getAttendanceSession().getSessionDate(),
                    record.getAttendanceSession().getSessionNumber(),
                    record.getStatus(),
                    escapeCsv(record.getRemarks() != null ? record.getRemarks() : "")
            );
        }

        writer.flush();
        return out.toByteArray();
    }

    /** Escape special characters for CSV */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
