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
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = System.getenv("SPRING_DATASOURCE_URL");
        }

        if (dbUrl != null && !dbUrl.isBlank()) {
            String jdbcUrl = dbUrl;
            String user = System.getenv("DB_USERNAME");
            String pass = System.getenv("DB_PASSWORD");

            if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://") || dbUrl.startsWith("jdbc:postgresql:")) {
                System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");
                System.setProperty("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
                System.setProperty("spring.jpa.database-platform", "org.hibernate.dialect.PostgreSQLDialect");
                
                if (!dbUrl.startsWith("jdbc:")) {
                    try {
                        java.net.URI uri = new java.net.URI(dbUrl);
                        String host = uri.getHost();
                        int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                        String path = uri.getPath();
                        String userInfo = uri.getUserInfo();
                        
                        jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                        if (userInfo != null && userInfo.contains(":")) {
                            String[] parts = userInfo.split(":", 2);
                            user = parts[0];
                            pass = parts[1];
                        }
                    } catch (Exception e) {
                        jdbcUrl = "jdbc:" + dbUrl;
                    }
                }
            } else if (dbUrl.startsWith("mysql://") || dbUrl.startsWith("jdbc:mysql:")) {
                System.setProperty("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");
                System.setProperty("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
                System.setProperty("spring.jpa.database-platform", "org.hibernate.dialect.MySQLDialect");

                if (!dbUrl.startsWith("jdbc:")) {
                    try {
                        java.net.URI uri = new java.net.URI(dbUrl);
                        String host = uri.getHost();
                        int port = uri.getPort() == -1 ? 3306 : uri.getPort();
                        String path = uri.getPath();
                        String userInfo = uri.getUserInfo();
                        
                        jdbcUrl = "jdbc:mysql://" + host + ":" + port + path + "?useSSL=false&allowPublicKeyRetrieval=true";
                        if (userInfo != null && userInfo.contains(":")) {
                            String[] parts = userInfo.split(":", 2);
                            user = parts[0];
                            pass = parts[1];
                        }
                    } catch (Exception e) {
                        jdbcUrl = "jdbc:" + dbUrl;
                    }
                }
            }

            System.setProperty("spring.datasource.url", jdbcUrl);
            System.setProperty("SPRING_DATASOURCE_URL", jdbcUrl);
            System.setProperty("DB_URL", jdbcUrl);
            if (user != null) {
                System.setProperty("spring.datasource.username", user);
                System.setProperty("SPRING_DATASOURCE_USERNAME", user);
                System.setProperty("DB_USERNAME", user);
            }
            if (pass != null) {
                System.setProperty("spring.datasource.password", pass);
                System.setProperty("SPRING_DATASOURCE_PASSWORD", pass);
                System.setProperty("DB_PASSWORD", pass);
            }
        } else {
            // Local vs Cloud check:
            // If in cloud (RENDER or PORT is set), and no DB URL is set, use H2 embedded file database
            if (System.getenv("RENDER") != null || System.getenv("PORT") != null) {
                String h2Url = "jdbc:h2:file:./data/attendex_db;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false";
                System.setProperty("spring.datasource.url", h2Url);
                System.setProperty("SPRING_DATASOURCE_URL", h2Url);
                System.setProperty("DB_URL", h2Url);
                System.setProperty("spring.datasource.username", "sa");
                System.setProperty("spring.datasource.password", "");
                System.setProperty("spring.datasource.driver-class-name", "org.h2.Driver");
                System.setProperty("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            } else {
                // Local dev machine: default to local MySQL
                String mysqlUrl = "jdbc:mysql://localhost:3306/attendex_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                System.setProperty("spring.datasource.url", mysqlUrl);
                System.setProperty("SPRING_DATASOURCE_URL", mysqlUrl);
                System.setProperty("DB_URL", mysqlUrl);
                System.setProperty("spring.datasource.username", "root");
                System.setProperty("spring.datasource.password", "");
                System.setProperty("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            }
        }
    }
}
