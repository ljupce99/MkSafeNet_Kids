<template>
  <div class="dashboard">
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="sidebar-logo">
        <img src="../../assets/logo.png" alt="MkSafeNet Logo" class="sidebar-logo-img" />
      </div>
      <nav>
        <button :class="{ active: tab === 'sessions' }" @click="tab = 'sessions'; detailSession = null">📋 Мои сесии</button>
        <button @click="certificateModalOpen = true">🎓 Создади сертификат</button>
        <button :class="{ active: tab === 'new' }" @click="tab = 'new'">➕ Нова сесија</button>
      </nav>
      <div class="sidebar-footer">
        <div class="user-info">
          <span class="user-avatar">👩‍🏫</span>
          <div>
            <div class="user-name">{{ auth.displayName }}</div>
            <div class="user-role">{{ auth.schoolName }}</div>
          </div>
        </div>
        <button class="logout-btn" @click="logout">Одјави се</button>
      </div>
    </aside>

    <!-- Main -->
    <main class="main-content">

      <!-- Session Detail View -->
      <div v-if="detailSession">
        <div class="back-row">
          <button class="btn btn-secondary btn-sm" @click="detailSession = null">← назад</button>
          <h2 class="page-title">{{ activeDetailSession.name }}</h2>
          <div class="session-status">
            <span class="badge" :class="activeDetailSession.active ? 'badge-green' : 'badge-red'">
              {{ activeDetailSession.active ? '● активно' : '○ неактивно' }}
            </span>
            <button class="btn btn-sm" :class="activeDetailSession.active ? 'btn-danger' : 'btn-success'"
                    @click="toggleSession(activeDetailSession.id, !activeDetailSession.active)">
              {{ activeDetailSession.active ? 'деактивирај' : 'активирај' }}
            </button>
          </div>
        </div>

        <!-- QR Code -->
        <div class="qr-row card">
          <div class="qr-left">
            <h3>QR Code за сесија</h3>
            <p>Сподели со учениците за да започне вежбата!</p>
            <code class="token-code">Token: {{ activeDetailSession.token }}</code>
          </div>
          <div class="qr-img-wrap">
            <img v-if="sessionQr" :src="'data:image/png;base64,' + sessionQr" class="qr-img" alt="Session QR code" />
            <button v-else class="btn btn-primary" @click="loadQr(activeDetailSession.id)">Прикажи QR код</button>
            <div v-if="sessionQr" class="qr-link-wrap">
              <div class="qr-url">{{ getSessionUrl(activeDetailSession.token) }}</div>
            </div>
          </div>
        </div>

        <!-- Stats -->
        <div class="kpi-mini">
          <div class="kpi-card">
            <div class="kpi-num">{{ activeDetailSession.totalStudents }}</div>
            <div class="kpi-label">Вклучени ученици</div>
          </div>
          <div class="kpi-card">
            <div class="kpi-num">{{ activeDetailSession.completedStudents }}</div>
            <div class="kpi-label">Завршени</div>
          </div>
          <div class="kpi-card">
            <div class="kpi-num">{{ activeDetailSession.averageScore }}</div>
            <div class="kpi-label">Средна оценка</div>
          </div>
        </div>

        <!-- Students Table -->
        <div class="card mt-16">
          <h3 class="section-title">Ученици</h3>
          <table class="data-table">
            <thead>
              <tr>
                <th>Име</th>
                <th>Поени</th>
                <th>Статус</th>
                <th>С1</th><th>С2</th><th>С3</th><th>С4</th><th>С5</th>
                <th>Завршено на</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="s in detailStudents" :key="s.id">
                <td class="bold">{{ s.name }}</td>
                <td>
                  <span class="score-tag" :class="scoreClass(s.score)">{{ s.score }}</span>
                </td>
                <td>
                  <span class="badge" :class="s.completed ? 'badge-green' : 'badge-yellow'">
                    {{ s.completed ? 'Завршено' : 'Во процес' }}
                  </span>
                </td>
                <td v-for="sc in 5" :key="sc">
                  {{ getResponse(s.responses, sc) }}
                </td>
                <td>{{ s.completedAt ? fmtDate(s.completedAt) : '-' }}</td>
              </tr>
              <tr v-if="!detailStudents.length">
                <td colspan="10" class="empty-row">Нема вклучени ученици</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Sessions List -->
      <div v-else-if="tab === 'sessions'">
        <h2 class="page-title">Мои сесии</h2>
        <div v-if="loading" class="loading">Се вчитува...</div>
        <div v-else-if="!sessions.length" class="empty-state">
          <div class="empty-icon">📋</div>
          <p>Нема сеции, направи една!</p>
          <button class="btn btn-primary" @click="tab = 'new'">Додади сесија</button>
        </div>
        <div v-else class="sessions-grid">
          <div v-for="s in sessions" :key="s.id" class="session-card" @click="openDetail(s.id)">
            <div class="session-card-top">
              <h3>{{ s.name }}</h3>
              <span class="badge" :class="s.active ? 'badge-green' : 'badge-red'">
                {{ s.active ? '● Активно' : '○ Неактивно' }}
              </span>
            </div>
            <div class="session-meta">
              <span>👥 {{ s.totalStudents }} ученици</span>
              <span>✅ {{ s.completedStudents }} завршено</span>
              <span>⭐ просек: {{ s.averageScore }}</span>
            </div>
            <div class="session-token">Токен: <code>{{ s.token }}</code></div>
            <div class="session-date">Направено на: {{ fmtDate(s.createdAt) }}</div>
          </div>
        </div>
      </div>

      <!-- New Session Tab -->
      <div v-else-if="tab === 'new'">
        <h2 class="page-title">Додади нова сесија</h2>
        <div class="card" style="max-width: 500px">
          <p class="create-hint">Додади сесија и QR code за твоето одделение. Учениците скенираат и започнуваат со вежбата</p>
          <div class="form-group">
            <label>Име на сесија</label>
            <input v-model="newSessionName" placeholder="e.g. клас/одделение 5A — April 2026" />
          </div>
          <div v-if="createError" class="error-msg">{{ createError }}</div>
          <button class="btn btn-primary create-btn" @click="createSession" :disabled="creating">
            {{ creating ? 'Се создава...' : '🔗 Додади сесија и QR код' }}
          </button>
        </div>

        <!-- Created Session Result -->
        <div v-if="createdSession" class="card mt-24 created-result">
          <h3>✅ Сесијата е направена!</h3>
          <p>Сподели QR код или URL со твоите ученици.</p>
          <div class="created-info">
            <div class="qr-center">
              <img :src="'data:image/png;base64,' + createdSession.qrCode" class="qr-img-lg" alt="Создаден QR код за сесија" />
              <div class="qr-url">{{ createdSession.url }}</div>
              <code class="token-lg">Токен: {{ createdSession.token }}</code>
            </div>
          </div>
          <button class="btn btn-secondary" @click="tab = 'sessions'; loadSessions()">Види ги сите сесии</button>
        </div>
      </div>
    </main>

    <!-- Certificate Modal -->
    <div v-if="certificateModalOpen" class="modal-backdrop" @click.self="closeCertificateModal">
      <div class="modal-card">
        <div class="modal-header">
          <h3>Генерирај сертификати</h3>
          <button class="modal-close" @click="closeCertificateModal">✕</button>
        </div>

        <p class="modal-text">Внеси го името што треба да биде на сертификатот.</p>

        <div class="form-group">
          <label>Име</label>
          <input
            v-model="certificateName"
            type="text"
            placeholder="пример: Петар Петровски"
            @keyup.enter="downloadCertificate"
          />
        </div>

        <div v-if="certificateError" class="error-msg">{{ certificateError }}</div>

        <div class="modal-actions">
          <button class="btn btn-secondary" @click="closeCertificateModal" :disabled="certificateLoading">Откажи</button>
          <button class="btn btn-primary" @click="downloadCertificate" :disabled="certificateLoading">
            {{ certificateLoading ? 'Се генерира...' : 'Download PDF' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth.js'
import api from '../../api/index.js'

const router = useRouter()
const auth = useAuthStore()
const tab = ref('sessions')
const sessions = ref([])
const loading = ref(false)
const detailSession = ref(null)
const sessionQr = ref(null)
const activeDetailSession = computed(() => detailSession.value?.session || {})
const detailStudents = computed(() => detailSession.value?.students || [])

const newSessionName = ref('')
const creating = ref(false)
const createError = ref('')
const createdSession = ref(null)

const certificateModalOpen = ref(false)
const certificateName = ref('')
const certificateLoading = ref(false)
const certificateError = ref('')

onMounted(() => loadSessions())

async function loadSessions() {
  loading.value = true
  try { sessions.value = (await api.get('/teacher/sessions')).data }
  finally { loading.value = false }
}

async function openDetail(id) {
  sessionQr.value = null
  detailSession.value = (await api.get(`/teacher/sessions/${id}`)).data
  tab.value = 'sessions'
}

async function loadQr(id) {
  const res = await api.get(`/teacher/sessions/${id}/qr`)
  sessionQr.value = res.data.qrCode
}

async function toggleSession(id, active) {
  await api.put(`/teacher/sessions/${id}/toggle`, { active })
  detailSession.value.session.active = active
}

async function createSession() {
  if (!newSessionName.value.trim()) { createError.value = 'Име на сесија е задолжително!'; return }
  creating.value = true
  createError.value = ''
  try {
    const res = await api.post('/teacher/sessions', { name: newSessionName.value.trim() })
    createdSession.value = res.data
    newSessionName.value = ''
  } catch (e) {
    createError.value = e.response?.data?.error || 'Неуспешно создавање на сесија'
  } finally {
    creating.value = false
  }
}

function getResponse(responses, scenarioId) {
  const r = responses?.find(x => x.scenarioId === scenarioId)
  if (!r) return '-'
  return r.correct ? '✅' : '❌'
}

function logout() { auth.logout(); router.push('/login') }

function closeCertificateModal() {
  certificateModalOpen.value = false
  certificateName.value = ''
  certificateError.value = ''
  certificateLoading.value = false
}

async function downloadCertificate() {
  const name = certificateName.value.trim()
  if (!name) {
    certificateError.value = 'Името е задолжително'
    return
  }

  certificateLoading.value = true
  certificateError.value = ''

  try {
    const res = await api.get('/certificates/download', {
      params: { name },
      responseType: 'blob'
    })

    const blob = new Blob([res.data], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')

    const disposition = res.headers?.['content-disposition'] || ''
    const match = disposition.match(/filename="?([^";]+)"?/i)
    const filename = match?.[1] || `certificate-${name.replace(/\s+/g, '_')}.pdf`

    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)

    closeCertificateModal()
  } catch (e) {
    certificateError.value = e.response?.data?.error || 'Неуспешно создавање сертификат'
  } finally {
    certificateLoading.value = false
  }
}

function scoreClass(s) {
  if (s >= 80) return 'score-green'
  if (s >= 60) return 'score-yellow'
  return 'score-red'
}
function fmtDate(d) {
  if (!d) return '-'
  return new Date(d).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
}
function getSessionUrl(token) {
  const baseUrl = window.location.origin
  return `${baseUrl}/chat?token=${token}`
}
</script>

<style scoped>
.dashboard { display: flex; min-height: 100vh; background: #f0f4ff; }

.sidebar {
  width: 240px; flex-shrink: 0;
  background: white; border-right: 2px solid #e2e8f0;
  display: flex; flex-direction: column; padding: 24px 16px;
  position: sticky; top: 0; height: 100vh;
}
.sidebar-logo { display: flex; align-items: center; justify-content: center; margin-bottom: 32px; padding: 0 8px; }
.sidebar-logo-img { width: 180px; height: 80px; object-fit: contain; }
nav { display: flex; flex-direction: column; gap: 4px; flex: 1; }
nav button { display: flex; align-items: center; gap: 10px; padding: 10px 14px; border-radius: 10px; border: none; background: transparent; text-align: left; font-family: inherit; font-size: 0.93rem; font-weight: 700; color: #64748b; cursor: pointer; transition: all 0.2s; }
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
.mt-16 { margin-top: 16px; }
.mt-24 { margin-top: 24px; }
.section-title { font-size: 1rem; font-weight: 800; margin-bottom: 16px; }

.back-row { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.back-row .page-title { margin-bottom: 0; flex: 1; }
.session-status { display: flex; align-items: center; gap: 10px; }

.qr-row { display: flex; align-items: center; gap: 32px; margin-bottom: 16px; }
.qr-left { flex: 1; }
.qr-left h3 { font-size: 1rem; font-weight: 800; margin-bottom: 4px; }
.qr-left p { color: #64748b; font-size: 0.88rem; margin-bottom: 12px; }
.token-code { background: #f1f5f9; padding: 6px 12px; border-radius: 8px; font-size: 0.85rem; font-family: monospace; }
.qr-img-wrap { flex-shrink: 0; display: flex; flex-direction: column; align-items: center; }
.qr-img { width: 140px; height: 140px; border-radius: 12px; border: 3px solid #e2e8f0; }
.qr-link-wrap { margin-top: 12px; text-align: center; }
.qr-url { font-size: 0.82rem; color: #64748b; word-break: break-all; background: #f1f5f9; padding: 8px 12px; border-radius: 8px; font-family: monospace; }

.kpi-mini { display: flex; gap: 16px; margin-bottom: 0; }
.kpi-card { background: white; border-radius: 14px; padding: 18px 24px; flex: 1; text-align: center; box-shadow: 0 2px 12px rgba(79,70,229,0.07); }
.kpi-num { font-size: 1.8rem; font-weight: 900; color: #4f46e5; }
.kpi-label { font-size: 0.8rem; color: #64748b; font-weight: 700; }

.sessions-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px; }
.session-card { background: white; border-radius: 16px; padding: 20px; cursor: pointer; border: 2px solid transparent; transition: all 0.2s; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.session-card:hover { border-color: #4f46e5; transform: translateY(-2px); box-shadow: 0 8px 24px rgba(79,70,229,0.15); }
.session-card-top { display: flex; align-items: flex-start; justify-content: space-between; gap: 8px; margin-bottom: 12px; }
.session-card h3 { font-size: 1rem; font-weight: 900; }
.session-meta { display: flex; gap: 12px; flex-wrap: wrap; font-size: 0.82rem; color: #64748b; font-weight: 600; margin-bottom: 8px; }
.session-token { font-size: 0.8rem; color: #94a3b8; margin-bottom: 4px; }
.session-token code { background: #f8faff; padding: 2px 6px; border-radius: 4px; font-size: 0.78rem; }
.session-date { font-size: 0.78rem; color: #cbd5e1; }

.empty-state { text-align: center; padding: 64px; }
.empty-icon { font-size: 4rem; margin-bottom: 16px; }
.empty-state p { color: #64748b; margin-bottom: 20px; font-size: 1rem; }

.create-hint { color: #64748b; font-size: 0.9rem; margin-bottom: 20px; }
.create-btn { width: 100%; justify-content: center; padding: 14px; font-size: 1rem; border-radius: 14px; margin-top: 4px; }
.create-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.created-result { text-align: center; }
.created-result h3 { font-size: 1.2rem; font-weight: 900; margin-bottom: 8px; color: #059669; }
.created-result p { color: #64748b; margin-bottom: 24px; }
.qr-center { display: flex; flex-direction: column; align-items: center; gap: 12px; margin-bottom: 24px; }
.qr-img-lg { width: 200px; height: 200px; border-radius: 16px; border: 3px solid #e2e8f0; }
.qr-url { font-size: 0.82rem; color: #64748b; word-break: break-all; }
.token-lg { background: #f1f5f9; padding: 8px 16px; border-radius: 10px; font-size: 1rem; }

.data-table { width: 100%; border-collapse: collapse; }
.data-table th { text-align: left; padding: 10px 12px; font-size: 0.8rem; color: #64748b; font-weight: 800; border-bottom: 2px solid #f1f5f9; text-transform: uppercase; letter-spacing: 0.05em; }
.data-table td { padding: 12px; font-size: 0.88rem; border-bottom: 1px solid #f1f5f9; }
.data-table .bold { font-weight: 700; }
.empty-row { text-align: center; color: #94a3b8; padding: 32px !important; }

.score-tag { display: inline-block; padding: 2px 10px; border-radius: 99px; font-weight: 800; font-size: 0.85rem; }
.score-green { background: #d1fae5; color: #065f46; }
.score-yellow { background: #fef3c7; color: #92400e; }
.score-red { background: #fee2e2; color: #991b1b; }

.error-msg { background: #fee2e2; color: #991b1b; padding: 10px 14px; border-radius: 10px; font-size: 0.9rem; margin-bottom: 12px; }
.loading { padding: 40px; text-align: center; color: #94a3b8; }

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-card {
  width: 100%;
  max-width: 460px;
  background: white;
  border-radius: 18px;
  padding: 20px;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.25);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 8px;
}

.modal-header h3 {
  font-size: 1.1rem;
  font-weight: 900;
}

.modal-close {
  border: none;
  background: #f1f5f9;
  color: #64748b;
  width: 36px;
  height: 36px;
  border-radius: 999px;
  cursor: pointer;
  font-size: 0.95rem;
}

.modal-close:hover { background: #e2e8f0; }

.modal-text {
  color: #64748b;
  font-size: 0.92rem;
  margin-bottom: 18px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}
</style>
