# Troubleshooting & FAQ

Common issues, error messages, and their solutions.

## Backend Issues

### Build Failures

#### Error: "Maven not found" or "'mvn' is not recognized"

**Cause**: Maven not installed or not in PATH

**Solution**:
```powershell
# Install Maven
# Download from https://maven.apache.org/download.cgi
# Extract and add to PATH (System Variables → JAVA_HOME, PATH)

# Verify installation
mvn -version

# Or use Maven wrapper if included
cd backend
./mvnw clean install
```

#### Error: "Cannot find symbol" in Java compilation

**Cause**: Missing dependency or import

**Solution**:
```powershell
cd backend

# Clear cache and reinstall dependencies
mvn clean install -U

# Check pom.xml for typos in dependency names
```

#### Error: "Port 8080 is already in use"

**Cause**: Backend process already running or another application using port

**Solution (PowerShell)**:
```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill process by PID (e.g., PID 5432)
taskkill /PID 5432 /F

# Or change port in application.properties
# server.port=8081
```

### Runtime Errors

#### Error: "No such file or directory: mksafenet.db"

**Cause**: Database file not created yet

**Solution**:
```powershell
# Database auto-creates on first run
# Just ensure backend has write permissions in its directory

# Or create manually
cd backend
echo $null > mksafenet.db
```

#### Error: "401 Unauthorized" on API calls

**Cause**: Missing or invalid JWT token

**Solution**:
```javascript
// Frontend: Verify token in localStorage
console.log('Token:', localStorage.getItem('token'))

// Verify token is sent in request
// Check Network tab → Request Headers → Authorization: Bearer <token>

// Re-login if token expired (default 24 hours)
```

Check [Security & Authentication](security.md) for JWT troubleshooting.

#### Error: "CORS error: Access blocked by CORS policy"

**Cause**: Frontend origin not allowed by backend CORS config

**Solution**:
1. Check `app.frontend.url` in `backend/src/main/resources/application.properties`
2. Ensure it matches your frontend URL exactly
3. Include protocol, domain, and port: `http://localhost:5173`

```properties
app.frontend.url=http://localhost:5173
```

4. Restart backend
5. Clear browser cache

#### Error: "NullPointerException" in logs

**Cause**: Code trying to access null object

**Solution**:
1. Check backend logs for full stack trace
2. Look at line number mentioned in error
3. Add null checks: `if (object != null) { ... }`
4. Use Optional: `Optional.ofNullable(object).orElseThrow(...)`

### Database Issues

#### Error: "Database is locked"

**Cause**: Multiple processes accessing SQLite or previous process didn't close properly

**Solution**:
```powershell
# Kill Java process
taskkill /IM java.exe /F

# Delete database file to start fresh
Remove-Item backend/mksafenet.db -Force

# Restart backend
mvn spring-boot:run
```

#### Error: "Foreign key constraint violation"

**Cause**: Deleting record that has dependent records

**Solution**:
1. Delete dependent records first
2. Or configure cascade delete in entity:
   ```java
   @OneToMany(cascade = CascadeType.ALL)
   private List<Child> children;
   ```

## Frontend Issues

### Build Failures

#### Error: "npm not found" or "npm is not recognized"

**Cause**: Node.js not installed or not in PATH

**Solution**:
```powershell
# Install Node.js from https://nodejs.org/
# Includes npm

# Verify installation
node --version
npm --version
```

#### Error: "Module not found" or "Cannot find module"

**Cause**: Dependency not installed or import path wrong

**Solution**:
```powershell
cd frontend

# Clear node_modules and reinstall
Remove-Item node_modules -Recurse -Force
npm install

# Check for typos in import path
# import api from '../api/index.js'  # Correct
# import api from '../api'           # Wrong
```

#### Error: "Unexpected token" in build

**Cause**: JavaScript/Vue syntax error

**Solution**:
1. Check IDE for red squiggly lines
2. Look at line number in error message
3. Common issues:
   - Missing closing bracket/brace: `}`, `]`, `)`
   - Unmatched quotes: `'text"` should be `'text'`
   - Wrong syntax in template: `{{ var }` should be `{{ var }}`

### Runtime Errors

#### Error: "Cannot read property 'X' of undefined"

**Cause**: Trying to access property of undefined/null object

**Solution**:
```vue
<!-- Wrong -->
{{ user.name }}  <!-- Error if user is undefined -->

<!-- Right -->
{{ user?.name }}  <!-- Optional chaining -->
<!-- or -->
<div v-if="user">{{ user.name }}</div>
```

#### Error: "401 Unauthorized" on API requests

**Cause**: Same as backend (see above)

**Solution**:
```javascript
// Check if token is saved
console.log('Stored token:', localStorage.getItem('token'))

// Verify auth store has token
const authStore = useAuthStore()
console.log('Auth token:', authStore.token)

// Re-login if expired
if (!authStore.isLoggedIn) {
  router.push('/login')
}
```

#### Error: "API request failed" with no details

**Cause**: Network error or server not responding

**Solution**:
1. Check if backend is running: `http://localhost:8080`
2. Check browser Network tab for actual error
3. Check backend logs for errors
4. Verify CORS configuration (see CORS error above)

#### Error: "Pinia state not persisting"

**Cause**: State cleared on page refresh

**Solution**:
1. Manually save to localStorage:
   ```javascript
   const authStore = useAuthStore()
   watch(() => authStore.token, (newToken) => {
     if (newToken) localStorage.setItem('token', newToken)
   })
   ```
2. Or use Pinia persistence plugin

#### White screen of death (blank page)

**Cause**: JavaScript error preventing app render

**Solution**:
1. Open Developer Console (F12)
2. Check for errors in Console tab
3. Check Network tab for failed requests
4. Look for stack trace in console
5. Common causes:
   - API endpoint not found (404)
   - Syntax error in component
   - Missing import

```vue
<script setup>
// Check all imports resolve
import api from '../api/index.js'  // Verify file exists
</script>
```

### Performance Issues

#### Slow page loads or API calls hang

**Cause**: Backend not responding or network latency

**Solution**:
1. Check backend is running
2. Check network latency: F12 → Network → observe request times
3. Verify backend logs for slow queries
4. Add loading state to UI while waiting

#### High memory usage

**Cause**: Memory leak or large data structures

**Solution**:
1. Check browser DevTools → Memory tab
2. Look for detached DOM nodes
3. Clear watchers and intervals on component unmount:
   ```vue
   <script setup>
   onUnmounted(() => {
     // Clean up
   })
   </script>
   ```

## Port Conflicts

### Check what's using a port

```powershell
# Check port 8080
netstat -ano | findstr :8080

# Check port 5173
netstat -ano | findstr :5173
```

### Solution: Change ports

**Backend** (`application.properties`):
```properties
server.port=8081
```

**Frontend** (`vite.config.js`):
```javascript
export default {
  server: {
    port: 5174
  }
}
```

Then update `app.frontend.url` in backend to match.

## HTTPS/SSL Issues

#### Error: "SSL certificate problem" or "NET::ERR_CERT_AUTHORITY_INVALID"

**Cause**: Self-signed or invalid SSL certificate

**Solution (Development Only)**:
- Use HTTP (not HTTPS) for local development
- Don't ignore certificate warnings in production

**Solution (Production)**:
1. Obtain valid certificate from Let's Encrypt, Comodo, etc.
2. Configure in web server (Nginx, Apache)
3. See [Deployment Guide](deployment.md) for HTTPS setup

## Environment Variables

#### Can't find environment variable

**Backend** (`application.properties`):
```properties
# Read from environment variable
jwt.secret=${JWT_SECRET}

# With default if not set
jwt.secret=${JWT_SECRET:default-secret}
```

**Frontend** (Vite):
```javascript
// Access via import.meta.env
const apiUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
```

**Setting variables**:
```powershell
# Windows PowerShell
$env:JWT_SECRET = "my-secret-value"
$env:VITE_API_BASE_URL = "http://localhost:8080"

# Run app
mvn spring-boot:run
```

## Common Error Messages & Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| "Invalid username or password" | Wrong login credentials | Verify username/password (default: admin/admin) |
| "Session not found or inactive" | Invalid/expired session token | Create new session and get fresh token |
| "Forbidden" (403) | Insufficient permissions | Check user role matches endpoint requirement |
| "Resource not found" (404) | Endpoint doesn't exist | Check URL spelling and API reference |
| "Internal server error" (500) | Backend error | Check backend logs for detailed error |
| "Cannot connect to server" | Backend not running | Start backend: `mvn spring-boot:run` |
| "Failed to load module" | Missing import | Install dependency: `npm install <package>` |

## Debugging Tips

### Backend Debugging

1. **Enable SQL Logging**:
   ```properties
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   ```

2. **Add Log Statements**:
   ```java
   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;

   private static final Logger logger = LoggerFactory.getLogger(MyClass.class);
   logger.info("Debug message: {}", variable);
   logger.error("Error occurred", exception);
   ```

3. **Use IDE Debugger**:
   - Set breakpoint (click line number)
   - Step through code (F10)
   - Watch variables
   - Inspect stack trace

### Frontend Debugging

1. **Console Logging**:
   ```javascript
   console.log('Debug:', variable)
   console.error('Error occurred:', error)
   console.table(arrayOfObjects)  // Pretty-print arrays
   ```

2. **Vue DevTools Extension**:
   - Inspect component tree
   - View props and state
   - Track state changes

3. **Network Debugging**:
   - F12 → Network tab
   - Check request/response headers
   - View response body
   - Check response status codes

4. **Browser Debugger**:
   - Set breakpoints in Sources tab
   - Step through JavaScript
   - Watch variables
   - Check call stack

## Getting Help

1. **Check Logs**:
   - Backend: Console output from `mvn spring-boot:run`
   - Frontend: Browser Console (F12)

2. **Search Stack Overflow**: Include error message + technology

3. **Check Official Docs**:
   - Spring Boot: https://spring.io/projects/spring-boot
   - Vue: https://vuejs.org/guide/
   - Pinia: https://pinia.vuejs.org/

4. **Review [Architecture](architecture.md) and [API Reference](api-reference.md)**

5. **Open Issue**: Provide:
   - Error message (full stack trace)
   - Steps to reproduce
   - Expected behavior
   - Actual behavior
   - Your environment (OS, Java version, Node version, etc.)

---

Still stuck? See [Local Setup Guide](setup-local.md) for step-by-step environment configuration.

