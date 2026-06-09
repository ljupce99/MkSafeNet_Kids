# Backend Developer Guide

Guide for implementing new features in the Spring Boot backend, including adding endpoints, services, DTOs, and database changes.

## Project Structure

```
backend/src/main/java/com/mksafenet/
├── controller/              # REST endpoints (@RestController)
├── service/                 # Business logic (@Service)
├── model/                   # JPA entities (@Entity)
├── repository/              # Data access (@Repository)
├── dto/                     # Request/Response objects
├── config/                  # Spring configuration
├── converter/               # Type converters for JSON
└── util/                    # Utilities (JWT, etc.)
```

## Adding a New API Endpoint

### Step 1: Create the DTO (if needed)

**File**: `backend/src/main/java/com/mksafenet/dto/MyRequestDto.java`

```java
package com.mksafenet.dto;

import lombok.Data;

@Data
public class MyRequestDto {
    private String field1;
    private Integer field2;
}
```

**File**: `backend/src/main/java/com/mksafenet/dto/MyResponseDto.java`

```java
package com.mksafenet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyResponseDto {
    private Long id;
    private String status;
    private String message;
}
```

**Notes:**
- Use Lombok annotations (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) to reduce boilerplate
- `@Data` generates getters, setters, `equals()`, `hashCode()`, `toString()`
- `@Builder` enables the builder pattern

### Step 2: Create/Update the Service

**File**: `backend/src/main/java/com/mksafenet/service/MyService.java`

```java
package com.mksafenet.service;

import com.mksafenet.dto.MyRequestDto;
import com.mksafenet.dto.MyResponseDto;
import com.mksafenet.model.MyEntity;
import com.mksafenet.repository.MyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyService {

    private final MyRepository myRepository;

    public MyResponseDto handleRequest(MyRequestDto request) {
        // Validate input
        if (request.getField1() == null || request.getField1().isBlank()) {
            throw new IllegalArgumentException("field1 is required");
        }

        // Business logic
        MyEntity entity = new MyEntity();
        entity.setField1(request.getField1());
        entity.setField2(request.getField2());

        // Persist
        MyEntity saved = myRepository.save(entity);

        // Return response
        return MyResponseDto.builder()
            .id(saved.getId())
            .status("success")
            .message("Request processed successfully")
            .build();
    }
}
```

**Notes:**
- Use `@Service` to mark as a Spring service bean
- Use `@RequiredArgsConstructor` (from Lombok) to inject dependencies via constructor
- Throw `IllegalArgumentException` for validation errors; controller catches and returns 400
- Use `@Transactional` if modifying multiple entities (for automatic rollback on error)

### Step 3: Create/Update the Controller

**File**: `backend/src/main/java/com/mksafenet/controller/MyController.java`

```java
package com.mksafenet.controller;

import com.mksafenet.dto.MyRequestDto;
import com.mksafenet.dto.MyResponseDto;
import com.mksafenet.service.MyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
public class MyController {

    private final MyService myService;

    @PostMapping("/endpoint")
    @PreAuthorize("hasRole('TEACHER')") // Only TEACHER role
    public ResponseEntity<?> myEndpoint(@RequestBody MyRequestDto request) {
        try {
            MyResponseDto response = myService.handleRequest(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            // Fetch and return
            return ResponseEntity.ok(myService.getById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
```

**Notes:**
- Use `@RestController` for REST endpoints
- Use `@RequestMapping("/api/...")` to set base path
- Use `@PostMapping`, `@GetMapping`, `@PutMapping`, `@DeleteMapping` for HTTP methods
- Use `@PreAuthorize("hasRole('ROLE_NAME')")` to restrict by role (ADMIN, TEACHER, STUDENT)
- Use `@PathVariable` for path parameters
- Use `@RequestBody` for JSON request bodies
- Return `ResponseEntity` with appropriate status codes
- Catch exceptions and return meaningful error responses

## Adding a New Entity (Database Table)

### Step 1: Create the JPA Entity

**File**: `backend/src/main/java/com/mksafenet/model/MyEntity.java`

```java
package com.mksafenet.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "my_entity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String field1;

    @Column(nullable = true)
    private Integer field2;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
```

**Notes:**
- Use `@Entity` to mark as JPA entity
- Use `@Table(name = "...")` to specify table name
- Use `@Id` and `@GeneratedValue` for auto-increment primary key
- Use `@Column` annotations to define column properties
- Use `@ManyToOne`, `@OneToMany`, `@OneToOne` for relationships
- Use `@PrePersist` to set default values before insert

### Step 2: Create the Repository

**File**: `backend/src/main/java/com/mksafenet/repository/MyRepository.java`

```java
package com.mksafenet.repository;

import com.mksafenet.model.MyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MyRepository extends JpaRepository<MyEntity, Long> {

    // Simple query methods (auto-generated by Spring Data JPA)
    Optional<MyEntity> findByField1(String field1);
    List<MyEntity> findByField2(Integer field2);
    List<MyEntity> findByUserId(Long userId);

    // Custom JPQL query
    @Query("SELECT m FROM MyEntity m WHERE m.field1 = :field1 AND m.field2 > :field2")
    List<MyEntity> findByField1AndField2(
        @Param("field1") String field1,
        @Param("field2") Integer field2
    );
}
```

**Notes:**
- Extend `JpaRepository<Entity, PrimaryKeyType>`
- Spring Data JPA auto-generates simple query methods based on method names
- Use `@Query` for custom JPQL queries
- Use `@Param` to bind query parameters

## Database Schema Changes

### Adding a Column

1. **Update the Entity:**
   ```java
   @Column(nullable = false)
   private String newField;
   ```

2. **Restart Backend:**
   - Spring Boot auto-updates schema (via Hibernate `ddl-auto=update` in `application.properties`)
   - No manual SQL migration needed for development

3. **For Production:**
   - Use a migration tool like Flyway or Liquibase
   - Manually verify schema changes

### Adding a Relationship

```java
@ManyToOne
@JoinColumn(name = "parent_id")
private ParentEntity parent;

@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
private List<ChildEntity> children;
```

## Using Transactions

```java
@Service
@RequiredArgsConstructor
public class MyService {
    private final MyRepository myRepository;

    @Transactional  // Wraps method in a transaction
    public void updateMultiple() {
        // All database operations here
        // Auto-rollback if exception occurs
    }
}
```

## Testing

### Unit Test Example

**File**: `backend/src/test/java/com/mksafenet/service/MyServiceTest.java`

```java
package com.mksafenet.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MyServiceTest {

    private MyService myService;

    @Mock
    private MyRepository myRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        myService = new MyService(myRepository);
    }

    @Test
    public void testHandleRequest_Success() {
        // Arrange
        MyRequestDto request = new MyRequestDto();
        request.setField1("test");

        // Act
        MyResponseDto response = myService.handleRequest(request);

        // Assert
        assertNotNull(response);
        assertEquals("success", response.getStatus());
    }

    @Test
    public void testHandleRequest_ValidationError() {
        // Arrange
        MyRequestDto request = new MyRequestDto();
        request.setField1(null);  // Invalid

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            myService.handleRequest(request);
        });
    }
}
```

**Run Tests:**
```powershell
cd backend
mvn -q test
```

## Best Practices

1. **Separation of Concerns:**
   - Controllers handle HTTP requests/responses
   - Services handle business logic
   - Repositories handle data access
   - DTOs for API contracts

2. **Error Handling:**
   - Use exceptions for errors
   - Controllers catch and convert to HTTP responses
   - Avoid returning null; throw exceptions instead

3. **Validation:**
   - Validate input in services
   - Throw `IllegalArgumentException` for validation errors
   - Use `@Valid` annotation on controller parameters for bean validation

4. **Security:**
   - Use `@PreAuthorize` to restrict endpoints by role
   - Don't expose sensitive data in responses
   - Hash passwords (use Spring Security's `PasswordEncoder`)
   - Validate JWT tokens on protected endpoints

5. **Naming Conventions:**
   - Classes: PascalCase (e.g., `MyService`, `MyEntity`)
   - Methods/Fields: camelCase (e.g., `myMethod()`, `myField`)
   - Database columns: snake_case (e.g., `my_column`)
   - Endpoints: kebab-case (e.g., `/my-endpoint`)

6. **Documentation:**
   - Add Javadoc to public methods
   - Document complex business logic
   - Update API reference when adding endpoints

## Common Annotations

| Annotation | Usage |
|-----------|-------|
| `@Service` | Mark class as service bean |
| `@Repository` | Mark class as repository |
| `@RestController` | Mark class as REST controller |
| `@RequestMapping` | Base URL for controller |
| `@PostMapping`, `@GetMapping` | HTTP method mapping |
| `@PathVariable` | Extract path parameter |
| `@RequestBody` | Bind JSON request to object |
| `@PreAuthorize` | Role-based access control |
| `@Transactional` | Manage database transactions |
| `@Entity` | Mark class as JPA entity |
| `@Table` | Specify table name |
| `@Column` | Specify column properties |
| `@ManyToOne`, `@OneToMany` | Entity relationships |
| `@Data`, `@Builder` | Lombok annotations for boilerplate |

## Debugging

### Enable SQL Logging

In `application.properties`:
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### View Generated SQL

Check backend console output when `show-sql=true`.

### Use IDE Debugger

Set breakpoints in IntelliJ/VS Code and step through code.

---

See [API Reference](api-reference.md) for endpoint documentation and [Architecture Overview](architecture.md) for system design details.

