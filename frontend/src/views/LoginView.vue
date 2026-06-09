<template>
  <div class="login-wrap">
    <div class="login-card">
      <div class="logo">
        <img src="../assets/logo.png" alt="MkSafeNet Logo" class="logo-img" />
        <p>Вежбаме, учиме и препознаваме „Фишинг" напади.</p>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>Корисничко име</label>
          <input v-model="form.username" type="text" placeholder="Внеси корисничко име" required />
        </div>
        <div class="form-group">
          <label>Лозинка</label>
          <input v-model="form.password" type="password" placeholder="Внеси лозинка" required />
        </div>
        <div v-if="error" class="error-msg">{{ error }}</div>
        <button type="submit" class="btn btn-primary login-btn" :disabled="loading">
          {{ loading ? 'Се најавува...' : 'Најави се' }}
        </button>
      </form>
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
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 36px;
}
.logo-img { width: 350px; height: 100px; object-fit: contain; }
.logo p { color: #64748b; font-size: 0.85rem; text-align: center; }

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
