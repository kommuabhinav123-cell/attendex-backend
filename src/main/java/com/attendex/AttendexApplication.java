package com.attendex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main entry point for the Attendex Attendance Management System.
 * Run this class to start the Spring Boot application.
 *
 * @EnableAsync allows SMS notifications to be sent in the background
 * without blocking the attendance marking request.
 */
@SpringBootApplication
@EnableAsync
public class AttendexApplication {

    public static void main(String[] args) {
        normalizeCloudDatabaseEnvironment();
        SpringApplication.run(AttendexApplication.class, args);
    }

    private static void normalizeCloudDatabaseEnvironment() {
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = System.getenv("DATABASE_URL");
        }
        if (dbUrl != null && !dbUrl.isBlank()) {
            if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
                try {
                    java.net.URI uri = new java.net.URI(dbUrl);
                    String host = uri.getHost();
                    int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                    String path = uri.getPath();
                    String userInfo = uri.getUserInfo();
                    
                    String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                    System.setProperty("spring.datasource.url", jdbcUrl);
                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        System.setProperty("spring.datasource.username", parts[0]);
                        System.setProperty("spring.datasource.password", parts[1]);
                    }
                } catch (Exception e) {
                    if (!dbUrl.startsWith("jdbc:")) {
                        System.setProperty("spring.datasource.url", "jdbc:" + dbUrl);
                    }
                }
            } else if (dbUrl.startsWith("mysql://")) {
                try {
                    java.net.URI uri = new java.net.URI(dbUrl);
                    String host = uri.getHost();
                    int port = uri.getPort() == -1 ? 3306 : uri.getPort();
                    String path = uri.getPath();
                    String userInfo = uri.getUserInfo();
                    
                    String jdbcUrl = "jdbc:mysql://" + host + ":" + port + path + "?useSSL=false&allowPublicKeyRetrieval=true";
                    System.setProperty("spring.datasource.url", jdbcUrl);
                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        System.setProperty("spring.datasource.username", parts[0]);
                        System.setProperty("spring.datasource.password", parts[1]);
                    }
                } catch (Exception e) {
                    if (!dbUrl.startsWith("jdbc:")) {
                        System.setProperty("spring.datasource.url", "jdbc:" + dbUrl);
                    }
                }
            }
        } else {
            // If deployed on Render or cloud with no database URL configured, fallback to embedded H2 file database
            String renderEnv = System.getenv("RENDER");
            if (renderEnv != null || "true".equalsIgnoreCase(System.getenv("USE_H2"))) {
                System.setProperty("spring.datasource.url", "jdbc:h2:file:./data/attendex_db;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false");
                System.setProperty("spring.datasource.username", "sa");
                System.setProperty("spring.datasource.password", "");
                System.setProperty("spring.datasource.driver-class-name", "org.h2.Driver");
            }
        }
    }
}
