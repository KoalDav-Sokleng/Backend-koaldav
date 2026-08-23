# Project Structure Guide (AI Context)

This document describes the structure and conventions of this Spring Boot application so that AI agents and developers can navigate and extend it efficiently.

## Overview

A REST API for personal productivity and finance tracking. It manages **Goals** that are broken into **Milestones**, which are completed by logging **Focus Sessions**. It also has placeholder modules for **Expenses**, **Habits**, and **Saving Goals** (skeletons only — not yet implemented).

## Tech Stack

- Java 21
- Spring Boot 3.5.15
- Spring Web (REST controllers)
- Spring Data JPA (Hibernate, Oracle dialect)
- Bean Validation (`spring-boot-starter-validation` is on the classpath, not yet used in DTOs)
- Lombok (reduces boilerplate: `@Data`, `@Getter/@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- springdoc-openapi 2.5.0 (Swagger UI available)
- Oracle Database (ojdbc11 driver)
- Build tool: Gradle (Groovy DSL)

## Build & Run

```bash
./gradlew build        # compile + test
./gradlew bootRun      # run the application
./gradlew test         # run tests (JUnit Platform)
```

- Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Database is configured in `src/main/resources/application.properties` (Oracle, `jdbc:oracle:thin:@localhost:1521:free`, user `testapi`, `ddl-auto=update`, `show-sql=true`).

## Directory Structure

```
project/
├── build.gradle                 # dependencies & build config
├── settings.gradle              # rootProject.name = 'project'
├── gradlew / gradlew.bat        # Gradle wrapper
└── src/main/
    ├── java/com/example/project/
    │   ├── ProjectApplication.java        # @SpringBootApplication entry point
    │   ├── controller/                    # REST endpoints (@RestController)
    │   │   ├── GoalController.java
    │   │   ├── MilestoneController.java
    │   │   ├── FocusSessioncontroller.java   # note lowercase 'c' in name
    │   │   ├── ExpenseController.java        # SKELETON
    │   │   ├── HabitController.java          # SKELETON
    │   │   └── SavingGoalController.java     # SKELETON
    │   ├── service/                        # business logic (@Service)
    │   │   ├── GoalService.java            # uses GoalMapper (old manual mapping kept as comments)
    │   │   ├── MilestoneService.java
    │   │   ├── FocusSessionService.java    # uses FocusSessionMapper
    │   │   ├── ExpenseService.java         # SKELETON
    │   │   ├── HabitService.java           # SKELETON
    │   │   └── SavingGoalService.java      # SKELETON
    │   ├── repository/                     # Spring Data JPA repositories
    │   │   ├── GoalRepository.java
    │   │   ├── MilestoneRepository.java
    │   │   ├── FocusSessionRepository.java
    │   │   ├── ExpenseRepository.java       # SKELETON
    │   │   ├── HabitRepository.java         # SKELETON
    │   │   └── SavingGoalRepository.java    # SKELETON
    │   ├── Entity/                         # JPA entities (@Entity, PascalCase package)
    │   │   ├── Goal.java
    │   │   ├── Milestone.java
    │   │   ├── FocusSession.java
    │   │   ├── Expense.java                 # SKELETON
    │   │   ├── Habit.java                   # SKELETON
    │   │   └── SavingGoal.java              # SKELETON
    │   ├── Enum/
    │   │   ├── GoalStatus.java              # IN_PROGRESS, COMPLETED, MISSED
    │   │   └── MilestoneStatus.java         # IN_PROGRESS, COMPLETED, MISSED
    │   ├── dto/
    │   │   ├── request/                     # GoalRequest, MilestoneRequest, FocusSessionRequest,
    │   │   │                                # ExpenseRequest / HabitRequest / SavingGoalRequest (SKELETON)
    │   │   ├── response/                    # GoalResponse, MilestoneResponse, FocusSessionResponse,
    │   │   │                                # ExpenseResponse / HabitResponse / SavingGoalResponse (SKELETON)
    │   │   └── exception/
    │   │       ├── GlobalExceptionHandler.java        # SKELETON
    │   │       └── ResourceNotFoundException.java     # SKELETON
    │   └── mapper/                          # @Component classes, Entity <-> DTO mapping
    │       ├── GoalMapper.java
    │       ├── MilestoneMapper.java
    │       └── FocusSessionMapper.java
    └── resources/
        └── application.properties
```

## Domain Model (implemented modules)

The core implemented feature is a Goal → Milestone → FocusSession hierarchy:

- **Goal** (`goals` table): `id`, `title`, `deadline` (LocalDate), `status` (GoalStatus), `milestone` (OneToMany to Milestone, cascade ALL + orphanRemoval). Note the collection field is named `milestone` (singular) — access it via `goal.getMilestone()`.
- **Milestone** (`milestone` table): `id`, `title`, `status` (MilestoneStatus), `goal` (ManyToOne → `goal_id`), `focusSessions` (OneToMany, cascade ALL).
- **FocusSession** (`focus_session` table): `id`, `durationMinutes` (Integer), `focusedDate` (LocalDate), `milestone` (ManyToOne → `milestone_id`).

### Business rules

- A milestone can only be marked **COMPLETED** if the total focus time across its sessions is **at least 60 minutes**; otherwise a `RuntimeException("Need at least 1 hour focus time")` is thrown.
- When all milestones of a goal are completed, the goal's status is set to **COMPLETED** automatically.
- `focusedDate` of a focus session is set server-side to `LocalDate.now()` at creation time (not taken from the client).

## API Endpoints (implemented)

| Method | Path | Purpose |
| ------ | ---- | ------- |
| POST | `/api/goals` | Create a goal |
| GET | `/api/goals/{id}` | Fetch a goal by id |
| GET | `/api/goals` | List all goals |
| POST | `/api/goals/{goalId}/milestones` | Create a milestone for a goal |
| PATCH | `/api/goals/{goalId}/milestones/{milestoneId}/complete` | Complete a milestone (requires ≥60 min focus) |
| POST | `/api/milestones/{milestoneId}/sessions` | Log a focus session for a milestone |

## Code Conventions

- **Package naming is inconsistent** — please follow the existing layout:
  - Entities use `Entity` (capital E): `com.example.project.Entity.*`
  - Enums use `Enum`: `com.example.project.Enum.*`
  - Others use lowercase: `controller`, `service`, `repository`, `dto`, `mapper`.
- **Controllers**: `@RestController` + constructor injection (no `@Autowired` field injection). Controllers stay thin and delegate to services.
- **Services**: `@Service` + constructor injection. Services own business logic and call mappers for entity/DTO conversion.
- **Repositories**: extend `JpaRepository<Entity, Long>` (e.g. `GoalRepository` is annotated `@Repository`; others rely on Spring Data auto-detection).
- **Mapping**: done via the `mapper` package — one `@Component` mapper per module with `toEntity(Request)` / `toResponse(Entity)` methods (`GoalMapper`, `MilestoneMapper`, `FocusSessionMapper`). New modules should follow this pattern instead of manual constructor calls in services.
- **DTOs**: request DTOs use `@Data` or `@Getter/@Setter` + `@NoArgsConstructor`; response DTOs use `@Getter/@Setter` + `@AllArgsConstructor` (mappers construct responses positionally).
- **Status handling**: on creation, `status` is defaulted server-side to `IN_PROGRESS` inside the mapper's `toEntity`; it is not accepted from the client.
- **Error handling**: currently inconsistent — services throw raw `RuntimeException`. `GlobalExceptionHandler` and `ResourceNotFoundException` exist as empty skeletons to be implemented.

## Implementation Status

| Module | Status |
| ------ | ------ |
| Goal | Implemented (create, get by id, list all) |
| Milestone | Implemented (create, complete w/ focus-time rule, auto goal status update) |
| FocusSession | Implemented (create; date auto-set to today) |
| Expense | Skeleton — entity, controller, service, repository, DTOs are empty stubs |
| Habit | Skeleton — empty stubs |
| SavingGoal | Skeleton — empty stubs |
| Exception handling | Skeleton — files exist but are empty |

## Notes / Known Issues

- `FocusSessionResponse` maps the response field to `date` while the entity field is `focusedDate` (intentional rename in the mapper — keep consistent when extending).
- `GoalService` contains a large block of commented-out legacy code (manual mapping before `GoalMapper` was introduced); safe to delete when touching that file.
- `MilestoneService` has an unused `import static java.util.Arrays.stream;`.
- No security / authentication is configured.
- No tests are currently written.
