# Frontend Developer Guide

Guide for building Vue 3 components, creating views, integrating with backend APIs, and managing application state.

## Project Structure

```
frontend/src/
├── views/                   # Full page components (one per route)
│   ├── LoginView.vue        # Login page
│   ├── ChatView.vue         # Chat/scenario interface
│   ├── admin/
│   │   └── AdminDashboard.vue
│   └── teacher/
│       └── TeacherDashboard.vue
├── components/              # Reusable UI components
│   └── ConsequenceModal.vue
├── stores/                  # Pinia state management
│   └── auth.js              # Authentication state
├── router/                  # Vue Router configuration
│   └── index.js
├── api/                     # Axios HTTP client
│   └── index.js
├── assets/                  # Static files (images, etc.)
│   └── logo.png
├── App.vue                  # Root component
├── main.js                  # Entry point
└── index.html               # HTML template
```

## Technology Stack

- **Vue 3** – Reactive UI framework (Composition API preferred)
- **Vue Router 4** – Client-side routing
- **Pinia** – State management
- **Axios** – HTTP client
- **Vite** – Build tool and dev server

## Creating a New View Component

Views are full-page components that map to routes. Create them in `frontend/src/views/`.

### Example: New Dashboard View

**File**: `frontend/src/views/MyView.vue`

```vue
<template>
  <div class="my-view">
    <h1>My View</h1>
    
    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else class="content">
      <p>{{ message }}</p>
      <button @click="fetchData">Refresh Data</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api/index.js'

const message = ref('Hello from MyView')
const loading = ref(false)
const error = ref(null)

const fetchData = async () => {
  loading.value = true
  error.value = null
  try {
    const res = await api.get('/my-endpoint')
    message.value = res.data.message
  } catch (err) {
    error.value = err.response?.data?.error || 'An error occurred'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.my-view {
  padding: 20px;
}

.loading, .error {
  padding: 10px;
  margin: 10px 0;
}

.error {
  color: red;
  background: #ffe0e0;
}

.content {
  margin-top: 20px;
}

button {
  padding: 10px 20px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

button:hover {
  background: #0056b3;
}
</style>
```

**Notes:**
- Use `<script setup>` syntax (modern Vue 3 Composition API)
- Import utilities like `api` from `api/index.js`
- Use `ref()` for reactive state
- Use `onMounted()` to fetch data when component loads
- Handle loading and error states
- Use `v-if`, `v-else`, `v-for` for conditional/list rendering

## Creating a Reusable Component

Components are smaller, reusable UI pieces. Create them in `frontend/src/components/`.

### Example: Confirmation Dialog Component

**File**: `frontend/src/components/ConfirmDialog.vue`

```vue
<template>
  <div v-if="visible" class="dialog-overlay">
    <div class="dialog-box">
      <h2>{{ title }}</h2>
      <p>{{ message }}</p>
      <div class="dialog-actions">
        <button @click="confirm" class="btn-confirm">{{ confirmText }}</button>
        <button @click="cancel" class="btn-cancel">{{ cancelText }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, required: true },
  message: { type: String, required: true },
  confirmText: { type: String, default: 'Confirm' },
  cancelText: { type: String, default: 'Cancel' }
})

const emit = defineEmits(['confirm', 'cancel'])

const confirm = () => emit('confirm')
const cancel = () => emit('cancel')
</script>

<style scoped>
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.dialog-box {
  background: white;
  padding: 20px;
  border-radius: 8px;
  max-width: 400px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
}

.dialog-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
  justify-content: flex-end;
}

button {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-confirm {
  background: #28a745;
  color: white;
}

.btn-confirm:hover {
  background: #218838;
}

.btn-cancel {
  background: #6c757d;
  color: white;
}

.btn-cancel:hover {
  background: #5a6268;
}
</style>
```

**Usage in Another Component:**

```vue
<template>
  <div>
    <button @click="showDialog = true">Delete Item</button>
    <ConfirmDialog
      :visible="showDialog"
      title="Delete Confirmation"
      message="Are you sure you want to delete this item?"
      confirmText="Delete"
      cancelText="Cancel"
      @confirm="handleDelete"
      @cancel="showDialog = false"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ConfirmDialog from '../components/ConfirmDialog.vue'

const showDialog = ref(false)

const handleDelete = async () => {
  // Call API to delete
  showDialog.value = false
}
</script>
```

**Notes:**
- Use `defineProps()` to accept props
- Use `defineEmits()` to emit events
- Components communicate via props (down) and events (up)

## API Integration with Axios

### Making HTTP Requests

The `api` client is configured in `frontend/src/api/index.js` with JWT token handling.

**Example: Login**

```vue
<script setup>
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth.js'
import { useRouter } from 'vue-router'
import api from '../api/index.js'

const authStore = useAuthStore()
const router = useRouter()

const username = ref('')
const password = ref('')
const error = ref('')

const login = async () => {
  try {
    const response = await authStore.login(username.value, password.value)
    router.push('/dashboard')
  } catch (err) {
    error.value = err.response?.data?.error || 'Login failed'
  }
}
</script>
```

**Example: Fetching Data**

```vue
<script setup>
import { ref, onMounted } from 'vue'
import api from '../api/index.js'

const items = ref([])
const loading = ref(false)

const fetchItems = async () => {
  loading.value = true
  try {
    const response = await api.get('/admin/items')
    items.value = response.data
  } catch (err) {
    console.error('Error fetching items:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchItems())
</script>
```

**Example: Creating an Item (POST)**

```vue
<script setup>
import { ref } from 'vue'
import api from '../api/index.js'

const name = ref('')
const description = ref('')
const loading = ref(false)
const error = ref('')

const createItem = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await api.post('/admin/items', {
      name: name.value,
      description: description.value
    })
    // Success - clear form
    name.value = ''
    description.value = ''
    // Emit event to parent or refresh list
  } catch (err) {
    error.value = err.response?.data?.error || 'Failed to create'
  } finally {
    loading.value = false
  }
}
</script>
```

### Error Handling

The Axios interceptor automatically:
- Attaches JWT token to requests (from localStorage)
- Redirects to login on 401 (unauthorized)

Custom error handling:

```vue
<script setup>
const handleRequest = async () => {
  try {
    const response = await api.post('/endpoint', data)
    // Success
  } catch (err) {
    if (err.response?.status === 401) {
      // Unauthorized - interceptor handles redirect
    } else if (err.response?.status === 403) {
      // Forbidden - insufficient permissions
      alert('You do not have permission to perform this action')
    } else if (err.response?.status === 400) {
      // Validation error
      alert(err.response.data.error)
    } else {
      // Server error
      alert('An error occurred. Please try again.')
    }
  }
}
</script>
```

## State Management with Pinia

### Using the Auth Store

The `auth` store in `frontend/src/stores/auth.js` manages user authentication.

**Accessing Auth State:**

```vue
<script setup>
import { useAuthStore } from '../stores/auth.js'

const authStore = useAuthStore()

// Access state
const isLoggedIn = authStore.isLoggedIn
const username = authStore.displayName
const role = authStore.role

// Call action
const logout = () => authStore.logout()
</script>
```

### Creating a New Store

**File**: `frontend/src/stores/mystore.js`

```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api/index.js'

export const useMyStore = defineStore('mystore', () => {
  const items = ref([])
  const loading = ref(false)

  const itemCount = computed(() => items.value.length)

  async function fetchItems() {
    loading.value = true
    try {
      const res = await api.get('/my-items')
      items.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function addItem(item) {
    const res = await api.post('/my-items', item)
    items.value.push(res.data)
  }

  function clearItems() {
    items.value = []
  }

  return { items, loading, itemCount, fetchItems, addItem, clearItems }
})
```

**Usage:**

```vue
<script setup>
import { useMyStore } from '../stores/mystore.js'

const myStore = useMyStore()

// Access state and computed
console.log(myStore.itemCount)

// Call actions
await myStore.fetchItems()
</script>
```

## Routing

Routes are defined in `frontend/src/router/index.js`.

### Adding a Route

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import MyView from '../views/MyView.vue'
import { useAuthStore } from '../stores/auth.js'

const routes = [
  {
    path: '/my-route',
    name: 'MyRoute',
    component: MyView,
    meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'] }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard to check auth
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

export default router
```

### Navigating Programmatically

```vue
<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goToDashboard = () => {
  router.push('/dashboard')
  // or router.push({ name: 'Dashboard' })
}
</script>
```

## Component Communication

### Parent to Child (Props)

```vue
<!-- Parent -->
<ChildComponent :title="myTitle" :count="5" />

<!-- Child -->
<script setup>
defineProps({
  title: String,
  count: Number
})
</script>
```

### Child to Parent (Events)

```vue
<!-- Child -->
<button @click="$emit('delete', itemId)">Delete</button>

<!-- Parent -->
<ChildComponent @delete="handleDelete" />
```

### Sibling Communication (Store or Event Bus)

Use Pinia store for shared state between components.

## Best Practices

1. **Keep Components Small**
   - One responsibility per component
   - Extract reusable logic to stores or composables

2. **Reactive State Management**
   - Use `ref()` for primitives, `reactive()` for objects
   - Use Pinia for cross-component state

3. **Async Operations**
   - Use `async/await` for clarity
   - Always handle errors with try/catch
   - Show loading states during requests

4. **Performance**
   - Use `v-show` for frequent toggling, `v-if` for conditional rendering
   - Lazy-load routes for large apps
   - Avoid inline functions in templates

5. **Code Organization**
   - Separate concerns (template, script, style)
   - Use descriptive variable/function names
   - Comment complex logic

6. **Styling**
   - Use scoped styles to avoid conflicts: `<style scoped>`
   - Use CSS classes, not inline styles
   - Consider a CSS framework (Bootstrap, Tailwind) for consistency

## Common Vue 3 Composition API Patterns

### Fetching Data on Mount

```vue
<script setup>
import { ref, onMounted } from 'vue'

const data = ref(null)
const loading = ref(true)
const error = ref(null)

const fetchData = async () => {
  try {
    const res = await api.get('/data')
    data.value = res.data
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>
```

### Computed Properties

```vue
<script setup>
import { ref, computed } from 'vue'

const firstName = ref('John')
const lastName = ref('Doe')

const fullName = computed(() => `${firstName.value} ${lastName.value}`)
</script>

<template>
  <p>{{ fullName }}</p> <!-- "John Doe" -->
</template>
```

### Watchers (React to Changes)

```vue
<script setup>
import { ref, watch } from 'vue'

const searchQuery = ref('')
const results = ref([])

watch(searchQuery, async (newQuery) => {
  if (newQuery) {
    const res = await api.get(`/search?q=${newQuery}`)
    results.value = res.data
  }
})
</script>
```

## Debugging

### Browser DevTools

1. Open Developer Tools (F12)
2. Check Console for errors
3. Use Vue DevTools extension for inspecting components
4. Check Network tab for API requests

### Vue DevTools Extension

Install "Vue.js devtools" browser extension to inspect:
- Component tree
- Props and state
- Pinia store state

### Console Logging

```vue
<script setup>
console.log('Component mounted')
console.log('Data:', myVariable)
</script>
```

## Building for Production

```powershell
cd frontend
npm run build
```

Generates optimized files in `frontend/dist/`. Deploy these static files to a web server.

---

See [API Reference](api-reference.md) for endpoint documentation, [Architecture](architecture.md) for system design, and [Backend Developer Guide](backend-developer-guide.md) for backend integration details.

