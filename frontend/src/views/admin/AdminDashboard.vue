<template>
  <div class="dashboard">
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="sidebar-logo">
        <span>🛡️</span>
        <span>MkSafeNet</span>
      </div>
      <nav>
        <button :class="{ active: tab === 'stats' }" @click="tab = 'stats'">📊 Statistics</button>
        <button :class="{ active: tab === 'schools' }" @click="tab = 'schools'">🏫 Schools</button>
        <button :class="{ active: tab === 'teachers' }" @click="tab = 'teachers'">👩‍🏫 Teachers</button>
      </nav>
      <div class="sidebar-footer">
        <div class="user-info">
          <span class="user-avatar">👤</span>
          <div>
            <div class="user-name">{{ auth.displayName }}</div>
            <div class="user-role">Administrator</div>
          </div>
        </div>
        <button class="logout-btn" @click="logout">Sign Out</button>
      </div>
    </aside>

    <!-- Main -->
    <main class="main-content">
      <!-- Statistics Tab -->
      <div v-if="tab === 'stats'">
        <h2 class="page-title">Global Statistics</h2>
        <div v-if="loadingStats" class="loading">Loading...</div>
        <div v-else-if="stats">
          <!-- KPI Cards -->
          <div class="kpi-grid">
            <div class="kpi-card">
              <div class="kpi-icon">🏫</div>
              <div class="kpi-num">{{ stats.totalSchools }}</div>
              <div class="kpi-label">Schools</div>
            </div>
            <div class="kpi-card">
              <div class="kpi-icon">👥</div>
              <div class="kpi-num">{{ stats.totalStudents }}</div>
              <div class="kpi-label">Total Students</div>
            </div>
            <div class="kpi-card">
              <div class="kpi-icon">✅</div>
              <div class="kpi-num">{{ stats.completionRate }}%</div>
              <div class="kpi-label">Completion Rate</div>
            </div>
            <div class="kpi-card">
              <div class="kpi-icon">⭐</div>
              <div class="kpi-num">{{ stats.averageScore }}</div>
              <div class="kpi-label">Avg Score / 100</div>
            </div>
          </div>

          <!-- School Performance -->
          <div class="card mt-24">
            <h3 class="section-title">School Performance</h3>
            <table class="data-table">
              <thead>
                <tr>
                  <th>School</th>
                  <th>City</th>
                  <th>Students</th>
                  <th>Completed</th>
                  <th>Completion</th>
                  <th>Avg Score</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="s in stats.schoolStats" :key="s.schoolId" class="clickable" @click="viewSchool(s.schoolId)">
                  <td class="bold">{{ s.schoolName }}</td>
                  <td>{{ s.city }}</td>
                  <td>{{ s.totalStudents }}</td>
                  <td>{{ s.completedStudents }}</td>
                  <td>
                    <div class="mini-bar">
                      <div class="mini-fill" :style="{ width: s.completionRate + '%' }"></div>
                    </div>
                    {{ s.completionRate }}%
                  </td>
                  <td>
                    <span class="score-tag" :class="scoreClass(s.averageScore)">{{ s.averageScore }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Scenario Difficulty -->
          <div class="card mt-24">
            <h3 class="section-title">Scenario Difficulty (% correct answers)</h3>
            <div class="scenario-bars">
              <div v-for="sc in stats.scenarioStats" :key="sc.scenarioId" class="scenario-bar-row">
                <span class="sc-label">{{ sc.scenarioId }}. {{ sc.title }}</span>
                <div class="sc-bar-wrap">
                  <div class="sc-bar" :style="{ width: sc.successRate + '%' }"
                       :class="{ 'bar-red': sc.successRate < 50, 'bar-yellow': sc.successRate < 75 && sc.successRate >= 50, 'bar-green': sc.successRate >= 75 }">
                  </div>
                </div>
                <span class="sc-pct">{{ sc.successRate }}%</span>
                <span class="sc-total">({{ sc.totalAnswers }} answered)</span>
              </div>
            </div>
          </div>

          <!-- School Detail Modal -->
          <div v-if="schoolDetail" class="modal-overlay" @click.self="schoolDetail = null">
            <div class="modal-box">
              <div class="modal-header">
                <h3>🏫 {{ schoolDetail.school.name }}</h3>
                <button class="close-btn" @click="schoolDetail = null">✕</button>
              </div>
              <div class="modal-body">
                <table class="data-table">
                  <thead>
                    <tr><th>Student</th><th>Session</th><th>Score</th><th>Status</th><th>Completed</th></tr>
                  </thead>
                  <tbody>
                    <tr v-for="s in schoolDetail.students" :key="s.id">
                      <td class="bold">{{ s.name }}</td>
                      <td>{{ s.sessionName }}</td>
                      <td><span class="score-tag" :class="scoreClass(s.score)">{{ s.score }}</span></td>
                      <td>
                        <span class="badge" :class="s.completed ? 'badge-green' : 'badge-yellow'">
                          {{ s.completed ? 'Done' : 'In Progress' }}
                        </span>
                      </td>
                      <td>{{ s.completedAt ? fmtDate(s.completedAt) : '-' }}</td>
                    </tr>
                    <tr v-if="!schoolDetail.students.length">
                      <td colspan="5" class="empty-row">No students yet</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Schools Tab -->
      <div v-if="tab === 'schools'">
        <div class="tab-header">
          <h2 class="page-title">Schools</h2>
          <button class="btn btn-primary" @click="showAddSchool = true">+ Add School</button>
        </div>
        <div class="card mt-16">
          <table class="data-table">
            <thead><tr><th>Name</th><th>City</th><th>Address</th><th>Added</th></tr></thead>
            <tbody>
              <tr v-for="s in schools" :key="s.id">
                <td class="bold">{{ s.name }}</td>
                <td>{{ s.city || '-' }}</td>
                <td>{{ s.address || '-' }}</td>
                <td>{{ fmtDate(s.createdAt) }}</td>
              </tr>
              <tr v-if="!schools.length"><td colspan="4" class="empty-row">No schools added yet</td></tr>
            </tbody>
          </table>
        </div>

        <!-- Add School Modal -->
        <div v-if="showAddSchool" class="modal-overlay" @click.self="showAddSchool = false">
          <div class="modal-box small-modal">
            <div class="modal-header">
              <h3>Add School</h3>
              <button class="close-btn" @click="showAddSchool = false">✕</button>
            </div>
            <div class="modal-body">
              <div class="form-group"><label>School Name *</label><input v-model="newSchool.name" /></div>
              <div class="form-group"><label>City</label><input v-model="newSchool.city" /></div>
              <div class="form-group"><label>Address</label><input v-model="newSchool.address" /></div>
              <div v-if="schoolError" class="error-msg">{{ schoolError }}</div>
              <button class="btn btn-primary" style="width:100%" @click="addSchool">Add School</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Teachers Tab -->
      <div v-if="tab === 'teachers'">
        <div class="tab-header">
          <h2 class="page-title">Teachers</h2>
          <button class="btn btn-primary" @click="showAddTeacher = true">+ Add Teacher</button>
        </div>
        <div class="card mt-16">
          <table class="data-table">
            <thead><tr><th>Name</th><th>Username</th><th>School</th><th>Added</th></tr></thead>
            <tbody>
              <tr v-for="t in teachers" :key="t.id">
                <td class="bold">{{ t.displayName }}</td>
                <td>{{ t.username }}</td>
                <td>{{ t.schoolName || '-' }}</td>
                <td>{{ fmtDate(t.createdAt) }}</td>
              </tr>
              <tr v-if="!teachers.length"><td colspan="4" class="empty-row">No teachers yet</td></tr>
            </tbody>
          </table>
        </div>

        <!-- Add Teacher Modal -->
        <div v-if="showAddTeacher" class="modal-overlay" @click.self="showAddTeacher = false">
          <div class="modal-box small-modal">
            <div class="modal-header">
              <h3>Add Teacher</h3>
              <button class="close-btn" @click="showAddTeacher = false">✕</button>
            </div>
            <div class="modal-body">
              <div class="form-group"><label>Full Name *</label><input v-model="newTeacher.displayName" /></div>
              <div class="form-group"><label>Username *</label><input v-model="newTeacher.username" /></div>
              <div class="form-group"><label>Password *</label><input v-model="newTeacher.password" type="password" /></div>
              <div class="form-group">
                <label>School *</label>
                <select v-model="newTeacher.schoolId">
                  <option value="">Select school...</option>
                  <option v-for="s in schools" :key="s.id" :value="s.id">{{ s.name }}</option>
                </select>
              </div>
              <div v-if="teacherError" class="error-msg">{{ teacherError }}</div>
              <button class="btn btn-primary" style="width:100%" @click="addTeacher">Add Teacher</button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth.js'
import api from '../../api/index.js'

const router = useRouter()
const auth = useAuthStore()
const tab = ref('stats')

const stats = ref(null)
const loadingStats = ref(false)
const schoolDetail = ref(null)

const schools = ref([])
const showAddSchool = ref(false)
const newSchool = ref({ name: '', city: '', address: '' })
const schoolError = ref('')

const teachers = ref([])
const showAddTeacher = ref(false)
const newTeacher = ref({ displayName: '', username: '', password: '', schoolId: '' })
const teacherError = ref('')

onMounted(() => {
  loadStats()
  loadSchools()
  loadTeachers()
})

async function loadStats() {
  loadingStats.value = true
  try { stats.value = (await api.get('/admin/stats')).data }
  finally { loadingStats.value = false }
}
async function loadSchools() {
  schools.value = (await api.get('/admin/schools')).data
}
async function loadTeachers() {
  teachers.value = (await api.get('/admin/teachers')).data
}

async function viewSchool(id) {
  schoolDetail.value = (await api.get(`/admin/schools/${id}`)).data
}

async function addSchool() {
  schoolError.value = ''
  if (!newSchool.value.name) { schoolError.value = 'Name is required'; return }
  try {
    await api.post('/admin/schools', newSchool.value)
    showAddSchool.value = false
    newSchool.value = { name: '', city: '', address: '' }
    await loadSchools()
    await loadStats()
  } catch (e) { schoolError.value = e.response?.data?.error || 'Failed' }
}

async function addTeacher() {
  teacherError.value = ''
  if (!newTeacher.value.displayName || !newTeacher.value.username || !newTeacher.value.password || !newTeacher.value.schoolId) {
    teacherError.value = 'All fields are required'
    return
  }
  try {
    await api.post('/admin/teachers', newTeacher.value)
    showAddTeacher.value = false
    newTeacher.value = { displayName: '', username: '', password: '', schoolId: '' }
    await loadTeachers()
  } catch (e) { teacherError.value = e.response?.data?.error || 'Failed' }
}

function logout() { auth.logout(); router.push('/login') }
function scoreClass(s) {
  if (s >= 80) return 'score-green'
  if (s >= 60) return 'score-yellow'
  return 'score-red'
}
function fmtDate(d) {
  if (!d) return '-'
  return new Date(d).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
}
</script>

<style scoped>
.dashboard { display: flex; min-height: 100vh; background: #f0f4ff; }

.sidebar {
  width: 240px; flex-shrink: 0;
  background: white;
  border-right: 2px solid #e2e8f0;
  display: flex; flex-direction: column;
  padding: 24px 16px;
  position: sticky; top: 0; height: 100vh;
}
.sidebar-logo {
  display: flex; align-items: center; gap: 10px;
  font-size: 1.25rem; font-weight: 900; color: #4f46e5;
  margin-bottom: 32px; padding: 0 8px;
}
.sidebar-logo span:first-child { font-size: 1.5rem; }

nav { display: flex; flex-direction: column; gap: 4px; flex: 1; }
nav button {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 14px; border-radius: 10px; border: none;
  background: transparent; text-align: left;
  font-family: inherit; font-size: 0.93rem; font-weight: 700;
  color: #64748b; cursor: pointer; transition: all 0.2s;
}
nav button:hover { background: #f1f5f9; color: #4f46e5; }
nav button.active { background: #eef2ff; color: #4f46e5; }

.sidebar-footer { border-top: 1px solid #e2e8f0; padding-top: 16px; }
.user-info { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.user-avatar { font-size: 1.8rem; }
.user-name { font-weight: 800; font-size: 0.9rem; }
.user-role { font-size: 0.75rem; color: #64748b; }
.logout-btn { width: 100%; padding: 8px; border-radius: 8px; border: 2px solid #e2e8f0; background: white; font-family: inherit; font-size: 0.85rem; font-weight: 700; color: #64748b; cursor: pointer; }
.logout-btn:hover { background: #fee2e2; border-color: #ef4444; color: #ef4444; }

.main-content { flex: 1; padding: 32px; overflow-y: auto; }
.page-title { font-size: 1.6rem; font-weight: 900; margin-bottom: 24px; }
.tab-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0; }
.mt-16 { margin-top: 16px; }
.mt-24 { margin-top: 24px; }

.kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.kpi-card { background: white; border-radius: 16px; padding: 24px; text-align: center; box-shadow: 0 2px 12px rgba(79,70,229,0.08); }
.kpi-icon { font-size: 2rem; margin-bottom: 8px; }
.kpi-num { font-size: 2.2rem; font-weight: 900; color: #4f46e5; }
.kpi-label { font-size: 0.82rem; color: #64748b; font-weight: 700; margin-top: 4px; }

.section-title { font-size: 1rem; font-weight: 800; margin-bottom: 16px; }

.data-table { width: 100%; border-collapse: collapse; }
.data-table th { text-align: left; padding: 10px 12px; font-size: 0.8rem; color: #64748b; font-weight: 800; border-bottom: 2px solid #f1f5f9; text-transform: uppercase; letter-spacing: 0.05em; }
.data-table td { padding: 12px; font-size: 0.9rem; border-bottom: 1px solid #f1f5f9; }
.data-table .bold { font-weight: 700; }
.data-table tr.clickable:hover td { background: #f8faff; cursor: pointer; }
.empty-row { text-align: center; color: #94a3b8; padding: 32px !important; }

.mini-bar { display: inline-block; width: 80px; height: 6px; background: #f1f5f9; border-radius: 99px; margin-right: 6px; vertical-align: middle; }
.mini-fill { height: 100%; background: linear-gradient(90deg, #4f46e5, #06b6d4); border-radius: 99px; }

.score-tag { display: inline-block; padding: 2px 10px; border-radius: 99px; font-weight: 800; font-size: 0.85rem; }
.score-green { background: #d1fae5; color: #065f46; }
.score-yellow { background: #fef3c7; color: #92400e; }
.score-red { background: #fee2e2; color: #991b1b; }

.scenario-bars { display: flex; flex-direction: column; gap: 14px; }
.scenario-bar-row { display: flex; align-items: center; gap: 12px; }
.sc-label { min-width: 220px; font-size: 0.88rem; font-weight: 600; }
.sc-bar-wrap { flex: 1; height: 10px; background: #f1f5f9; border-radius: 99px; overflow: hidden; }
.sc-bar { height: 100%; border-radius: 99px; transition: width 0.8s ease; }
.bar-red { background: #ef4444; }
.bar-yellow { background: #f59e0b; }
.bar-green { background: #10b981; }
.sc-pct { min-width: 42px; font-weight: 800; font-size: 0.88rem; }
.sc-total { font-size: 0.78rem; color: #94a3b8; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal-box { background: white; border-radius: 20px; max-width: 800px; width: calc(100% - 48px); max-height: 85vh; overflow: hidden; display: flex; flex-direction: column; }
.small-modal { max-width: 440px; }
.modal-header { display: flex; align-items: center; justify-content: space-between; padding: 20px 24px; border-bottom: 2px solid #f1f5f9; }
.modal-header h3 { font-size: 1.1rem; font-weight: 900; }
.close-btn { background: none; border: none; font-size: 1.1rem; cursor: pointer; color: #94a3b8; }
.modal-body { padding: 20px 24px; overflow-y: auto; }

.error-msg { background: #fee2e2; color: #991b1b; padding: 10px 14px; border-radius: 10px; font-size: 0.9rem; margin-bottom: 12px; }

.loading { padding: 40px; text-align: center; color: #94a3b8; }
</style>
