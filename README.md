# ⚙️ Attendex Backend Module

This folder contains the Java 21 / Spring Boot 3.2.5 backend codebase for Attendex.

## 📁 Package Structure

```
backend/
├── pom.xml                                   # Maven build file & dependencies
└── src/
    ├── main/
    │   ├── java/com/attendex/
    │   │   ├── AttendexApplication.java      # Application entry point
    │   │   ├── config/                       # Spring Security, CommandLineRunner DataInitializer
    │   │   ├── controller/                   # Spring MVC Controllers (Auth, Admin, Faculty, Student)
    │   │   ├── dto/                          # Request/Response Data Transfer Objects
    │   │   ├── entity/                       # JPA Database Entities
    │   │   ├── exception/                    # Global Exception Handler & Custom Exceptions
    │   │   ├── repository/                   # Spring Data JPA Repositories
    │   │   ├── security/                     # UserDetailsService & Role Success Handler
    │   │   └── service/                      # Academic, Attendance, Faculty, Student Services
    │   └── resources/
    │       └── application.properties        # Database & server settings
    └── test/
        └── java/com/attendex/
            └── AttendanceServiceTest.java    # Automated unit tests
```

## 🚀 Running the Backend
From the project root:
```cmd
set JAVA_HOME=D:\Program Files\Java\jdk-21.0.12.1+1
set PATH=%JAVA_HOME%\bin;D:\tools\apache-maven-3.9.6\bin;%PATH%
mvn spring-boot:run -Dmaven.repo.local=D:\Attendex-cache\maven
```
Or run `run-attendex.bat` directly.
