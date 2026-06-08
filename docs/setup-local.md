# Local Development Setup

This guide walks you through setting up a complete local development environment for MkSafeNet_Kids on Windows.

## Prerequisites

Before you begin, ensure you have the following installed:

### Java & Maven
- **Java 17 or higher** ([Download](https://adoptopenjdk.net/))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))

Verify installation:
```powershell
java -version
mvn -version
```

### Node.js & npm
- **Node.js 18 or higher** ([Download](https://nodejs.org/))
- npm comes bundled with Node.js

Verify installation:
```powershell
node --version
npm --version
```

### Database (Included)
- SQLite is embedded; no separate installation needed
- Database file `mksafenet.db` is auto-created on first backend run

### Git (Recommended)
- For cloning and version control ([Download](https://git-scm.com/))

## Step-by-Step Setup

### 1. Clone the Repository

```powershell
git clone https://github.com/<your-repo>/MkSafeNet_Kids.git
cd MkSafeNet_Kids
```

Or if already downloaded, navigate to the project directory:
```powershell
cd C:\Users\Ljupc\Documents\CODES\Java_Projects\MkSafeNet_Kids
```

### 2. Backend Setup & Configuration

#### Navigate to Backend Directory
```powershell
cd backend
```

#### Configure Application Properties
Edit `src/main/resources/application.properties` to suit your environment:

```properties
# Database (SQLite)
spring.datasource.url=jdbc:sqlite:mksafenet.db
spring.datasource.driver-class-name=org.sqlite.JDBC

# Hibernate (Auto-create/update schema)
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# JWT Configuration
jwt.secret=mksafenet-super-secret-jwt-key-change-in-production-32chars
jwt.expiration=86400000

# Frontend URL (for CORS)
app.frontend.url=http://localhost:5173

# Server Port
server.port=8080
```

**Key Configuration Notes:**
- `jwt.secret`: Change this to a strong random value in production
- `jwt.expiration`: Default is 24 hours (86400000 ms); adjust as needed
- `app.frontend.url`: Must match frontend development URL for CORS
- `server.port`: Backend port; change if 8080 is already in use

#### Build Backend
```powershell
mvn clean install
```

This downloads dependencies and builds the project. First run may take a few minutes.

#### Run Backend
```powershell
mvn spring-boot:run
```

**Expected Output:**
```
...
 Initializing H2 database...
 Started MksafenetApplication in 5.123s
 Tomcat started on port(s): 8080 (http)
```

The backend is now running on `http://localhost:8080`. The database file `mksafenet.db` is created in the `backend/` directory.

**Verify Backend:**
```powershell
# In a new PowerShell window, test the login endpoint
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"username\":\"admin\",\"password\":\"admin\"}'
```

Expected response:
```json
{"token":"eyJhbGciOiJIUzI1NiJ9...","role":"ADMIN","username":"admin","displayName":"Admin","schoolId":null,"schoolName":null}
```

### 3. Frontend Setup & Configuration

#### Navigate to Frontend Directory
```powershell
cd ..\frontend
```

#### Create Environment File (Optional)
Create `frontend/.env` for environment-specific variables (optional, defaults are in `api/index.js`):

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=MkSafeNet_Kids
```

#### Install Dependencies
```powershell
npm install
```

This installs all npm packages listed in `package.json`.

#### Run Development Server
```powershell
npm run dev
```

**Expected Output:**
```
  VITE v5.0.12  ready in 123 ms

  ➜  Local:   http://localhost:5173/
  ➜  press h to show help
```

The frontend is now running on `http://localhost:5173`.

**Verify Frontend:**
Open your browser and navigate to `http://localhost:5173`. You should see the login page.

### 4. Using Startup Scripts (Windows Batch)

Alternatively, you can use the provided startup scripts for convenience:

**Start Backend:**
```powershell
.\start-backend.bat
```

**Start Frontend (in a new terminal):**
```powershell
.\start-frontend.bat
```

These scripts automate the navigation and startup commands.

## Accessing the Application

### Login

1. Open `http://localhost:5173` in your browser
2. Login with default credentials:
   - **Username**: `admin`
   - **Password**: `admin`
3. You're now authenticated and can navigate the dashboard

### Default Users (Created by DataSeeder)
The backend initializes sample data on first run via `DataSeeder` class:

| Username | Password | Role | School | Notes |
|----------|----------|------|--------|-------|
| `admin` | `admin` | ADMIN | N/A | System administrator |
| `teacher1` | `teacher1` | TEACHER | Test School | Can create sessions |
| `teacher2` | `teacher2` | TEACHER | Test School | Can create sessions |

Feel free to create additional users via admin panel or modify `DataSeeder.java` to add more test data.

## Development Workflow

### Making Backend Changes

1. Edit Java source files in `backend/src/main/java/com/mksafenet/`
2. Spring Boot automatically detects changes and reloads (if running with `spring-boot:run`)
3. Refresh your browser or API client to test changes

**For database schema changes:**
- Modify JPA entities in `model/` directory
- Spring Boot will auto-update schema (via Hibernate `ddl-auto=update`)
- No manual SQL migrations needed for development

### Making Frontend Changes

1. Edit `.vue` or `.js` files in `frontend/src/`
2. Vite automatically recompiles and hot-reloads in the browser
3. No refresh needed; changes appear instantly

### Checking Logs

**Backend Logs:**
- Printed to console running `mvn spring-boot:run`
- Log level configured in `application.properties` (default: INFO)

**Frontend Logs:**
- Browser Developer Console (F12 → Console tab)
- Vite console output in terminal

## Running Tests

### Backend Unit Tests
```powershell
cd backend
mvn -q test
```

### Frontend Tests
Frontend testing setup depends on your test framework (Jest, Vitest, etc.). If configured:
```powershell
cd frontend
npm test
```

## Stopping the Application

### Backend
Press `Ctrl+C` in the terminal running `mvn spring-boot:run`

### Frontend
Press `Ctrl+C` in the terminal running `npm run dev` or close the terminal

## Troubleshooting

### Port Already in Use

**Backend (Port 8080):**
```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill process by PID (e.g., PID 1234)
taskkill /PID 1234 /F

# Or change port in application.properties
# server.port=8081
```

**Frontend (Port 5173):**
```powershell
# Find process using port 5173
netstat -ano | findstr :5173

# Or let Vite use next available port (will print in console)
npm run dev -- --host 0.0.0.0
```

### Maven Build Failures

**Clear cache and rebuild:**
```powershell
cd backend
mvn clean install -U
```

### NPM Package Conflicts

```powershell
cd frontend
rm -r node_modules package-lock.json
npm install
```

### Database Lock Issue
If backend crashes unexpectedly, the SQLite database may be locked:
```powershell
# Delete the database file to start fresh
rm backend/mksafenet.db

# Restart backend; database is auto-created
mvn spring-boot:run
```

### JWT Token Errors
If you see "401 Unauthorized" errors:
1. Ensure token is being sent in `Authorization: Bearer <token>` header
2. Check `jwt.secret` in `application.properties` is unchanged
3. Verify token hasn't expired (default 24 hours)
4. See [Security & Authentication](security.md) for details

## IDE Setup Recommendations

### IntelliJ IDEA / WebStorm

1. **Open Project:**
   - File → Open → Select `MkSafeNet_Kids` folder
   - Mark `backend/src/main/java` as "Sources Root"
   - Mark `frontend/src` as "Sources Root" (for frontend module)

2. **Maven Configuration:**
   - IntelliJ auto-detects `pom.xml`
   - Right-click `pom.xml` → Add as Maven Project

3. **Run Configurations:**
   - Create run config for `mvn spring-boot:run` (backend)
   - Create run config for `npm run dev` (frontend)
   - Run both simultaneously with "Edit Configurations" → "Compound"

4. **Vue/JavaScript Support:**
   - Ensure Vue plugin is installed (Settings → Plugins → search "Vue")

### VS Code

1. **Extensions:**
   - Extension Pack for Java
   - Vetur (Vue support)
   - Prettier (formatter)

2. **Run Terminal:**
   - Terminal → New Terminal
   - Split terminal: Ctrl+Shift+5
   - Run backend in one terminal, frontend in other

## Next Steps

- Read [Architecture Overview](architecture.md) to understand the codebase
- Explore [Backend Developer Guide](backend-developer-guide.md) to add features
- Check [API Reference](api-reference.md) for endpoint documentation
- Review [Frontend Developer Guide](frontend-developer-guide.md) for UI updates
- See [Troubleshooting](troubleshooting.md) for common issues

Happy coding! 🎉

