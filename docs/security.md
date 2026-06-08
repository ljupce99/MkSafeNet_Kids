# Security & Authentication

Comprehensive guide to JWT-based authentication, role-based access control, and security best practices.

## Overview

MkSafeNet_Kids uses **JWT (JSON Web Tokens)** for stateless, token-based authentication. Users receive a token on login and include it in subsequent requests. The backend validates tokens using a shared secret key.

## Authentication Flow

### 1. Login (Token Issuance)

```
User (Frontend)                    Backend
    |                                 |
    |-- POST /api/auth/login -------->|
    |   (username, password)           |
    |                                 |
    |                          Validate credentials
    |                          Generate JWT token
    |<------- 200 OK -----------|
    |   {token, role, user info}|
    |                                 |
    Store token in localStorage       |
```

**Request:**
```json
{
  "username": "teacher1",
  "password": "teacher1"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZWFjaGVyMSIsInJvbGUiOiJURUFDSEVSIiwiaWF0IjoxNzA5MjI4ODAwLCJleHAiOjE3MDkzMTUyMDB9.xyzabc...",
  "role": "TEACHER",
  "username": "teacher1",
  "displayName": "Ms. Smith",
  "schoolId": 1,
  "schoolName": "Test School"
}
```

**JWT Breakdown:**
- **Header**: `{"alg":"HS256","typ":"JWT"}`
- **Payload**: `{"sub":"teacher1","role":"TEACHER","iat":1709228800,"exp":1709315200}`
  - `sub` (subject): username
  - `role`: user role
  - `iat`: issued at (timestamp)
  - `exp`: expiration (timestamp)
- **Signature**: HMAC-SHA256 signature using backend secret key

### 2. Authenticated Request

```
User (Frontend)                    Backend
    |                                 |
    |-- GET /api/teacher/sessions ->|
    |   Header: Authorization:        |
    |   Bearer <token>                |
    |                                 |
    |                          JwtAuthFilter
    |                          validates token
    |                          extracts role/user
    |                                 |
    |<------ 200 OK + Data ------|
    |                                 |
```

**Frontend (Axios):**
```javascript
// API client includes token in every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
```

### 3. Token Expiration & Logout

**Token Expiration (Default: 24 hours):**
- Set in `backend/src/main/resources/application.properties`
  ```properties
  jwt.expiration=86400000  # milliseconds
  ```

**Logout (Frontend):**
```vue
<script setup>
const authStore = useAuthStore()
const logout = () => authStore.logout()  // Clears token from localStorage
</script>
```

**On 401 (Unauthorized):**
- Frontend automatically redirects to login
- Axios interceptor catches 401 response

```javascript
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.clear()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)
```

## Role-Based Access Control (RBAC)

Three roles control access to endpoints:

| Role | Description | Endpoints |
|------|-------------|-----------|
| **ADMIN** | System administrator | `/api/admin/*`, `/api/admin/scenarios/*` |
| **TEACHER** | Educator, creates sessions | `/api/teacher/*` |
| **STUDENT** | Participant in scenarios | `/api/chat/*`, `/api/certificates/*` |

### Endpoint Protection

**Backend (Java):**
```java
@RestController
@RequestMapping("/api/admin/scenarios")
@PreAuthorize("hasRole('ADMIN')")  // Only ADMIN role
@RequiredArgsConstructor
public class ScenarioController {
    
    @PostMapping
    public ResponseEntity<?> createScenario(@RequestBody Scenario scenario) {
        // Only ADMIN can create scenarios
    }
}
```

**Frontend (Vue Router):**
```javascript
const routes = [
  {
    path: '/admin',
    component: AdminDashboard,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  }
]

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
  } else if (to.meta.roles && !to.meta.roles.includes(authStore.role)) {
    next('/unauthorized')
  } else {
    next()
  }
})
```

## JWT Configuration

### Backend (Java)

**File**: `backend/src/main/resources/application.properties`

```properties
# JWT Secret (should be at least 32 characters)
jwt.secret=mksafenet-super-secret-jwt-key-change-in-production-32chars

# JWT Token Expiration (milliseconds)
# 86400000 = 24 hours
jwt.expiration=86400000
```

**Frontend URL (for CORS):**
```properties
app.frontend.url=http://localhost:5173
```

### Generating a Secure Secret

For production, generate a strong random secret:

```powershell
# PowerShell
$bytes = [System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32)
[Convert]::ToBase64String($bytes)
```

Or online: https://www.random.org/bytes/ (then Base64 encode)

**Recommended**: At least 32 characters, mix of uppercase, lowercase, numbers, special characters.

### Environment Variables (Production)

Instead of hardcoding secrets in `application.properties`, use environment variables:

```properties
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}
app.frontend.url=${APP_FRONTEND_URL}
```

Then set environment variables before running:
```powershell
$env:JWT_SECRET = "your-secret-key-here"
$env:JWT_EXPIRATION = "86400000"
$env:APP_FRONTEND_URL = "https://yourdomain.com"
java -jar app.jar
```

## JWT Implementation Details

### Token Generation (AuthService)

**Location**: `backend/src/main/java/com/mksafenet/service/AuthService.java`

```java
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        // Authenticate user (check password)
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        
        // Validate password (should use bcrypt)
        if (!passwordMatches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        // Return response with token
        return LoginResponse.builder()
            .token(token)
            .role(user.getRole().toString())
            .username(user.getUsername())
            .displayName(user.getDisplayName())
            .schoolId(user.getSchool().getId())
            .schoolName(user.getSchool().getName())
            .build();
    }
}
```

### Token Validation (JwtAuthFilter)

**Location**: `backend/src/main/java/com/mksafenet/config/JwtAuthFilter.java`

```java
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        // Extract token from header
        String token = extractTokenFromHeader(request);

        if (token != null && jwtUtil.isTokenValid(token)) {
            // Extract username from token
            String username = jwtUtil.getUsername(token);
            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null) {
                // Create authentication object
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities()
                    );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
```

## Security Best Practices

### 1. **Password Hashing**

**Current Implementation** (verify this in your code):
```java
// Should use bcrypt or similar
String hashedPassword = passwordEncoder.encode(rawPassword);
```

**Spring Security PasswordEncoder:**
```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 2. **HTTPS in Production**

- **Always use HTTPS** for all endpoints
- JWT tokens are vulnerable if transmitted over HTTP
- Configure SSL/TLS certificates on production server

### 3. **Secure Token Storage (Frontend)**

**Current (localStorage):**
```javascript
localStorage.setItem('token', token)
```

**Risks:**
- Vulnerable to XSS (Cross-Site Scripting)
- Not accessible to backend (no CSRF protection)

**Alternative (HttpOnly Cookies)** - More Secure:
```java
// Backend sets HttpOnly cookie
response.addCookie(new Cookie("token", jwtToken));
```

```javascript
// Frontend automatically includes cookie in requests
// No need to manually add Authorization header
```

### 4. **Token Refresh**

For long-running sessions, implement a refresh token:

1. Issuer two tokens:
   - **Access Token**: Short-lived (15 minutes)
   - **Refresh Token**: Long-lived (7 days)

2. Frontend uses refresh token to get new access token when expired

3. Refresh token endpoint:
   ```
   POST /api/auth/refresh
   Body: { refreshToken: "..." }
   Response: { accessToken: "...", refreshToken: "..." }
   ```

### 5. **CORS Configuration**

**Backend (Java):**
```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of(
                "http://localhost:5173",      // Dev
                "https://yourdomain.com"      // Production
            ));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(true);
            return config;
        }));
        // ... other config
    }
}
```

### 6. **Validate Input**

```java
@PostMapping("/create")
public ResponseEntity<?> create(@RequestBody @Valid CreateRequest request) {
    // @Valid triggers bean validation
}
```

### 7. **Rate Limiting**

Consider adding rate limiting to prevent brute-force attacks:

```java
// Example: Spring Cloud Gateway or custom interceptor
@Component
public class RateLimitFilter {
    private final RateLimiter rateLimiter = RateLimiter.create(100); // 100 requests/second

    public void doFilter(HttpServletRequest request) {
        if (!rateLimiter.tryAcquire()) {
            throw new TooManyRequestsException();
        }
    }
}
```

### 8. **Audit Logging**

Log sensitive operations:

```java
@Service
public class AuditService {
    public void log(String action, User user, String details) {
        logger.info("USER_ACTION: {} by {} - {}", action, user.getUsername(), details);
    }
}
```

## Troubleshooting Authentication Issues

### Issue: "401 Unauthorized" Error

**Causes:**
1. Token missing from request header
2. Token expired (> 24 hours)
3. Token invalid (wrong secret or tampered with)
4. Token not matching current user

**Solutions:**
```javascript
// Verify token is being sent
const token = localStorage.getItem('token')
console.log('Token:', token)

// Check token expiration
const payload = JSON.parse(atob(token.split('.')[1]))
console.log('Expires at:', new Date(payload.exp * 1000))

// Re-login if token expired
if (new Date() > new Date(payload.exp * 1000)) {
  authStore.logout()
  router.push('/login')
}
```

### Issue: "403 Forbidden" Error

**Cause:** User's role doesn't have permission for endpoint

**Solution:**
- Check user's role matches endpoint requirement
- Verify `@PreAuthorize` annotation on controller method
- Admin panel may be restricted to ADMIN role only

### Issue: CORS Error

**Cause:** Frontend origin not allowed in backend CORS config

**Solution:**
- Check `app.frontend.url` in `application.properties`
- Add frontend URL to CORS allowed origins in `SecurityConfig`
- Ensure URLs match exactly (http vs https, ports, domains)

## Testing Authentication

### Login Test (curl)

```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "username": "admin",
    "password": "admin"
  }' | ConvertFrom-Json | Select-Object -ExpandProperty token | Out-File token.txt

# Store token in variable
$token = Get-Content token.txt

# Use token in subsequent request
curl -X GET http://localhost:8080/api/teacher/sessions `
  -H "Authorization: Bearer $token"
```

### Postman Collection

1. Create Postman collection
2. Set variable: `{{base_url}}` = `http://localhost:8080/api`
3. Create request: POST `/{{base_url}}/auth/login`
4. In response, copy token to new variable: `{{token}}`
5. Use `{{token}}` in Authorization header for protected endpoints

## Additional Resources

- [JWT.io](https://jwt.io/) – JWT decoder and documentation
- [Spring Security Docs](https://spring.io/projects/spring-security)
- [Vue Router Navigation Guards](https://router.vuejs.org/guide/advanced/navigation-guards)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)

---

See [API Reference](api-reference.md) for endpoint documentation and [Architecture](architecture.md) for system design.

