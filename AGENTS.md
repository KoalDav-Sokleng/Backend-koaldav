# Project Structure Guide (AI Context)

This document describes the structure, architectural patterns, and conventions of this Spring Boot application so that AI agents and developers can navigate, maintain, and extend it reliably.

---

## Overview

A modular REST API for personal productivity and financial wellness tracking. Key domains include:

- **Authentication & Security**: JWT-based stateless authentication, user registration, login, and 6-digit OTP email password reset.
- **Goals & Focus**: Goal tracking with hierarchical Milestones and Focus Sessions (logging focused study/work duration, milestone completion validation $\ge 60$ min).
- **Habits & Garden Gamification**: Daily habit tracking paired with a Duolingo-style sunflower garden (growth stages 0–7, multi-day inactivity step-back decay, streak freeze tokens).
- **Saving Goals & Trip Goals**: Target savings accumulation with deposits and travel goal management.
- **Expense & Finance**: Category expense tracking with monthly analytics and financial summaries.
- **Notifications**: Automated deadline sweeps across goals and real-time STOMP WebSocket + email alerts.
- **Error Handling**: Standardized global exception handling with structured JSON error responses.

---

## Tech Stack

- **Java**: 21
- **Framework**: Spring Boot 3.5.15
- **Security**: Spring Security 6 + JJWT (`io.jsonwebtoken` 0.12.6)
- **Database / Persistence**: Spring Data JPA (Hibernate), PostgreSQL driver (`org.postgresql:postgresql`)
- **Email**: Spring Boot Starter Mail (JavaMailSender / Gmail SMTP)
- **Validation**: Bean Validation (`spring-boot-starter-validation`)
- **DTO Mapping**: MapStruct 1.5.5.Final + Lombok
- **API Documentation**: springdoc-openapi 2.8.17 (Swagger UI)
- **Build Tool**: Gradle (Groovy DSL)

---

## Build & Run

```bash
./gradlew build        # compile + test + package
./gradlew bootRun      # run application locally
./gradlew test         # run JUnit Platform unit & integration tests
```

- **Base URL**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html` or `http://localhost:8080/swagger-ui/index.html`
- **Database Configuration**: Configured in `src/main/resources/application.properties` (PostgreSQL, `ddl-auto=update`, `show-sql=true`).

---

## Directory Structure

```
project/
├── build.gradle                 # dependencies, plugins & build config
├── settings.gradle              # rootProject.name = 'project'
├── gradlew / gradlew.bat        # Gradle wrapper
└── src/
    ├── main/
    │   ├── java/com/example/project/
    │   │   ├── ProjectApplication.java        # @SpringBootApplication entry point
    │   │   ├── controller/                    # REST endpoints (@RestController)
    │   │   │   ├── AuthController.java
    │   │   │   ├── GoalController.java
    │   │   │   ├── MilestoneController.java
    │   │   │   ├── FocusSessioncontroller.java
    │   │   │   ├── HabitController.java
    │   │   │   ├── GardenController.java
    │   │   │   ├── SavingGoalController.java
    │   │   │   └── ExpenseController.java
    │   │   ├── service/                       # Business logic (@Service)
    │   │   │   ├── AuthService.java
    │   │   │   ├── OtpService.java
    │   │   │   ├── EmailService.java
    │   │   │   ├── GoalService.java
    │   │   │   ├── MilestoneService.java
    │   │   │   ├── FocusSessionService.java
    │   │   │   ├── HabitService.java
    │   │   │   ├── GardenService.java
    │   │   │   ├── SavingGoalService.java
    │   │   │   └── ExpenseService.java
    │   │   ├── repository/                    # Spring Data JPA repositories
    │   │   │   ├── UserRepository.java
    │   │   │   ├── PasswordResetOtpRepository.java
    │   │   │   ├── GoalRepository.java
    │   │   │   ├── MilestoneRepository.java
    │   │   │   ├── FocusSessionRepository.java
    │   │   │   ├── HabitRepository.java
    │   │   │   ├── GardenRepository.java
    │   │   │   ├── SavingGoalRepository.java
    │   │   │   └── ExpenseRepository.java
    │   │   ├── Entity/                        # JPA entities (@Entity - PascalCase package)
    │   │   │   ├── User.java
    │   │   │   ├── PasswordResetOtp.java
    │   │   │   ├── Goal.java
    │   │   │   ├── Milestone.java
    │   │   │   ├── FocusSession.java
    │   │   │   ├── Habit.java
    │   │   │   ├── Garden.java
    │   │   │   ├── SavingGoal.java
    │   │   │   └── Expense.java
    │   │   ├── Enum/                          # Enums (PascalCase package)
    │   │   │   ├── Role.java
    │   │   │   ├── GoalStatus.java            # IN_PROGRESS, COMPLETED, MISSED
    │   │   │   └── MilestoneStatus.java       # IN_PROGRESS, COMPLETED, MISSED
    │   │   ├── security/                      # Spring Security & JWT components
    │   │   │   ├── CustomUserDetailsService.java
    │   │   │   ├── JwtAuthFilter.java
    │   │   │   ├── JwtUtil.java
    │   │   │   └── SecurityConfig.java
    │   │   ├── dto/                           # Data Transfer Objects
    │   │   │   ├── AuthResponse.java
    │   │   │   ├── LoginRequest.java
    │   │   │   ├── RegisterRequest.java
    │   │   │   ├── ForgotPasswordRequest.java
    │   │   │   ├── VerifyOtpRequest.java
    │   │   │   ├── ResetPasswordRequest.java
    │   │   │   ├── request/                   # GoalRequest, MilestoneRequest, HabitRequest, etc.
    │   │   │   ├── response/                  # GoalResponse, HabitResponse, GardenResponse, etc.
    │   │   │   └── exception/                 # GlobalExceptionHandler, ErrorResponse, ResourceNotFoundException
    │   │   └── mapper/                        # Entity <-> DTO mappers
    │   │       ├── GoalMapper.java
    │   │       ├── MilestoneMapper.java
    │   │       ├── FocusSessionMapper.java
    │   │       ├── HabitMapper.java
    │   │       └── GardenMapper.java
    │   └── resources/
    │       └── application.properties         # DB credentials, JWT secret, Mail configuration
    └── test/
        └── java/com/example/project/
            └── service/                       # Unit and integration test suites
```

---

## Code Conventions & Architecture Rules

1. **Package Casing (CRITICAL)**:
   - JPA Entities are located in `com.example.project.Entity.*` (**Capital `E`**). Do **NOT** use `import com.example.project.entity.*` as it breaks builds on case-sensitive filesystems.
   - Enums are located in `com.example.project.Enum.*` (**Capital `E`**).
   - All other packages are lowercase: `controller`, `service`, `repository`, `security`, `dto`, `mapper`.

2. **Security & Authentication**:
   - Stateless JWT authentication via `JwtAuthFilter` reading the `Authorization: Bearer <token>` header.
   - Public endpoints permitted by `SecurityConfig`:
     - `/api/auth/**`
     - `/swagger-ui/**`, `/v3/api-docs/**`, `/swagger-ui.html`
     - `/ws/**` (WebSocket handshake)
   - All other endpoints require authentication.

3. **Controllers & Services**:
   - Constructor injection only (no `@Autowired` field injection).
   - Controllers remain thin and delegate directly to services.
   - `@Valid` is used on request bodies to enforce validation annotations (`@NotBlank`, `@Email`, `@Min`, etc.).

4. **Error Handling**:
   - Handled centrally by [`GlobalExceptionHandler`](file:///d:/Springboot%20etec/project/project/src/main/java/com/example/project/dto/exception/GlobalExceptionHandler.java).
   - Returns consistent [`ErrorResponse`](file:///d:/Springboot%20etec/project/project/src/main/java/com/example/project/dto/exception/ErrorResponse.java) JSON payloads (`status`, `message`, `timestamp`).
   - Use [`ResourceNotFoundException`](file:///d:/Springboot%20etec/project/project/src/main/java/com/example/project/dto/exception/ResourceNotFoundException.java) for missing entity lookups.

5. **Entity / Database Field Types**:
   - Avoid `@Column(precision = ..., scale = ...)` on `Double` fields (causes Hibernate 6 schema validation errors). Use standard `@Column` instead.
   - Use `CAST(:param AS string)` in native PostgreSQL queries when handling nullable parameters.

---

## API Endpoints Reference

### 🔐 Authentication (`/api/auth`)

| Method | Path                        | Purpose                                     |
| :----- | :-------------------------- | :------------------------------------------ |
| `POST` | `/api/auth/register`        | Register a new user account                 |
| `POST` | `/api/auth/login`           | Authenticate user and return JWT token      |
| `POST` | `/api/auth/forgot-password` | Generate & email 6-digit password reset OTP |
| `POST` | `/api/auth/verify-otp`      | Verify validity of 6-digit OTP              |
| `POST` | `/api/auth/reset-password`  | Set new password with verified OTP          |

### 🎯 Goals & Milestones (`/api/goals`, `/api/milestones`)

| Method  | Path                                                    | Purpose                                               |
| :------ | :------------------------------------------------------ | :---------------------------------------------------- |
| `POST`  | `/api/goals`                                            | Create a goal                                         |
| `GET`   | `/api/goals/{id}`                                       | Fetch goal details with milestones                    |
| `GET`   | `/api/goals`                                            | List all goals                                        |
| `POST`  | `/api/goals/{goalId}/milestones`                        | Create a milestone under a goal                       |
| `PATCH` | `/api/goals/{goalId}/milestones/{milestoneId}/complete` | Mark milestone complete ($\ge 60$ min focus required) |
| `POST`  | `/api/milestones/{milestoneId}/sessions`                | Log a focus session (duration in minutes)             |

### 🌿 Habits & Sunflower Garden (`/api/habits`, `/api/garden`)

| Method   | Path                      | Purpose                                               |
| :------- | :------------------------ | :---------------------------------------------------- |
| `GET`    | `/api/habits`             | List active habits                                    |
| `POST`   | `/api/habits`             | Create a new habit                                    |
| `POST`   | `/api/habits/{id}/toggle` | Toggle habit completion for a specific date           |
| `DELETE` | `/api/habits/{id}`        | Delete a habit                                        |
| `GET`    | `/api/garden`             | Get garden status (streak, growth stage 0-7, freezes) |
| `POST`   | `/api/garden/freeze`      | Use a streak freeze token                             |

### 💰 Saving Goals (`/api/saving-goals`)

| Method | Path                             | Purpose                             |
| :----- | :------------------------------- | :---------------------------------- |
| `GET`  | `/api/saving-goals`              | List saving goals                   |
| `POST` | `/api/saving-goals`              | Create a saving goal                |
| `POST` | `/api/saving-goals/{id}/deposit` | Deposit funds towards target amount |

### 📊 Expenses & Finance (`/api/expenses`)

| Method | Path                     | Purpose                                 |
| :----- | :----------------------- | :-------------------------------------- |
| `GET`  | `/api/expenses`          | List/filter expenses                    |
| `POST` | `/api/expenses`          | Record a new expense                    |
| `GET`  | `/api/expenses/overview` | 12-month summary and category breakdown |
