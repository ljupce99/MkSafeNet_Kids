# Database Schema & Migrations

Overview of the SQLite database schema, entity relationships, and data persistence strategy.

## Database Technology

- **Engine**: SQLite (file-based, no server required)
- **File**: `mksafenet.db` (created in project root on first run)
- **ORM**: Hibernate (Spring Data JPA)
- **Schema Management**: Automatic (Hibernate `ddl-auto=update`)

## Entity Relationship Diagram (ERD)

```
┌─────────────────────────────────────────────────────────────┐
│                         USERS                               │
├─────────────────────────────────────────────────────────────┤
│ id (PK)                                                     │
│ username (UNIQUE)                                           │
│ password (hashed)                                           │
│ display_name                                                │
│ role (ENUM: ADMIN, TEACHER, STUDENT)                       │
│ school_id (FK → SCHOOLS)                                   │
│ created_at                                                  │
└─────────────────────────────────────────────────────────────┘
              ↓
              │ (1:N relationship)
              ↓
┌─────────────────────────────────────────────────────────────┐
│                        SCHOOLS                              │
├────────────��────────────────────────────────────────────────┤
│ id (PK)                                                     │
│ name                                                        │
│ address                                                     │
│ city                                                        │
│ created_at                                                  │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                       SESSIONS                              │
├─────────────────────────────────────────────────────────────┤
│ id (PK)                                                     │
│ name                                                        │
│ token (UNIQUE)                                              │
│ active (BOOLEAN)                                            │
│ teacher_id (FK → USERS)                                    │
│ created_at                                                  │
└─────────────────────────────────────────────────────────────┘
              ↓
              │ (1:N relationship)
              ↓
┌─────────────────────────────────────────────────────────────┐
│                    STUDENT_SESSIONS                         │
├─────────────────────────────────────────────────────────────┤
│ id (PK)                                                     │
│ student_name                                                │
│ session_id (FK → SESSIONS)                                 │
│ score                                                       │
│ passed (BOOLEAN)                                            │
│ completed_at                                                │
└─────────────────────────────────────────────────────────────┘
              ↓
              │ (1:N relationship)
              ↓
┌─────────────────────────────────────────────────────────────┐
│                     SCENARIOS                               │
├─────────────────────────────────────────────────────────────┤
│ id (PK)                                                     │
│ title                                                       │
│ description                                                 │
│ question                                                    │
│ options (JSON: [{"key":"A","text":"..."}])                │
│ correct_answer                                              │
│ consequence                                                 │
│ created_at                                                  │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    CHAT_HISTORY                             │
├─────────────────────────────────────────────────────────────┤
│ id (PK)                                                     │
│ student_session_id (FK → STUDENT_SESSIONS)                │
│ scenario_id (FK → SCENARIOS)                              │
│ student_answer                                              │
│ is_correct (BOOLEAN)                                        │
│ points_earned                                               │
│ created_at                                                  │
└─────────────────────────────────────────────────────────────┘
```

## Core Tables

### USERS
Stores user accounts for admins, teachers, and students.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `username` | VARCHAR(255) | NOT NULL, UNIQUE | Login username |
| `password` | VARCHAR(255) | NOT NULL | Hashed password (bcrypt) |
| `display_name` | VARCHAR(255) | NOT NULL | User's full name |
| `role` | VARCHAR(50) | NOT NULL | ADMIN, TEACHER, or STUDENT |
| `school_id` | INTEGER | FOREIGN KEY (SCHOOLS) | Associated school (nullable for ADMIN) |
| `created_at` | TIMESTAMP | NOT NULL | Account creation date |

**Default Users** (created by DataSeeder):
- `admin` / `admin` (ADMIN role)
- `teacher1` / `teacher1` (TEACHER role, Test School)
- `teacher2` / `teacher2` (TEACHER role, Test School)

### SCHOOLS
Represents educational institutions.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `name` | VARCHAR(255) | NOT NULL | School name |
| `address` | VARCHAR(255) | NOT NULL | Street address |
| `city` | VARCHAR(255) | NOT NULL | City |
| `created_at` | TIMESTAMP | NOT NULL | Creation date |

**Default Schools** (created by DataSeeder):
- Test School (Springfield)

### SESSIONS
Teacher-created learning sessions. Students join using the session token.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `name` | VARCHAR(255) | NOT NULL | Session name (e.g., "Period 1") |
| `token` | VARCHAR(255) | NOT NULL, UNIQUE | Access token for students (QR code) |
| `active` | BOOLEAN | DEFAULT true | Session active/inactive status |
| `teacher_id` | INTEGER | FOREIGN KEY (USERS) | Session creator (teacher) |
| `created_at` | TIMESTAMP | NOT NULL | Creation date |

### STUDENT_SESSIONS
Tracks each student's participation in a session.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `student_name` | VARCHAR(255) | NOT NULL | Student's name |
| `session_id` | INTEGER | FOREIGN KEY (SESSIONS) | Associated session |
| `score` | INTEGER | | Final score (0-100) |
| `passed` | BOOLEAN | | Whether student passed |
| `completed_at` | TIMESTAMP | | Completion timestamp |

### SCENARIOS
Educational scenarios/questions that students answer.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `title` | VARCHAR(255) | NOT NULL | Scenario title |
| `description` | TEXT | | Scenario context/setup |
| `question` | TEXT | NOT NULL | Question for student |
| `options` | TEXT | | JSON array of answer options |
| `correct_answer` | VARCHAR(10) | NOT NULL | Correct answer key (e.g., "A") |
| `consequence` | TEXT | | Message if answer is wrong |
| `created_at` | TIMESTAMP | NOT NULL | Creation date |

**Example scenario JSON (options column):**
```json
[
  {"key":"A","text":"Block and report the stranger"},
  {"key":"B","text":"Reply and continue the conversation"},
  {"key":"C","text":"Ignore and move on"}
]
```

### CHAT_HISTORY
Records of student answers and performance.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `student_session_id` | INTEGER | FOREIGN KEY (STUDENT_SESSIONS) | Associated student session |
| `scenario_id` | INTEGER | FOREIGN KEY (SCENARIOS) | Question scenario |
| `student_answer` | VARCHAR(10) | NOT NULL | Student's answer (e.g., "A") |
| `is_correct` | BOOLEAN | NOT NULL | Correct/incorrect |
| `points_earned` | INTEGER | DEFAULT 0 | Points awarded |
| `created_at` | TIMESTAMP | NOT NULL | Answer timestamp |

## Schema Creation

Hibernate automatically creates tables on first run based on entity classes in `backend/src/main/java/com/mksafenet/model/`.

**Configuration** (`application.properties`):
```properties
spring.jpa.hibernate.ddl-auto=update
# Options: create (drop existing), create-drop (drop on shutdown), update (auto-update), validate (no changes), none
```

## Adding a New Table

### 1. Create Entity Class

**File**: `backend/src/main/java/com/mksafenet/model/NewEntity.java`

```java
@Entity
@Table(name = "new_table")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ParentEntity parent;

    @PrePersist
    public void prePersist() {
        // Set defaults before insert
    }
}
```

### 2. Create Repository

**File**: `backend/src/main/java/com/mksafenet/repository/NewEntityRepository.java`

```java
@Repository
public interface NewEntityRepository extends JpaRepository<NewEntity, Long> {
    Optional<NewEntity> findByName(String name);
    List<NewEntity> findByParentId(Long parentId);
}
```

### 3. Restart Backend

When you restart the backend, Hibernate automatically:
- Detects the new entity
- Creates the table with columns
- Establishes relationships (foreign keys)

**Verify:**
```powershell
# Check database file
ls backend/mksafenet.db

# Query with SQLite CLI
sqlite3 backend/mksafenet.db ".tables"
sqlite3 backend/mksafenet.db ".schema new_table"
```

## Querying the Database

### Using SQLite CLI

```powershell
# Open database
sqlite3 backend/mksafenet.db

# List tables
.tables

# Show schema for a table
.schema users

# Query data
SELECT * FROM users;

# Exit
.quit
```

### Using Spring Data JPA (in Code)

```java
@Service
@RequiredArgsConstructor
public class MyService {
    private final UserRepository userRepository;

    public User getUser(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public List<User> getTeachers() {
        return userRepository.findByRole(Role.TEACHER);
    }
}
```

## Data Persistence Across Restarts

The SQLite database file (`mksafenet.db`) is persistent:
- Created on first backend startup
- Data persists across restarts
- To reset database, delete `mksafenet.db` and restart backend

**Development Workflow:**
1. Start backend → `mksafenet.db` created
2. Add test data via API or frontend
3. Restart backend → data still there
4. Delete `mksafenet.db` to start fresh

## Backups

### Manual Backup

```powershell
# Copy database file
Copy-Item backend/mksafenet.db backend/mksafenet.db.backup
```

### Automated Backup (Production)

```bash
# Cron job (Linux/Mac): daily backup at 2 AM
0 2 * * * cp /app/mksafenet.db /backups/mksafenet.db.$(date +\%Y\%m\%d)
```

## Performance Considerations

### Indexes

For frequently queried columns, add indexes:

```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_school_id", columnList = "school_id")
})
public class User {
    @Column(unique = true)
    private String username;
    // ...
}
```

### Query Optimization

Use JPQL queries to fetch only needed data:

```java
@Query("SELECT new map(u.id, u.username, u.displayName) FROM User u WHERE u.role = ?1")
List<Map<String, Object>> getTeachersSummary(Role role);
```

### Lazy vs. Eager Loading

Default is LAZY (load related entities on access):

```java
@ManyToOne(fetch = FetchType.LAZY)  // Default
private School school;

@OneToMany(fetch = FetchType.EAGER)  // Load immediately
private List<Session> sessions;
```

## Migration to Production Database

### Option 1: Use Same SQLite File

Copy `mksafenet.db` to production server in application's working directory.

```powershell
# On production
Copy-Item local/mksafenet.db /app/mksafenet.db
java -jar app.jar  # Reads and updates schema as needed
```

### Option 2: Migrate to PostgreSQL/MySQL

1. Update `pom.xml` dependencies:
   ```xml
   <dependency>
       <groupId>org.postgresql</groupId>
       <artifactId>postgresql</artifactId>
   </dependency>
   ```

2. Update `application-prod.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://db-host:5432/mksafenet
   spring.datasource.username=${DB_USER}
   spring.datasource.password=${DB_PASSWORD}
   spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
   ```

3. Export SQLite data and import to PostgreSQL.

## Troubleshooting

### Database Locked Error

**Cause**: Multiple processes accessing database or previous process didn't close properly

**Solution**:
```powershell
# Kill Java process
taskkill /IM java.exe /F

# Delete database to start fresh
rm backend/mksafenet.db

# Restart backend
mvn spring-boot:run
```

### Schema Mismatch

**Cause**: Entity classes updated but database schema out of sync

**Solution**:
```powershell
# Enable detailed logging
# In application.properties: spring.jpa.show-sql=true

# Check for migration errors in console
# If issues persist, delete database and restart
rm backend/mksafenet.db
mvn spring-boot:run
```

### Verify Data Integrity

```powershell
# Connect to database and check
sqlite3 backend/mksafenet.db

# Check for orphaned records (FK violations)
PRAGMA foreign_keys=ON;
SELECT * FROM sessions WHERE teacher_id NOT IN (SELECT id FROM users);

# Check record count
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM sessions;
```

---

See [Backend Developer Guide](backend-developer-guide.md) for adding entities and [API Reference](api-reference.md) for data structure details.

