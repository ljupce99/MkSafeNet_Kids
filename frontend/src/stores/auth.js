import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api/index.js'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || null)
  const role = ref(localStorage.getItem('role') || null)
  const displayName = ref(localStorage.getItem('displayName') || null)
  const schoolId = ref(localStorage.getItem('schoolId') || null)
  const schoolName = ref(localStorage.getItem('schoolName') || null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(username, password) {
    const res = await api.post('/auth/login', { username, password })
    token.value = res.data.token
    role.value = res.data.role
    displayName.value = res.data.displayName
    schoolId.value = res.data.schoolId
    schoolName.value = res.data.schoolName
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('role', res.data.role)
    localStorage.setItem('displayName', res.data.displayName)
    localStorage.setItem('schoolId', res.data.schoolId)
    localStorage.setItem('schoolName', res.data.schoolName)
    return res.data
  }

  function getDashboardRouteByRole(userRole = role.value) {
    if (userRole === 'ADMIN') return '/admin'
    if (userRole === 'TEACHER') return '/teacher'
    return '/login'
  }

  function logout() {
    token.value = null
    role.value = null
    displayName.value = null
    schoolId.value = null
    schoolName.value = null
    localStorage.clear()
  }

  return {
    token,
    role,
    displayName,
    schoolId,
    schoolName,
    isLoggedIn,
    login,
    logout,
    getDashboardRouteByRole
  }
})
