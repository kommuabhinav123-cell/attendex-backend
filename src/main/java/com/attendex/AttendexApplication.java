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
        SpringApplication.run(AttendexApplication.class, args);
    }
}
