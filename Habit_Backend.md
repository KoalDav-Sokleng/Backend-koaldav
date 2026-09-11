# Habit Backend — Project Structure Reference

> **AI Context Document.** This file describes the actual, current state of the Spring Boot backend.
> The `AGENTS.md` file at the project root is outdated — it references Goal/Milestone/FocusSession
> modules that no longer exist. Always refer to this document instead.

---

## Overview

A REST API for a **habit tracker with a gamified garden/streak system**. Users create daily habits,
toggle them complete each day, and a "garden" entity tracks their global streak, flower growth
(0–7 stages), and freeze tokens. All streak/growth/decay logic is server-side only — the frontend
only displays what the API returns.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.15 |
| Web | Spring Web (REST controllers) |
| Persistence | Spring Data JPA + Hibernate |
| Database | **PostgreSQL** (`jdbc:postgresql://localhost:5432/koaldav`) |
| Validation | `spring-boot-starter-validation` (`@NotBlank`, `@Valid`) |
| Security | Spring Security (CSRF disabled, stateless sessions, `/api/**` permitAll) |
| JWT | jjwt 0.12.6 (on classpath, not wired into auth yet) |
| OpenAPI | springdoc-openapi 2.8.17 (Swagger UI at `/swagger-ui.html`) |
| Mapping | Manual `@Component` mappers (MapStruct is on classpath but unused) |
| Boilerplate | Lombok (`@Data`, `@Getter/@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`) |
| Build | Gradle (Groovy DSL) |
| Server port | **8081** |

## Build & Run

```bash
./gradlew build        # compile + test
./gradlew bootRun      # run the application
./gradlew test         # run tests (JUnit Platform)
```

- Base URL: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`

---

## Directory Structure

```
project/
├── build.gradle
├── settings.gradle
├── gradlew / gradlew.bat
├── Habit_Backend.md                    ← this file
└── src/main/
    └── java/com/example/project/
        ├── ProjectApplication.java              # @SpringBootApplication entry point
        ├── config/
        │   └── SecurityConfig.java              # Spring Security config (permitAll for /api/**)
        ├── controller/
        │   ├── HabitController.java             # CRUD + toggle endpoints for habits
        │   ├── GardenController.java            # GET garden state, POST freeze
        │   ├── ExpenseController.java           # SKELETON (empty class body)
        │   └── SavingGoalController.java        # SKELETON (empty class body)
        ├── service/
        │   ├── HabitService.java                # Habit CRUD, toggle logic, streak math
        │   ├── GardenService.java               # Streak, growth, decay, freeze logic
        │   ├── ExpenseService.java              # SKELETON
        │   └── SavingGoalService.java           # SKELETON
        ├── repository/
        │   ├── HabitRepository.java             # JpaRepository<Habit, Long>
        │   ├── GardenRepository.java            # JpaRepository<Garden, Long> + findTopByOrderByIdAsc
        │   ├── ExpenseRepository.java           # SKELETON (not a real JPA interface)
        │   └── SavingGoalRepository.java        # SKELETON (not a real JPA interface)
        ├── Entity/
        │   ├── Habit.java                       # @Entity — habits table
        │   ├── Garden.java                      # @Entity — garden table + garden_freeze_dates
        │   ├── GardenRepository.java            # MISPLACED empty class (not a real repo)
        │   ├── Expense.java                     # SKELETON (empty class body)
        │   └── SavingGoal.java                  # SKELETON (empty class body)
        ├── Enum/
        │   ├── GoalStatus.java                  # IN_PROGRESS, COMPLETED, MISSED (unused by any entity)
        │   └── MilestoneStatus.java             # IN_PROGRESS, COMPLETED, MISSED (unused by any entity)
        ├── dto/
        │   ├── request/
        │   │   ├── HabitRequest.java            # title (@NotBlank), target, type
        │   │   ├── ToggleRequest.java           # date field (accepted but intentionally ignored)
        │   │   ├── FreezeRequest.java           # date field (accepted but intentionally ignored)
        │   │   └── GardenRequest.java           # EMPTY FILE (0 lines)
        │   ├── response/
        │   │   ├── HabitResponse.java           # id, title, target, type, streak, completed, startDate
        │   │   ├── ToggleResponse.java          # habit + garden + justReachedPerfectDay
        │   │   ├── GardenResponse.java          # streak, bestStreak, growthStage, freezes, freezeUsedDates, lastPerfectDate
        │   │   ├── ExpenseResponse.java         # SKELETON (empty class body)
        │   │   └── SavingGoalResponse.java      # SKELETON (empty class body)
        │   └── exception/
        │       ├── GlobalExceptionHandler.java  # @RestControllerAdvice — handles 404, 400, validation, 500
        │       ├── ResourceNotFoundException.java  # @ResponseStatus(NOT_FOUND) + two constructors
        │       └── ErrorResponse.java           # timestamp, status, error, message, path
        └── mapper/
            ├── HabitMapper.java                 # toEntity, applyUpdate, toResponse
            └── GardenMapper.java                # toResponse
```

---

## Domain Model

### Habit Entity (`habits` table)

| Column | Type | Notes |
|---|---|---|
| `id` | Long (auto-generated) | Primary key |
| `title` | String | Required (`@NotBlank` in request) |
| `target` | String | Optional, defaults to `"Daily goal"` in mapper |
| `type` | String | Optional, one of `"water"`, `"workout"`, `"read"`, `"study"`, `"other"` — stored as plain String to match frontend lowercase IDs |
| `streak` | Integer | Default `0`, managed by toggle logic only |
| `last_completed_date` | LocalDate | Set when completed today, cleared on undo |
| `start_date` | LocalDate | Set server-side to `LocalDate.now()` at creation, never editable |

**Key method:** `isCompletedOn(LocalDate date)` — returns `true` if `lastCompletedDate` equals the given date. This is how the `completed` boolean is derived; there is no stored boolean.

### Garden Entity (`garden` table)

| Column | Type | Notes |
|---|---|---|
| `id` | Long (auto-generated) | Primary key |
| `streak` | Integer | Current consecutive perfect days, default `0` |
| `best_streak` | Integer | All-time best streak, default `0` |
| `growth_stage` | Integer | 0–7, drives the sunflower visual |
| `freezes` | Integer | Freeze tokens available, starts at `2` |
| `last_perfect_date` | LocalDate | Last date ALL habits were completed |
| `last_checked_date` | LocalDate | Gates decay check to once per day |

**Associated collection:** `garden_freeze_dates` table (via `@ElementCollection`) — stores dates when freezes were used.

**Singleton pattern:** `GardenService.getOrCreate()` returns the first garden row, creating one if none exists. The app is single-tenant (no auth enforced, no userId column).

---

## Business Rules

### Habit Toggle (`POST /api/habits/{id}/toggle`)

1. Server clock (`LocalDate.now()`) is always used — client-supplied date in `ToggleRequest` is ignored.
2. **Completing:** If `lastCompletedDate` is yesterday, streak increments. Otherwise streak resets to 1.
3. **Un-completing (same-day undo):** Streak decrements by 1, `lastCompletedDate` set to null.
4. After toggling, the service checks if ALL habits are completed for today.
5. Calls `GardenService.registerPerfectDayIfNeeded()` with the result.

### Garden Streak & Growth (`GardenService.registerPerfectDayIfNeeded`)

1. Only fires once per day per garden (gated by `lastPerfectDate`).
2. If all habits are completed and today hasn't been counted yet:
   - Checks if yesterday was covered (perfect day or freeze used).
   - Advances streak (continues if yesterday was covered, resets to 1 otherwise).
   - Increments `growthStage` (capped at 7).
   - Updates `bestStreak`.
   - Every 7-day milestone grants +2 freezes.
3. Returns `justReachedPerfectDay = true` only on the exact call that first reached 100% today.

### Decay (`GardenService.settleDecay`)

- Runs at most once per calendar day (gated by `lastCheckedDate`).
- If yesterday was NOT a perfect day AND NOT protected by a freeze, the flower wilts (growthStage − 1, streak → 0).
- Called automatically by `getGarden()` and `useFreeze()`.

### Freeze (`POST /api/garden/freeze`)

- Consumes 1 freeze token to protect today from decay.
- Cannot freeze if: no freezes left, already frozen today, or today is already a perfect day.

---

## API Endpoints

| Method | Path | Request Body | Response | Notes |
|---|---|---|---|---|
| `GET` | `/api/habits` | — | `List<HabitResponse>` | `completed` is computed for today |
| `POST` | `/api/habits` | `HabitRequest` | `HabitResponse` | `streak=0`, `startDate=today` server-side |
| `PUT` | `/api/habits/{id}` | `HabitRequest` | `HabitResponse` | Only updates title/target/type; streak untouched |
| `DELETE` | `/api/habits/{id}` | — | `204 No Content` | |
| `POST` | `/api/habits/{id}/toggle` | `ToggleRequest` (optional) | `ToggleResponse` | Bundles updated habit + garden + perfectDay flag |
| `GET` | `/api/garden` | — | `GardenResponse` | Triggers decay check |
| `POST` | `/api/garden/freeze` | `FreezeRequest` (optional) | `GardenResponse` | Uses one freeze token |

---

## Code Conventions

### Packages
- **Entities:** `Entity` (capital E) — `com.example.project.Entity.*`
- **Enums:** `Enum` (capital E) — `com.example.project.Enum.*`
- **All others:** lowercase — `controller`, `service`, `repository`, `dto`, `mapper`, `config`

### Controllers
- `@RestController` + constructor injection (no `@Autowired`).
- Thin layer — delegates everything to the service.
- `@Valid @RequestBody` on request DTOs where validation is needed.

### Services
- `@Service` + constructor injection.
- `@Transactional` on write methods, `@Transactional(readOnly = true)` on reads.
- Throw `ResourceNotFoundException` for missing entities (handled by `GlobalExceptionHandler`).

### Repositories
- Extend `JpaRepository<Entity, Long>`.
- Annotated with `@Repository`.

### Mappers
- `@Component` classes in the `mapper` package.
- One mapper per module: `HabitMapper`, `GardenMapper`.
- Methods: `toEntity(Request)`, `applyUpdate(Entity, Request)`, `toResponse(Entity, LocalDate)`.
- Mappers own the conversion logic — services call mappers, never construct DTOs directly.
- **Convention:** Response constructors are positional (`@AllArgsConstructor`) matching the mapper's `toResponse` call order.

### DTOs
- **Request DTOs:** `@Data @NoArgsConstructor` with validation annotations.
- **Response DTOs:** `@Getter @Setter @AllArgsConstructor`.
- `ToggleRequest.date` and `FreezeRequest.date` are accepted but intentionally ignored — server clock is authoritative.

### Error Handling
- `GlobalExceptionHandler` (`@RestControllerAdvice`) handles:
  - `ResourceNotFoundException` → 404
  - `IllegalStateException` / `IllegalArgumentException` → 400
  - `MethodArgumentNotValidException` → 400 (field-level errors)
  - `HttpMessageNotReadableException` → 400 (malformed JSON)
  - `MethodArgumentTypeMismatchException` → 400 (wrong param type)
  - `Exception` (fallback) → 500
- `ErrorResponse` DTO: `timestamp`, `status`, `error`, `message`, `path`.

---

## Cross-Module Dependency

```
HabitController → HabitService → HabitRepository
                               → HabitMapper
                               → GardenService → GardenRepository
                                               → GardenMapper
```

`HabitService.toggleHabit()` is the only place where both modules interact — after toggling a habit, it calls `GardenService.registerPerfectDayIfNeeded()` and `GardenService.currentState()` to build the `ToggleResponse`.

---

## Known Issues / Notes

- **No userId / multi-tenancy:** All data is single-tenant. If auth is added later, a userId column must be added to `Habit` and `Garden` (and their queries updated).
- **Same-day undo only:** Un-completing a habit only works reliably for today. Without a completion-log table, multi-day undo cannot restore the exact prior streak.
- **`Entity/GardenRepository.java` is a misplaced empty class** — the real repo is at `repository/GardenRepository.java`. Safe to delete the misplaced one.
- **`Enum/GoalStatus.java` and `Enum/MilestoneStatus.java`** exist but are unused by any entity. Remnants of a previous iteration.
- **`GardenRequest.java`** is an empty file (0 lines). Unused.
- **Skeleton modules:** `Expense` and `SavingGoal` have empty stub files across all layers. Not implemented.
- **Test files:** `GoalServiceTest`, `MilestoneServiceTest`, `FocusSessionServiceTest` are entirely commented out and reference entities that no longer exist. Safe to delete.
