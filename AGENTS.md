# Project Structure Guide (AI Context)

This document describes the structure and conventions of this Spring Boot application so that AI agents and developers can navigate and extend it efficiently.

## Overview

A REST API for personal productivity and finance tracking. It manages **Goals** broken into **Milestones**, which are completed by logging **Focus Sessions**, plus a fully implemented **Saving Goals** module (with deposits that accumulate a current amount). It also sends **deadline-alert Notifications** by email for upcoming goals. **Expenses** and **Habits** remain skeletons only — not yet implemented.

## Tech Stack

- Java 21
- Spring Boot 3.5.15
- Spring Web (REST controllers)
- Spring Data JPA (Hibernate, PostgreSQL dialect)
- Bean Validation (`spring-boot-starter-validation`) — now in use (`@NotBlank` on `MilestoneRequest`, `@NotNull`/`@Min` on `FocusSessionRequest`)
- Lombok (`@Data`, `@Getter/@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- springdoc-openapi 2.8.17 (`springdoc-openapi-starter-webmvc-ui`, Swagger UI available)
- PostgreSQL (jdbc driver), database `koaldav` (see `application.properties`) — note: **not** Oracle anymore
- Spring Security (CORS config, CSRF off, stateless; `BCryptPasswordEncoder` bean). **No login is enforced:** all `/api/**` requests are `permitAll()`
- Spring Mail (`spring-boot-starter-mail`, Gmail SMTP) used by `EmailService` for notifications
- jjwt 0.12.6 and MapStruct 1.5.5 are on the classpath but **not actually wired** (mappers are manual `@Component` classes)
- Build tool: Gradle (Groovy DSL)

## Build & Run

```bash
./gradlew build        # compile + test
./gradlew bootRun      # run the application
./gradlew test         # run tests (JUnit Platform)
```

- Base URL: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- Database is configured in `src/main/resources/application.properties` (PostgreSQL `jdbc:postgresql://localhost:5432/koaldav`, user `postgres`, `ddl-auto=update`, `show-sql=true`).
- SMTP/notification env vars: `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_DEFAULT_RECIPIENT`. Without them, mail delivery fails silently (logged) and notifications default to `sreyleng143@gmail.com`. **Do not commit real mail credentials — they are read from env vars.**

## Directory Structure

```
project/
├── build.gradle                 # dependencies & build config
├── settings.gradle              # rootProject.name = 'project'
├── gradlew / gradlew.bat        # Gradle wrapper
└── src/
    ├── main/
    │   ├── java/com/example/project/
    │   │   ├── ProjectApplication.java        # @SpringBootApplication + @EnableScheduling
    │   │   ├── config/
    │   │   │   └── SecurityConfig.java        # CORS, CSRF off, stateless, BCrypt bean; /api/** permitAll
    │   │   ├── controller/
    │   │   │   ├── GoalController.java
    │   │   │   ├── MilestoneController.java
    │   │   │   ├── FocusSessioncontroller.java   # note lowercase 'c' in name
    │   │   │   ├── SavingGoalController.java
    │   │   │   ├── NotificationController.java
    │   │   │   ├── ExpenseController.java        # SKELETON
    │   │   │   └── HabitController.java          # SKELETON
    │   │   ├── service/
    │   │   │   ├── GoalService.java
    │   │   │   ├── MilestoneService.java
    │   │   │   ├── FocusSessionService.java
    │   │   │   ├── SavingGoalService.java
    │   │   │   ├── NotificationService.java      # scheduled deadline checks + email alerts
    │   │   │   ├── EmailService.java             # JavaMailSender wrapper
    │   │   │   ├── ExpenseService.java           # SKELETON
    │   │   │   └── HabitService.java             # SKELETON
    │   │   ├── repository/
    │   │   │   ├── GoalRepository.java
    │   │   │   ├── MilestoneRepository.java
    │   │   │   ├── FocusSessionRepository.java
    │   │   │   ├── SavingGoalRepository.java
    │   │   │   ├── SavingDepositRepository.java
    │   │   │   ├── NotificationRepository.java
    │   │   │   ├── ExpenseRepository.java         # SKELETON (plain class, not a JPA repo)
    │   │   │   └── HabitRepository.java           # SKELETON
    │   │   ├── Entity/                         # JPA entities (@Entity, PascalCase package)
    │   │   │   ├── Goal.java
    │   │   │   ├── Milestone.java
    │   │   │   ├── FocusSession.java
    │   │   │   ├── SavingGoal.java
    │   │   │   ├── SavingDeposit.java
    │   │   │   ├── Notification.java            # plain getters/setters, NO Lombok
    │   │   │   ├── Expense.java                 # SKELETON
    │   │   │   └── Habit.java                   # SKELETON
    │   │   ├── Enum/
    │   │   │   ├── GoalStatus.java              # IN_PROGRESS, COMPLETED, MISSED
    │   │   │   ├── MilestoneStatus.java         # IN_PROGRESS, COMPLETED, MISSED
    │   │   │   └── NotificationType.java        # DEADLINE_ALERT, SYSTEM_INFO
    │   │   ├── dto/
    │   │   │   ├── request/                     # GoalRequest, MilestoneRequest, FocusSessionRequest,
    │   │   │   │                                # SavingGoalRequest, SavingDepositRequest,
    │   │   │   │                                # ExpenseRequest / HabitRequest (SKELETON)
    │   │   │   ├── response/                    # GoalResponse, MilestoneResponse, FocusSessionResponse,
    │   │   │   │                                # SavingGoalResponse, SavingDepositResponse, NotificationResponse,
    │   │   │   │                                # ExpenseResponse / HabitResponse (SKELETON)
    │   │   │   └── exception/
    │   │   │       ├── GlobalExceptionHandler.java    # @RestControllerAdvice (404 / 400 / 500)
    │   │   │       ├── ErrorResponse.java             # timestamp, status, error, message, path
    │   │   │       └── ResourceNotFoundException.java
    │   │   └── mapper/                          # @Component classes, Entity <-> DTO mapping
    │   │       ├── GoalMapper.java
    │   │       ├── MilestoneMapper.java
    │   │       ├── FocusSessionMapper.java
    │   │       ├── SavingGoalMapper.java
    │   │       └── NotificationMapper.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/example/project/
            ├── controller/      # FocusSessionControllerTest (ACTIVE), GoalControllerTest + MilestoneControllerTest (commented out)
            └── service/         # NotificationServiceTest (ACTIVE), Goal/Milestone/FocusSessionServiceTest (commented out)
```

## Domain Model (implemented modules)

### Goal → Milestone → FocusSession

- **Goal** (`goals` table): `id`, `title`, `description`, `deadline` (LocalDate), `status` (GoalStatus), `milestone` (OneToMany to Milestone, cascade ALL + orphanRemoval). Note the collection field is named `milestone` (singular) — access it via `goal.getMilestone()`.
- **Milestone** (`milestone` table): `id`, `title`, `status` (MilestoneStatus), `goal` (ManyToOne → `goal_id`), `focusSessions` (OneToMany, cascade ALL).
- **FocusSession** (`focus_session` table): `id`, `durationMinutes` (Integer), `focusedDate` (LocalDate), `milestone` (ManyToOne → `milestone_id`).

### Saving Goals

- **SavingGoal** (`saving_goals` table): `id`, `title`, `icon` (default `"piggy"`), `targetAmount` (BigDecimal), `currentAmount` (BigDecimal, starts at 0), `deadline`, `description`, `deposits` (OneToMany, cascade ALL + orphanRemoval).
- **SavingDeposit** (`saving_deposits` table): `id`, `title` (default `"One-time Deposit"`), `depositDate` (LocalDate), `source`, `amount` (BigDecimal), `notes`, `savingGoal` (ManyToOne → `saving_goal_id`).

### Notifications

- **Notification** (`notifications` table): `id`, `userId`, `goalId` (unique constraint per userId+goalId), `goalTitle`, `deadline`, `daysLeft`, `warningMessage`, `type` (NotificationType, always `DEADLINE_ALERT`), `isRead`, `createdAt`. This entity uses explicit getters/setters (no Lombok).

## Business Rules

- A milestone can only be marked **COMPLETED** if the total focus time across its sessions is **at least 60 minutes**; otherwise a `RuntimeException("Need at least 1 hour focus time")` is thrown.
- When all milestones of a goal are completed, the goal's status is set to **COMPLETED** automatically.
- `focusedDate` of a focus session is set server-side to `LocalDate.now()` at creation time (not taken from the client).
- `GoalService.updateMissedGoals()` runs nightly (cron `0 0 0 * * ?`) and flips overdue IN_PROGRESS goals (and their IN_PROGRESS milestones) to **MISSED**.
- On goal create/update, `notificationService.sendDeadlineAlertForGoal()` is called. It only alerts when a goal is within **3 days** of its deadline (today → urgent variant). It upserts one notification per userId+goalId; an email is (re)sent only when `daysLeft` changes.
- `NotificationService.checkGoalDeadlines()` runs daily at 08:00 (cron `0 0 8 * * ?`) to scan for newly due goals. Both scheduled jobs depend on `@EnableScheduling` in `ProjectApplication`.

## API Endpoints

| Method | Path | Purpose |
| ------ | ---- | ------- |
| POST | `/api/goals` | Create a goal (triggers deadline check) |
| GET | `/api/goals/{id}` | Fetch a goal by id |
| GET | `/api/goals` | List all goals; optional `?status=IN_PROGRESS` filter |
| PUT | `/api/goals/{id}` | Update a goal (re-triggers deadline check) |
| DELETE | `/api/goals/{id}` | Delete a goal (204) |
| POST | `/api/goals/{goalId}/milestones` | Create a milestone for a goal |
| GET | `/api/goals/{goalId}/milestones` | List milestones for a goal |
| GET | `/api/goals/{goalId}/milestones/{milestoneId}` | Fetch a milestone |
| PATCH | `/api/goals/{goalId}/milestones/{milestoneId}/complete` | Complete a milestone (requires ≥60 min focus) |
| POST | `/api/milestones/{milestoneId}/sessions` | Log a focus session |
| GET | `/api/milestones/{milestoneId}/sessions` | List sessions for a milestone |
| POST | `/api/saving-goals` | Create a saving goal (201) |
| GET | `/api/saving-goals` | List all saving goals |
| GET | `/api/saving-goals/{id}` | Fetch a saving goal |
| PUT | `/api/saving-goals/{id}` | Update a saving goal (partial) |
| DELETE | `/api/saving-goals/{id}` | Delete a saving goal (204) |
| POST | `/api/saving-goals/{id}/deposits` | Add a deposit; adds to `currentAmount` (201) |
| GET | `/api/notifications?userId=1` | List user's notifications |
| GET | `/api/notifications/unread?userId=1` | List unread notifications |
| PATCH | `/api/notifications/{id}/read` | Mark a notification read |
| GET | `/api/notifications/{id}/open` | Mark read + redirect to frontend (`http://localhost:5173/goal`) |
| POST | `/api/notifications/trigger-check` | Manually run the deadline check |

`SavingGoalController` and `NotificationController` also carry `@CrossOrigin(origins = "*")` on top of the global CORS config in `SecurityConfig`.

## Code Conventions

- **Package naming is inconsistent** — please follow the existing layout:
  - Entities use `Entity` (capital E): `com.example.project.Entity.*`
  - Enums use `Enum`: `com.example.project.Enum.*`
  - Others use lowercase: `config`, `controller`, `service`, `repository`, `dto`, `mapper`.
- **Controllers**: `@RestController` + constructor injection (no `@Autowired` field injection). Controllers stay thin and delegate to services.
- **Services**: `@Service` + constructor injection. Services own business logic and call mappers for entity/DTO conversion.
- **Repositories**: extend `JpaRepository<Entity, Long>` and are annotated `@Repository`. (Exception: `ExpenseRepository`/`HabitRepository` are skeleton plain classes.)
- **Mapping**: done via the `mapper` package — one `@Component` mapper per module with `toEntity(Request)` / `toResponse(Entity)` methods. `SavingGoalMapper` also has `toDepositEntity` / `toDepositResponse`. New modules should follow this pattern instead of manual constructor calls in services.
- **DTOs**: request DTOs use `@Data` + `@NoArgsConstructor`; response DTOs use `@Getter/@Setter` + `@AllArgsConstructor` (mappers construct responses positionally).
- **Status handling**: on creation, `status` is defaulted server-side to `IN_PROGRESS` inside the mapper's `toEntity`; it is not accepted from the client.
- **Error handling**: `GlobalExceptionHandler` (`@RestControllerAdvice`) is implemented and returns `ErrorResponse` bodies — 404 for `ResourceNotFoundException`, 400 for `IllegalState`/`IllegalArgumentException`/validation/malformed JSON/type mismatch, 500 fallback. **However, services still throw raw `RuntimeException`** (e.g. "Goal not found", the 60-min focus rule), which lands in the 500 fallback — `ResourceNotFoundException` is currently only referenced by the handler and tests, not by services.

## Implementation Status

| Module | Status |
| ------ | ------ |
| Goal | Implemented (create, get, list + status filter, update, delete, nightly MISSED sweep) |
| Milestone | Implemented (create, list, get, complete w/ focus-time rule, auto goal status update) |
| FocusSession | Implemented (create + list; date auto-set to today) |
| SavingGoal | Implemented (CRUD + deposits that accumulate `currentAmount`) |
| Notification | Implemented (deadline alerts, email via `EmailService`, scheduled checks, read/unread) |
| Expense | Skeleton — entity, controller, service, repository, DTOs are empty stubs |
| Habit | Skeleton — empty stubs |
| Exception handling | Implemented — `GlobalExceptionHandler` + `ErrorResponse` + `ResourceNotFoundException` |
| Tests | Partial — `FocusSessionControllerTest` (4 tests) and `NotificationServiceTest` (1 test) are active; `GoalControllerTest`, `MilestoneControllerTest`, `GoalServiceTest`, `MilestoneServiceTest`, `FocusSessionServiceTest` are fully commented out |

## Notes / Known Issues

- Server runs on **port 8081** (was 8080). Frontend CORS allows `http://localhost:5173` and `http://localhost:3000`.
- Database is **PostgreSQL** (`koaldav`), not Oracle as originally planned.
- `FocusSessionResponse` maps the response field to `date` while the entity field is `focusedDate` (intentional rename in the mapper — keep consistent when extending).
- Security is configured but **permissive**: `/api/**` is `permitAll()` so there is no real authentication. jjwt and MapStruct dependencies exist but are not wired into any filter/mapping pipeline.
- `Notification` entity uses explicit getters/setters instead of Lombok (unlike the other entities).
- No production credentials should be committed; SMTP username/password come from env vars.