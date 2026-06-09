# System Architecture

## Overview

**MkSafeNet_Kids** follows a **client-server architecture** with a clear separation of concerns:

- **Backend**: Spring Boot REST API serving business logic and data persistence
- **Frontend**: Vue 3 Single Page Application (SPA) providing the user interface
- **Database**: SQLite for persistent data storage
- **Authentication**: JWT-based token authentication with role-based access control

```
┌─────────────────────────────────────────────────────────────┐
│                      Client Layer (Vue 3 SPA)               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  LoginView   │  │ TeacherDash  │  │  AdminDash   │ ...  │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         ↓                 ↓                    ↓              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Vue Router (Client-side routing)                     │  │
│  │  Pinia (State Management - Auth, Session)             │  │
│  │  Axios (HTTP Client with Interceptors)                │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                  HTTP/REST │ (JSON + JWT Token)
                            │
┌─────────────────────────────────────────────────────────────┐
│                   API Layer (Spring Boot REST)              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ AuthController│ │ChatController│  │TeacherContrl │ ...  │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         ↓                 ↓                    ↓              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Security Layer (JWT Filter, Role-based Access)      │  │
│  └───────────────────────────────────────────────────────┘  │
│         ↓                 ↓                    ↓              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ AuthService  │  │ ChatService  │  │TeacherService│ ...  │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         ↓                 ↓                    ↓              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Data Access Layer (JPA Repositories)                │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                   SQL      │
                            │
┌─────────────────────────────────────────────────────────────┐
│              SQLite Database (mksafenet.db)                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  users       │  │  sessions    │  │  scenarios   │ ...  │
│  │  schools     │  │  chat_msgs   │  │  chat_hist   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

## Backend Architecture

### Controller Layer
**Location**: `backend/src/main/java/com/mksafenet/controller/`

REST endpoints organized by feature:

| Controller | Base Path | Purpose | Roles |
|------------|-----------|---------|-------|
| `AuthController` | `/api/auth` | User login/authentication | PUBLIC |
| `ChatController` | `/api/chat` | Start/respond to chat scenarios | STUDENT |
| `CertificateController` | `/api/certificates` | Generate/download certificates | STUDENT |
| `TeacherController` | `/api/teacher` | Session management | TEACHER |
| `ScenarioController` | `/api/admin/scenarios` | CRUD scenarios | ADMIN |
| `AdminController` | `/api/admin` | School/teacher/stats management | ADMIN |

### Service Layer
**Location**: `backend/src/main/java/com/mksafenet/service/`

Business logic encapsulation:

- `AuthService` – Login, authentication, token generation
- `ChatService` – Chat session management, scenario presentation, answer processing
- `CertificateService` – PDF generation for completion certificates
- `TeacherService` – Session lifecycle, student tracking
- `ScenarioService` – Scenario CRUD and retrieval
- `AdminService` – School, teacher, and stats management

### Model/Entity Layer
**Location**: `backend/src/main/java/com/mksafenet/model/`

JPA entities representing core domain objects:

- `User` – Teachers, students, admins
- `School` – Educational institutions
- `Scenario` – Safety scenarios with questions/options
- `Session` – Teacher-created student learning sessions
- `ChatMessage` – Historical chat messages
- And related entities for tracking answers, results, etc.

### DTO Layer
**Location**: `backend/src/main/java/com/mksafenet/dto/`

Data Transfer Objects for API communication:

- `LoginRequest`, `LoginResponse` – Authentication
- `ChatStartRequest`, `ChatRespondRequest`, `ChatResponseDto`, `ChatMessageDto` – Chat flow
- `ScenarioOptionDto`, `ScenarioResultDto` – Scenario data
- See [API Reference](api-reference.md) for complete DTO specs

### Repository Layer
**Location**: `backend/src/main/java/com/mksafenet/repository/`

JPA repositories for database queries:

- Spring Data JPA repositories extend `CrudRepository` or `JpaRepository`
- Enables simple CRUD operations and custom query methods
- Example: `UserRepository.findByUsername()`

### Security & Configuration
**Location**: `backend/src/main/java/com/mksafenet/config/`

Key configurations:

- `SecurityConfig` – Spring Security filter chain, role-based access control, CORS
- `JwtAuthFilter` – JWT token validation on incoming requests
- `DataSeeder` – Initializes sample data for development/testing
- JWT secret and expiration configured in `application.properties`

### Utilities
**Location**: `backend/src/main/java/com/mksafenet/util/`

Helper classes:

- `JwtUtil` – JWT token generation, validation, claims extraction
- `ChatMessageListConverter`, `ScenarioOptionListConverter` – JSON serialization for complex types

## Frontend Architecture

### Components & Views
**Location**: `frontend/src/views/` and `frontend/src/components/`

Organized by feature/user role:

- `LoginView.vue` – User authentication
- `ChatView.vue` – Student chat interface
- `admin/AdminDashboard.vue` – Admin panel
- `teacher/TeacherDashboard.vue` – Teacher panel
- `ConsequenceModal.vue` – Consequence display component

### State Management (Pinia)
**Location**: `frontend/src/stores/`

- `auth.js` – Stores user token, role, display name, school info; exposes `login()` and `logout()`
- Can be extended with session stores, scenario data, etc.

### Routing
**Location**: `frontend/src/router/index.js`

Vue Router configuration:
- Defines routes for each view
- Guards routes based on authentication and user role
- Example: `/admin/*` routes require ADMIN role

### API Client
**Location**: `frontend/src/api/index.js`

Axios instance with:
- Base URL pointed to backend `/api`
- Request interceptor: attaches JWT token to `Authorization` header
- Response interceptor: redirects to login on 401 (unauthorized)

### Entry Point
- `frontend/src/main.js` – Initializes Vue app, Pinia store, Router
- `frontend/src/App.vue` – Root component
- `frontend/index.html` – HTML template

## Data Flow Examples

### Login Flow
```
User enters credentials
         ↓
LoginView.vue submits to /api/auth/login
         ↓
AuthController.login() calls AuthService.login()
         ↓
AuthService validates credentials, generates JWT token
         ↓
Token + user info returned to frontend
         ↓
Pinia auth store saves token to localStorage
         ↓
Axios interceptor includes token in future requests
         ↓
User redirected to dashboard
```

### Chat Scenario Flow
```
Student clicks "Start Chat" (Teacher provides QR token)
         ↓
ChatView.vue submits POST /api/chat/start with sessionToken, studentName
         ↓
ChatController.startChat() validates session, loads first scenario
         ↓
ChatService presents intro and first scenario question
         ↓
ChatResponseDto returned with messages and options
         ↓
Frontend displays messages and renders answer buttons
         ↓
Student clicks answer (e.g., "A")
         ↓
ChatView.vue submits POST /api/chat/respond with answer
         ↓
ChatService evaluates answer, determines consequence
         ↓
If correct: presents success message; if incorrect: consequence scenario
         ↓
ChatResponseDto returned with consequence messages + next scenario
         ↓
Flow continues until all scenarios completed → final score/certificate
```

## Technology Stack Details

### Backend
- **Spring Boot 3.x** – REST framework
- **Spring Security** – Authentication & authorization
- **Spring Data JPA** – ORM and data access
- **Hibernate** – JPA implementation
- **SQLite + SQLite JDBC** – Lightweight embedded database
- **Lombok** – Reduces boilerplate (annotations for getters, setters, builders)
- **iText** – PDF generation for certificates
- **Maven** – Build tool

### Frontend
- **Vue 3** – Reactive UI framework (Composition API)
- **Vite** – Fast development and build tool
- **Vue Router 4** – Client-side routing
- **Pinia** – State management (reactive store)
- **Axios** – HTTP client
- **Node.js / npm** – Package management

### Database
- **SQLite** – File-based SQL database, no server required, ideal for development and small deployments
- **Hibernate Dialect**: `org.hibernate.community.dialect.SQLiteDialect`
- **DDL Auto**: `update` (automatically creates/updates schema from entities)

## Deployment Considerations

- **Backend**: Can be deployed as a standalone JAR (`java -jar app.jar`) on any Java-capable server
- **Frontend**: Built as static files (`npm run build` → `dist/`) served by a web server or backend
- **Database**: SQLite file `mksafenet.db` must be persisted and accessible to backend process
- **Environment Variables**: JWT secret, frontend URL, database path configured via `application.properties` or environment overrides
- See [Deployment Guide](deployment.md) for production setup options

## Security Architecture

- **JWT Tokens**: Stateless, signed tokens issued on login; expire after 24 hours (configurable)
- **CORS**: Configured to allow frontend requests from `http://localhost:5173` (dev) or production URL
- **Role-based Access**: `@PreAuthorize("hasRole('ADMIN')")` annotations on sensitive endpoints
- **Password Hashing**: Should use bcrypt or similar (verify in `AuthService`)
- **HTTPS**: Required in production to protect token transmission
- See [Security & Authentication](security.md) for detailed security flows

## Extensibility

The architecture supports adding new features by:

1. **New Endpoint**: Create a controller method, add DTO classes
2. **New Business Logic**: Extend service classes or create new services
3. **New Entities**: Add JPA entity and repository
4. **New Views/Components**: Add Vue components and router entries
5. **State**: Extend Pinia stores for new state if needed

See [Backend Developer Guide](backend-developer-guide.md) and [Frontend Developer Guide](frontend-developer-guide.md) for step-by-step examples.

