package com.attendex.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check endpoint for Render container health probes,
 * external keep-alive crons, and frontend status indicators.
 */
@RestController
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Attendex Backend");
        status.put("timestamp", System.currentTimeMillis());

        try (Connection conn = dataSource.getConnection()) {
            boolean valid = conn.isValid(2);
            status.put("database", valid ? "CONNECTED" : "UNHEALTHY");
        } catch (Exception e) {
            status.put("database", "DOWN: " + e.getMessage());
        }

        return ResponseEntity.ok(status);
    }
}
