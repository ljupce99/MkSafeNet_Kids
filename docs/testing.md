# Testing Guide

Comprehensive guide to testing MkSafeNet_Kids, including unit tests, integration tests, and manual testing strategies.

## Testing Strategy

| Test Type | Scope | Tools | Coverage |
|-----------|-------|-------|----------|
| **Unit Tests** | Individual functions/methods | JUnit 5, Mockito (backend); Jest/Vitest (frontend) | Classes, services |
| **Integration Tests** | API endpoints, database interaction | Spring Boot Test, Testcontainers | Controllers, repositories |
| **End-to-End (E2E) Tests** | Complete user workflows | Selenium, Cypress, Playwright | Full application flows |
| **Manual Testing** | User interface, real-world scenarios | Browser, QA checklist | UI/UX, edge cases |

## Backend Testing

### Unit Testing with JUnit 5 and Mockito

**File**: `backend/src/test/java/com/mksafenet/service/AuthServiceTest.java`

```java
package com.mksafenet.service;

import com.mksafenet.dto.LoginRequest;
import com.mksafenet.dto.LoginResponse;
import com.mksafenet.model.Role;
import com.mksafenet.model.User;
import com.mksafenet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthService Tests")
class AuthServiceTest {

    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(userRepository, jwtUtil);
    }

    @Test
    @DisplayName("Login with valid credentials should return token")
    void testLogin_ValidCredentials() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin");

        User user = User.builder()
            .id(1L)
            .username("admin")
            .password("$2a$10$abc123...") // bcrypt hash
            .displayName("Administrator")
            .role(Role.ADMIN)
            .build();

        when(userRepository.findByUsername("admin"))
            .thenReturn(Optional.of(user));

        when(jwtUtil.generateToken(user))
            .thenReturn("valid.jwt.token");

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("valid.jwt.token", response.getToken());
        assertEquals("ADMIN", response.getRole());
        assertEquals("admin", response.getUsername());

        // Verify interactions
        verify(userRepository).findByUsername("admin");
        verify(jwtUtil).generateToken(user);
    }

    @Test
    @DisplayName("Login with invalid username should throw exception")
    void testLogin_InvalidUsername() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password");

        when(userRepository.findByUsername("nonexistent"))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(request);
        });
    }

    @Test
    @DisplayName("Login with invalid password should throw exception")
    void testLogin_InvalidPassword() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrongpassword");

        User user = User.builder()
            .username("admin")
            .password("$2a$10$abc123...")
            .build();

        when(userRepository.findByUsername("admin"))
            .thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(request);
        });
    }
}
```

**Test Naming Convention:**
- `testMethodName_Condition_ExpectedResult()`
- Example: `testLogin_ValidCredentials_ReturnsToken()`

### Integration Testing

**File**: `backend/src/test/java/com/mksafenet/controller/AuthControllerTest.java`

```java
package com.mksafenet.controller;

import com.mksafenet.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLogin_ValidCredentials_Returns200() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin");

        String json = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.role").value("ADMIN"))
            .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void testLogin_InvalidCredentials_Returns401() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrongpassword");

        String json = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").exists());
    }
}
```

### Running Backend Tests

```powershell
# Run all tests
cd backend
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Run with coverage report
mvn clean test jacoco:report

# View coverage report
# Open: backend/target/site/jacoco/index.html
```

### Test Coverage Goals

- **Services**: ≥ 80% coverage
- **Controllers**: ≥ 70% coverage
- **Entities**: ≥ 50% coverage (mostly getters/setters)

## Frontend Testing

### Unit Testing with Vitest

**File**: `frontend/src/stores/__tests__/auth.test.js`

```javascript
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../auth'
import api from '../../api/index'

// Mock the API
vi.mock('../../api/index', () => ({
  default: {
    post: vi.fn()
  }
}))

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should initialize with empty token', () => {
    const authStore = useAuthStore()
    expect(authStore.token).toBeNull()
    expect(authStore.isLoggedIn).toBe(false)
  })

  it('should store token after login', async () => {
    const authStore = useAuthStore()
    
    api.post.mockResolvedValue({
      data: {
        token: 'test-token',
        role: 'ADMIN',
        username: 'admin',
        displayName: 'Administrator'
      }
    })

    await authStore.login('admin', 'admin')

    expect(authStore.token).toBe('test-token')
    expect(authStore.role).toBe('ADMIN')
    expect(authStore.isLoggedIn).toBe(true)
  })

  it('should clear state on logout', async () => {
    const authStore = useAuthStore()
    
    // Setup initial state
    authStore.token = 'test-token'
    authStore.role = 'ADMIN'

    // Logout
    authStore.logout()

    expect(authStore.token).toBeNull()
    expect(authStore.role).toBeNull()
    expect(authStore.isLoggedIn).toBe(false)
  })
})
```

### Component Testing with Vitest + Vue Test Utils

**File**: `frontend/src/views/__tests__/LoginView.test.js`

```javascript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import LoginView from '../LoginView.vue'

vi.mock('../../api/index', () => ({
  default: {
    post: vi.fn()
  }
}))

describe('LoginView', () => {
  it('renders login form', () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [createPinia()]
      }
    })

    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
    expect(wrapper.find('button').text()).toContain('Login')
  })

  it('submits form on button click', async () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [createPinia()]
      }
    })

    // Set input values
    await wrapper.find('input[type="text"]').setValue('admin')
    await wrapper.find('input[type="password"]').setValue('admin')

    // Click submit button
    await wrapper.find('button').trigger('click')

    // Assert
    expect(wrapper.vm.loading).toBe(false) // After async operation
  })

  it('displays error message on login failure', async () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [createPinia()]
      }
    })

    // Mock API error
    api.post.mockRejectedValue({
      response: { data: { error: 'Invalid credentials' } }
    })

    await wrapper.find('input[type="text"]').setValue('admin')
    await wrapper.find('input[type="password"]').setValue('wrong')
    await wrapper.find('button').trigger('click')

    await wrapper.vm.$nextTick()

    expect(wrapper.find('.error').text()).toContain('Invalid credentials')
  })
})
```

### Running Frontend Tests

```powershell
# Install test dependencies (if not already present)
cd frontend
npm install --save-dev vitest @vue/test-utils jsdom

# Run tests
npm test

# Run with coverage
npm test -- --coverage

# Watch mode (auto-rerun on file changes)
npm test -- --watch
```

## End-to-End (E2E) Testing

### Example with Playwright

**File**: `tests/e2e/login.spec.ts`

```typescript
import { test, expect } from '@playwright/test'

test.describe('Login Flow', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:5173/login')
  })

  test('should login with valid credentials', async ({ page }) => {
    // Fill login form
    await page.fill('input[name="username"]', 'admin')
    await page.fill('input[name="password"]', 'admin')

    // Submit form
    await page.click('button[type="submit"]')

    // Wait for navigation
    await page.waitForURL('http://localhost:5173/dashboard')

    // Assert dashboard is visible
    expect(await page.locator('h1').textContent()).toContain('Dashboard')
  })

  test('should show error with invalid credentials', async ({ page }) => {
    // Fill with wrong credentials
    await page.fill('input[name="username"]', 'admin')
    await page.fill('input[name="password"]', 'wrongpassword')

    // Submit form
    await page.click('button[type="submit"]')

    // Assert error message is visible
    const error = page.locator('.error-message')
    await expect(error).toBeVisible()
    await expect(error).toContainText('Invalid username or password')
  })
})
```

**Install Playwright:**
```powershell
cd frontend
npm install --save-dev @playwright/test
npx playwright install
```

**Run E2E Tests:**
```powershell
npx playwright test

# Run specific test
npx playwright test login.spec.ts

# Run in headed mode (see browser)
npx playwright test --headed

# Debug mode
npx playwright test --debug
```

## Manual Testing Checklist

### Authentication
- [ ] Login with valid credentials
- [ ] Login with invalid username
- [ ] Login with invalid password
- [ ] Logout functionality
- [ ] Token persistence in localStorage
- [ ] Automatic redirect to login on 401 error

### Teacher Features
- [ ] Create new session
- [ ] View all sessions
- [ ] View session details
- [ ] Generate/scan QR code
- [ ] Toggle session active/inactive
- [ ] View student results

### Student Features
- [ ] Join session via token
- [ ] View scenario questions
- [ ] Select answer option
- [ ] View immediate feedback
- [ ] Progress through scenarios
- [ ] View final score/certificate
- [ ] Download certificate PDF

### Admin Features
- [ ] View all schools
- [ ] Create new school
- [ ] Create new teacher
- [ ] View all teachers
- [ ] Create/edit scenarios
- [ ] View global statistics

### UI/UX
- [ ] Responsive design on mobile/tablet/desktop
- [ ] No console errors
- [ ] Loading spinners appear during API calls
- [ ] Error messages are clear and helpful
- [ ] Navigation works correctly
- [ ] Buttons are clickable and responsive
- [ ] Forms validate input

## Continuous Integration (CI)

### GitHub Actions Example

**File**: `.github/workflows/test.yml`

```yaml
name: Tests

on: [push, pull_request]

jobs:
  backend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '17'
      - run: cd backend && mvn clean test

  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-node@v2
        with:
          node-version: '18'
      - run: cd frontend && npm install && npm test
```

## Test Reporting

### Backend Coverage Report

```powershell
cd backend
mvn clean test jacoco:report

# Open HTML report
# Windows
start target/site/jacoco/index.html

# Mac
open target/site/jacoco/index.html

# Linux
firefox target/site/jacoco/index.html
```

### Frontend Coverage Report

```powershell
cd frontend
npm test -- --coverage

# View report
# Windows
start coverage/index.html

# Mac
open coverage/index.html
```

## Performance Testing

### Load Testing with JMeter or k6

**File**: `tests/performance/load-test.js`

```javascript
import http from 'k6/http'
import { check } from 'k6'

export let options = {
  stages: [
    { duration: '2m', target: 100 },  // Ramp-up
    { duration: '5m', target: 100 },  // Stay at 100 users
    { duration: '2m', target: 0 }     // Ramp-down
  ]
}

export default function () {
  let response = http.get('http://localhost:8080/api/teacher/sessions', {
    headers: {
      'Authorization': `Bearer ${__ENV.TOKEN}`
    }
  })

  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500
  })
}
```

**Run:**
```powershell
# Install k6 (if not present)
# https://k6.io/docs/getting-started/installation/

# Run test
k6 run tests/performance/load-test.js
```

---

See [Backend Developer Guide](backend-developer-guide.md), [Frontend Developer Guide](frontend-developer-guide.md), and [API Reference](api-reference.md) for more implementation details.

