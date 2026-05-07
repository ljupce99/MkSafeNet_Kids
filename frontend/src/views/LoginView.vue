<template>
  <div class="login-wrap">
    <div class="login-card">
      <div class="logo">
        <span class="logo-icon">🛡️</span>
        <div>
          <h1>MkSafeNet</h1>
          <p>Phishing Awareness Platform</p>
        </div>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>Username</label>
          <input v-model="form.username" type="text" placeholder="Enter username" required />
        </div>
        <div class="form-group">
          <label>Password</label>
          <input v-model="form.password" type="password" placeholder="Enter password" required />
        </div>
        <div v-if="error" class="error-msg">{{ error }}</div>
        <button type="submit" class="btn btn-primary login-btn" :disabled="loading">
          {{ loading ? 'Signing in...' : 'Sign In' }}
        </button>
      </form>

      <p class="hint">Students: scan the QR code your teacher provided</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const auth = useAuthStore()
const form = ref({ username: '', password: '' })
const error = ref('')
const loading = ref(false)

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    const data = await auth.login(form.value.username, form.value.password)
    if (data.role === 'ADMIN') router.push('/admin')
    else router.push('/teacher')
  } catch (e) {
    error.value = e.response?.data?.error || 'Login failed. Check your credentials.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4f46e5 0%, #06b6d4 100%);
}

.login-card {
  background: white;
  border-radius: 24px;
  padding: 48px 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 24px 64px rgba(0,0,0,0.2);
}

.logo {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 36px;
}
.logo-icon { font-size: 3rem; }
.logo h1 { font-size: 1.8rem; font-weight: 900; color: #4f46e5; }
.logo p { color: #64748b; font-size: 0.85rem; }

.login-btn { width: 100%; justify-content: center; padding: 14px; font-size: 1rem; border-radius: 12px; }
.login-btn:disabled { opacity: 0.7; cursor: not-allowed; }

.error-msg {
  background: #fee2e2;
  color: #991b1b;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 0.9rem;
  margin-bottom: 12px;
}

.hint {
  text-align: center;
  color: #94a3b8;
  font-size: 0.82rem;
  margin-top: 24px;
}
</style>
