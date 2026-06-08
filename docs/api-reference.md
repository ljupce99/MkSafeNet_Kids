# API Reference

Complete documentation of all REST API endpoints, request/response formats, and example curl requests.

## Base URL

```
http://localhost:8080/api
```

**Production**: Replace with your production domain (e.g., `https://api.mksafenet.example.com/api`)

## Authentication

Most endpoints require a **JWT token** in the request header:

```
Authorization: Bearer <token>
```

Tokens are obtained via the `/auth/login` endpoint. Tokens expire after 24 hours (configurable in `application.properties`).

---

## 1. Authentication Endpoints

### Base Path: `/api/auth`

#### POST `/api/auth/login`
Authenticates a user and returns a JWT token.

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYwMjYwMDAwMCwiZXhwIjoxNjAyNjg2NDAwfQ.abcdefg",
  "role": "ADMIN",
  "username": "admin",
  "displayName": "Administrator",
  "schoolId": null,
  "schoolName": null
}
```

**Error (401 Unauthorized):**
```json
{
  "error": "Invalid username or password"
}
```

**Example curl:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }'
```

---

## 2. Chat Endpoints

### Base Path: `/api/chat`

Chat endpoints manage the interactive scenario flow for students.

#### GET `/api/chat/session/{token}`
Validates a teacher-created session token and returns session details.

**Path Parameters:**
- `token` (string): Session token provided by teacher (usually via QR code)

**Response (200 OK):**
```json
{
  "valid": true,
  "sessionName": "Period 1 Safety Class",
  "schoolName": "Test School"
}
```

**Error (404 Not Found):**
```json
{
  "error": "Session not found or inactive"
}
```

**Example curl:**
```bash
curl -X GET http://localhost:8080/api/chat/session/abc123xyz \
  -H "Content-Type: application/json"
```

#### POST `/api/chat/start`
Initiates a chat session with a student and returns the first scenario.

**Request Body:**
```json
{
  "sessionToken": "abc123xyz",
  "studentName": "John Doe"
}
```

**Response (200 OK):**
```json
{
  "studentId": "john-doe-001",
  "phase": "INTRO",
  "messages": [
    {
      "type": "bot",
      "text": "Welcome to the Online Safety Challenge!",
      "delayMs": 500,
      "icon": "👋"
    },
    {
      "type": "bot",
      "text": "Let's see how you handle some real online situations...",
      "delayMs": 1000,
      "icon": null
    }
  ],
  "scenarioId": null,
  "question": null,
  "options": null,
  "correct": null,
  "consequenceType": null,
  "consequenceMessages": null,
  "score": null,
  "grade": null,
  "passed": null,
  "correctCount": null,
  "totalScenarios": null,
  "badges": null,
  "scenarioResults": null
}
```

**Error (400 Bad Request):**
```json
{
  "error": "Invalid session token or session inactive"
}
```

**Example curl:**
```bash
curl -X POST http://localhost:8080/api/chat/start \
  -H "Content-Type: application/json" \
  -d '{
    "sessionToken": "abc123xyz",
    "studentName": "John Doe"
  }'
```

#### POST `/api/chat/respond`
Student submits an answer to a scenario question. Backend evaluates the answer and returns consequence or next scenario.

**Request Body:**
```json
{
  "studentId": "john-doe-001",
  "answer": "A"
}
```

**Response (200 OK) - Correct Answer:**
```json
{
  "studentId": "john-doe-001",
  "phase": "SCENARIO",
  "messages": [
    {
      "type": "success",
      "text": "Great choice! You made the safe decision.",
      "delayMs": 500,
      "icon": "✅"
    }
  ],
  "scenarioId": 2,
  "question": "A friend asks for your password. What do you do?",
  "options": [
    {"key": "A", "text": "Politely refuse and explain why sharing is unsafe"},
    {"key": "B", "text": "Share it because they're a trusted friend"},
    {"key": "C", "text": "Ignore the message without responding"}
  ],
  "correct": true,
  "pointsEarned": 10,
  "consequenceType": null,
  "consequenceMessages": null,
  "correctCount": 1,
  "totalScenarios": 3
}
```

**Response (200 OK) - Incorrect Answer with Consequence:**
```json
{
  "studentId": "john-doe-001",
  "phase": "CONSEQUENCE",
  "messages": [
    {
      "type": "bot",
      "text": "Hmm, let's think about this...",
      "delayMs": 500,
      "icon": "🤔"
    }
  ],
  "scenarioId": 1,
  "question": null,
  "options": null,
  "correct": false,
  "pointsEarned": 0,
  "consequenceType": "ACCOUNT_HACKED",
  "consequenceMessages": [
    {
      "type": "consequence",
      "text": "Your account was hacked and all your personal information was stolen.",
      "delayMs": 800,
      "icon": "🔓"
    },
    {
      "type": "consequence",
      "text": "Cybercriminals accessed your photos, contacts, and messages.",
      "delayMs": 1000,
      "icon": "⚠️"
    }
  ]
}
```

**Response (200 OK) - Quiz Complete:**
```json
{
  "studentId": "john-doe-001",
  "phase": "COMPLETE",
  "messages": [
    {
      "type": "success",
      "text": "Congratulations! You completed the Online Safety Challenge!",
      "delayMs": 500,
      "icon": "🎉"
    }
  ],
  "score": 70,
  "grade": "A",
  "passed": true,
  "correctCount": 7,
  "totalScenarios": 10,
  "badges": ["Safety Conscious", "Quick Learner"],
  "scenarioResults": [
    {
      "scenarioId": 1,
      "scenarioTitle": "Stranger Contact",
      "selectedAnswer": "A",
      "correct": true,
      "pointsEarned": 10
    },
    {
      "scenarioId": 2,
      "scenarioTitle": "Password Sharing",
      "selectedAnswer": "B",
      "correct": false,
      "pointsEarned": 0
    }
  ]
}
```

**Error (400 Bad Request):**
```json
{
  "error": "Invalid student ID or answer format"
}
```

**Example curl:**
```bash
curl -X POST http://localhost:8080/api/chat/respond \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "studentId": "john-doe-001",
    "answer": "A"
  }'
```

---

## 3. Certificate Endpoints

### Base Path: `/api/certificates`

#### GET `/api/certificates/download?name=<name>`
Generates and downloads a PDF certificate for a student.

**Query Parameters:**
- `name` (string): Student's full name

**Response (200 OK):**
- Content-Type: `application/pdf`
- File download: `certificate-<name>.pdf`

**Example curl:**
```bash
curl -X GET "http://localhost:8080/api/certificates/download?name=John%20Doe" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -o certificate.pdf
```

---

## 4. Teacher Endpoints

### Base Path: `/api/teacher`

**Authentication Required**: TEACHER role

#### POST `/api/teacher/sessions`
Creates a new learning session. Students join via session token.

**Request Body:**
```json
{
  "name": "Period 1 - Safety Class"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Period 1 - Safety Class",
  "token": "abc123xyz789",
  "active": true,
  "createdAt": "2024-01-15T10:30:00Z",
  "studentCount": 0,
  "teacherName": "Ms. Smith"
}
```

**Example curl:**
```bash
curl -X POST http://localhost:8080/api/teacher/sessions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "Period 1 - Safety Class"
  }'
```

#### GET `/api/teacher/sessions`
Retrieves all sessions for the authenticated teacher.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Period 1 - Safety Class",
    "token": "abc123xyz789",
    "active": true,
    "createdAt": "2024-01-15T10:30:00Z",
    "studentCount": 5,
    "teacherName": "Ms. Smith"
  },
  {
    "id": 2,
    "name": "Period 2 - Advanced Topics",
    "token": "def456abc123",
    "active": false,
    "createdAt": "2024-01-14T14:00:00Z",
    "studentCount": 0,
    "teacherName": "Ms. Smith"
  }
]
```

**Example curl:**
```bash
curl -X GET http://localhost:8080/api/teacher/sessions \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### GET `/api/teacher/sessions/{id}`
Retrieves details of a specific session including student results.

**Path Parameters:**
- `id` (number): Session ID

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Period 1 - Safety Class",
  "token": "abc123xyz789",
  "active": true,
  "createdAt": "2024-01-15T10:30:00Z",
  "teacherName": "Ms. Smith",
  "students": [
    {
      "studentName": "John Doe",
      "score": 85,
      "passed": true,
      "completedAt": "2024-01-15T11:15:00Z"
    },
    {
      "studentName": "Jane Smith",
      "score": 70,
      "passed": true,
      "completedAt": "2024-01-15T11:20:00Z"
    }
  ]
}
```

**Example curl:**
```bash
curl -X GET http://localhost:8080/api/teacher/sessions/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### GET `/api/teacher/sessions/{id}/qr`
Generates a QR code image for the session token (useful for students to scan and join).

**Path Parameters:**
- `id` (number): Session ID

**Response (200 OK):**
- Content-Type: `image/png`
- QR code image PNG

**Example curl:**
```bash
curl -X GET http://localhost:8080/api/teacher/sessions/1/qr \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -o session-qr.png
```

#### PUT `/api/teacher/sessions/{id}/toggle`
Activates or deactivates a session.

**Path Parameters:**
- `id` (number): Session ID

**Request Body:**
```json
{
  "active": false
}
```

**Response (200 OK):**
```json
{
  "success": true
}
```

**Example curl:**
```bash
curl -X PUT http://localhost:8080/api/teacher/sessions/1/toggle \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "active": false
  }'
```

---

## 5. Scenario Endpoints (Admin Only)

### Base Path: `/api/admin/scenarios`

**Authentication Required**: ADMIN role

#### GET `/api/admin/scenarios`
Retrieves all scenarios.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "title": "Stranger Contact",
    "description": "Someone you don't know sends you a message online.",
    "question": "What should you do?",
    "options": [
      {"key": "A", "text": "Block and report the stranger"},
      {"key": "B", "text": "Reply and continue the conversation"}
    ],
    "correctAnswer": "A"
  }
]
```

**Example curl:**
```bash
curl -X GET http://localhost:8080/api/admin/scenarios \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### GET `/api/admin/scenarios/{id}`
Retrieves a specific scenario.

**Path Parameters:**
- `id` (number): Scenario ID

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "Stranger Contact",
  "description": "Someone you don't know sends you a message online.",
  "question": "What should you do?",
  "options": [
    {"key": "A", "text": "Block and report the stranger"},
    {"key": "B", "text": "Reply and continue the conversation"}
  ],
  "correctAnswer": "A",
  "consequence": "Meeting strangers online can be dangerous. They may pretend to be someone they're not."
}
```

**Example curl:**
```bash
curl -X GET http://localhost:8080/api/admin/scenarios/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### POST `/api/admin/scenarios`
Creates a new scenario.

**Request Body:**
```json
{
  "title": "New Scenario Title",
  "description": "Scenario description",
  "question": "What should you do?",
  "options": [
    {"key": "A", "text": "Option A"},
    {"key": "B", "text": "Option B"}
  ],
  "correctAnswer": "A",
  "consequence": "Consequence message if wrong"
}
```

**Response (200 OK):**
```json
{
  "id": 5,
  "title": "New Scenario Title",
  "description": "Scenario description",
  "question": "What should you do?",
  "options": [
    {"key": "A", "text": "Option A"},
    {"key": "B", "text": "Option B"}
  ],
  "correctAnswer": "A"
}
```

**Example curl:**
```bash
curl -X POST http://localhost:8080/api/admin/scenarios \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "title": "New Scenario",
    "description": "Description",
    "question": "Question?",
    "options": [{"key":"A","text":"Option A"}],
    "correctAnswer": "A"
  }'
```

#### PUT `/api/admin/scenarios/{id}`
Updates an existing scenario.

**Path Parameters:**
- `id` (number): Scenario ID

**Request Body:** (same as POST)

**Response (200 OK):** Updated scenario object

**Example curl:**
```bash
curl -X PUT http://localhost:8080/api/admin/scenarios/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "title": "Updated Title",
    "description": "Updated description",
    "question": "Updated question?",
    "options": [{"key":"A","text":"Updated Option"}],
    "correctAnswer": "A"
  }'
```

#### DELETE `/api/admin/scenarios/{id}`
Deletes a scenario.

**Path Parameters:**
- `id` (number): Scenario ID

**Response (200 OK):**
```json
{
  "message": "Scenario deleted successfully"
}
```

**Example curl:**
```bash
curl -X DELETE http://localhost:8080/api/admin/scenarios/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

## 6. Admin Endpoints

### Base Path: `/api/admin`

**Authentication Required**: ADMIN role

#### GET `/api/admin/schools`
Retrieves all registered schools.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Test School",
    "address": "123 Main St",
    "city": "Springfield",
    "createdAt": "2024-01-01T00:00:00Z"
  }
]
```

**Example curl:**
```bash
curl -X GET http://localhost:8080/api/admin/schools \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### POST `/api/admin/schools`
Creates a new school.

**Request Body:**
```json
{
  "name": "New School Name",
  "address": "456 Oak Ave",
  "city": "Shelbyville"
}
```

**Response (200 OK):**
```json
{
  "id": 2,
  "name": "New School Name",
  "address": "456 Oak Ave",
  "city": "Shelbyville"
}
```

**Example curl:**
```bash
curl -X POST http://localhost:8080/api/admin/schools \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "New School",
    "address": "456 Oak Ave",
    "city": "Shelbyville"
  }'
```

#### GET `/api/admin/schools/{id}`
Retrieves details of a specific school.

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Test School",
  "address": "123 Main St",
  "city": "Springfield",
  "teachers": [
    {"id": 1, "username": "teacher1", "displayName": "Mr. Johnson"},
    {"id": 2, "username": "teacher2", "displayName": "Ms. Smith"}
  ]
}
```

**Example curl:**
```bash
curl -X GET http://localhost:8080/api/admin/schools/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### POST `/api/admin/teachers`
Creates a new teacher account.

**Request Body:**
```json
{
  "username": "newteacher",
  "password": "securepass123",
  "displayName": "Mr. New Teacher",
  "schoolId": 1
}
```

**Response (200 OK):**
```json
{
  "id": 3,
  "username": "newteacher",
  "displayName": "Mr. New Teacher",
  "schoolId": 1
}
```

**Example curl:**
```bash
curl -X POST http://localhost:8080/api/admin/teachers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "username": "newteacher",
    "password": "securepass123",
    "displayName": "Mr. New Teacher",
    "schoolId": 1
  }'
```

#### GET `/api/admin/teachers`
Retrieves all teachers.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "username": "teacher1",
    "displayName": "Mr. Johnson",
    "schoolId": 1,
    "schoolName": "Test School"
  },
  {
    "id": 2,
    "username": "teacher2",
    "displayName": "Ms. Smith",
    "schoolId": 1,
    "schoolName": "Test School"
  }
]
```

**Example curl:**
```bash
curl -X GET http://localhost:8080/api/admin/teachers \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### GET `/api/admin/stats`
Retrieves global statistics (total schools, teachers, sessions, etc.).

**Response (200 OK):**
```json
{
  "totalSchools": 1,
  "totalTeachers": 2,
  "totalStudentSessions": 15,
  "averageScore": 78.5,
  "totalCertificatesIssued": 10
}
```

**Example curl:**
```bash
curl -X GET http://localhost:8080/api/admin/stats \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

## Data Transfer Objects (DTOs)

### DTOs Reference

| DTO | Purpose | Fields |
|-----|---------|--------|
| `LoginRequest` | Login credentials | `username`, `password` |
| `LoginResponse` | Authentication response | `token`, `role`, `username`, `displayName`, `schoolId`, `schoolName` |
| `ChatStartRequest` | Start chat session | `sessionToken`, `studentName` |
| `ChatRespondRequest` | Submit answer | `studentId`, `answer` |
| `ChatResponseDto` | Chat/scenario response | `studentId`, `phase`, `messages`, `scenarioId`, `question`, `options`, `correct`, `score`, `grade`, etc. |
| `ChatMessageDto` | Single chat message | `type`, `text`, `delayMs`, `icon` |
| `ScenarioOptionDto` | Scenario answer option | `key`, `text` |
| `ScenarioResultDto` | Scenario answer result | `scenarioId`, `scenarioTitle`, `selectedAnswer`, `correct`, `pointsEarned` |

---

## Common Response Codes

| Code | Description |
|------|-------------|
| `200` | Success |
| `400` | Bad Request (validation error) |
| `401` | Unauthorized (invalid/missing token) |
| `403` | Forbidden (insufficient permissions) |
| `404` | Not Found (resource doesn't exist) |
| `500` | Internal Server Error |

## Error Handling

All errors are returned as JSON with an `error` field:

```json
{
  "error": "Descriptive error message"
}
```

Example:
```json
{
  "error": "Invalid username or password"
}
```

---

## Testing API Endpoints

Use any HTTP client (curl, Postman, VS Code REST Client) to test endpoints. See examples above for curl syntax.

### Postman Collection
Create a Postman collection with these endpoints for easy testing:
1. Set variable `{{baseUrl}}` to `http://localhost:8080/api`
2. Set variable `{{token}}` to the token from login response
3. Use `{{baseUrl}}/endpoint` and add header `Authorization: Bearer {{token}}`

---

## Next Steps

- Review the [Backend Developer Guide](backend-developer-guide.md) to add new endpoints
- Check [Frontend Developer Guide](frontend-developer-guide.md) to integrate API calls
- See [Security & Authentication](security.md) for detailed JWT flow

