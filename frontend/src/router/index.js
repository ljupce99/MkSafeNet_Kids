import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: () => import('../views/LoginView.vue') },
  { path: '/chat', component: () => import('../views/ChatView.vue') },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminDashboard.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/teacher',
    component: () => import('../views/teacher/TeacherDashboard.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (!to.meta.requiresAuth) return true
  const auth = useAuthStore()
  if (!auth.token) return '/login'
  if (to.meta.role && auth.role !== to.meta.role) return '/login'
  return true
})

export default router
