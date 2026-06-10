<template>
  <div class="dashboard">
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="sidebar-logo">
        <img src="../../assets/logo.png" alt="MkSafeNet Logo" class="sidebar-logo-img" />
      </div>
      <nav>
        <button :class="{ active: tab === 'stats' }" @click="tab = 'stats'">📊 Статистика</button>
        <button :class="{ active: tab === 'schools' }" @click="tab = 'schools'">🏫 Училишта</button>
        <button :class="{ active: tab === 'teachers' }" @click="tab = 'teachers'">👩‍🏫 Наставници</button>
        <button :class="{ active: tab === 'scenarios' }" @click="tab = 'scenarios'">🎮 Сценарија</button>
      </nav>
      <div class="sidebar-footer">
        <div class="user-info">
          <span class="user-avatar">👤</span>
          <div>
            <div class="user-name">{{ auth.displayName }}</div>
            <div class="user-role">Администратор</div>
          </div>
        </div>
        <button class="logout-btn" @click="logout">Одјави се</button>
      </div>
    </aside>

    <!-- Main -->
    <main class="main-content">
      <!-- Statistics Tab -->
      <div v-if="tab === 'stats'">
        <h2 class="page-title">Статистика</h2>
        <div v-if="loadingStats" class="loading">Се вчитува...</div>
        <div v-else-if="stats">
          <!-- KPI Cards -->
          <div class="kpi-grid">
            <div class="kpi-card">
              <div class="kpi-icon">🏫</div>
              <div class="kpi-num">{{ stats.totalSchools }}</div>
              <div class="kpi-label">Училишта</div>
            </div>
            <div class="kpi-card">
              <div class="kpi-icon">👥</div>
              <div class="kpi-num">{{ stats.totalStudents }}</div>
              <div class="kpi-label">Ученици</div>
            </div>
            <div class="kpi-card">
              <div class="kpi-icon">✅</div>
              <div class="kpi-num">{{ stats.completionRate }}%</div>
              <div class="kpi-label">Рата на завршеност</div>
            </div>
            <div class="kpi-card">
              <div class="kpi-icon">⭐</div>
              <div class="kpi-num">{{ stats.averageScore }}</div>
              <div class="kpi-label">Средна оценка / 100</div>
            </div>
          </div>

          <!-- School Performance -->
          <div class="card mt-24">
            <h3 class="section-title">Успех по училиште</h3>
            <table class="data-table">
              <thead>
                <tr>
                  <th>Име на училиште</th>
                  <th>град</th>
                  <th>ученици</th>
                  <th>завршени тестови</th>
                  <th>завршеност во проценти</th>
                  <th>средна оценка</th>
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
            <h3 class="section-title">Комплексност на сценарио (% точни одговори)</h3>
            <div class="scenario-bars">
              <div v-for="sc in stats.scenarioStats" :key="sc.scenarioId" class="scenario-bar-row">
                <span class="sc-label">{{ sc.scenarioId }}. {{ sc.title }}</span>
                <div class="sc-bar-wrap">
                  <div class="sc-bar" :style="{ width: sc.successRate + '%' }"
                       :class="{ 'bar-red': sc.successRate < 50, 'bar-yellow': sc.successRate < 75 && sc.successRate >= 50, 'bar-green': sc.successRate >= 75 }">
                  </div>
                </div>
                <span class="sc-pct">{{ sc.successRate }}%</span>
                <span class="sc-total">({{ sc.totalAnswers }} одговорени)</span>
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
                    <tr><th>ученик</th><th>сесија</th><th>оценка</th><th>статус</th><th>завршено</th></tr>
                  </thead>
                  <tbody>
                    <tr v-for="s in schoolDetail.students" :key="s.id">
                      <td class="bold">{{ s.name }}</td>
                      <td>{{ s.sessionName }}</td>
                      <td><span class="score-tag" :class="scoreClass(s.score)">{{ s.score }}</span></td>
                      <td>
                        <span class="badge" :class="s.completed ? 'badge-green' : 'badge-yellow'">
                          {{ s.completed ? 'Завршено' : 'Во изработка' }}
                        </span>
                      </td>
                      <td>{{ s.completedAt ? fmtDate(s.completedAt) : '-' }}</td>
                    </tr>
                    <tr v-if="!schoolDetail.students.length">
                      <td colspan="5" class="empty-row">нема ученици</td>
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
          <h2 class="page-title">Училишта</h2>
          <button class="btn btn-primary" @click="showAddSchool = true">+ додади училиште</button>
        </div>
        <div class="card mt-16">
          <table class="data-table">
            <thead><tr><th>Име</th><th>град</th><th>адреса</th><th>додадено на</th></tr></thead>
            <tbody>
              <tr v-for="s in schools" :key="s.id">
                <td class="bold">{{ s.name }}</td>
                <td>{{ s.city || '-' }}</td>
                <td>{{ s.address || '-' }}</td>
                <td>{{ fmtDate(s.createdAt) }}</td>
              </tr>
              <tr v-if="!schools.length"><td colspan="4" class="empty-row">нема училишта</td></tr>
            </tbody>
          </table>
        </div>

        <!-- Add School Modal -->
        <div v-if="showAddSchool" class="modal-overlay" @click.self="showAddSchool = false">
          <div class="modal-box small-modal">
            <div class="modal-header">
              <h3>Додади училиште</h3>
              <button class="close-btn" @click="showAddSchool = false">✕</button>
            </div>
            <div class="modal-body">
              <div class="form-group"><label>име на училипште *</label><input v-model="newSchool.name" /></div>
              <div class="form-group"><label>град</label><input v-model="newSchool.city" /></div>
              <div class="form-group"><label>адреса</label><input v-model="newSchool.address" /></div>
              <div v-if="schoolError" class="error-msg">{{ schoolError }}</div>
              <button class="btn btn-primary" style="width:100%" @click="addSchool">додади</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Teachers Tab -->
      <div v-if="tab === 'teachers'">
        <div class="tab-header">
          <h2 class="page-title">Наставници</h2>
          <button class="btn btn-primary" @click="showAddTeacher = true">+ додади наставник</button>
        </div>
        <div class="card mt-16">
          <table class="data-table">
            <thead><tr><th>име и презиме</th><th>корисничко име</th><th>училиште</th><th>додадено на</th></tr></thead>
            <tbody>
              <tr v-for="t in teachers" :key="t.id">
                <td class="bold">{{ t.displayName }}</td>
                <td>{{ t.username }}</td>
                <td>{{ t.schoolName || '-' }}</td>
                <td>{{ fmtDate(t.createdAt) }}</td>
              </tr>
              <tr v-if="!teachers.length"><td colspan="4" class="empty-row">нема наставници</td></tr>
            </tbody>
          </table>
        </div>

       <!-- Add Teacher Modal -->
         <div v-if="showAddTeacher" class="modal-overlay" @click.self="showAddTeacher = false">
           <div class="modal-box small-modal">
             <div class="modal-header">
               <h3>Додади наставник</h3>
               <button class="close-btn" @click="showAddTeacher = false">✕</button>
             </div>
             <div class="modal-body">
               <div class="form-group"><label>Име и презиме *</label><input v-model="newTeacher.displayName" /></div>
               <div class="form-group"><label>Корисничко име *</label><input v-model="newTeacher.username" /></div>
               <div class="form-group"><label>Лозинка *</label><input v-model="newTeacher.password" type="password" /></div>
               <div class="form-group">
                 <label>Училиште *</label>
                 <select v-model="newTeacher.schoolId">
                   <option value="">Одбери училиште...</option>
                   <option v-for="s in schools" :key="s.id" :value="s.id">{{ s.name }}</option>
                 </select>
               </div>
               <div v-if="teacherError" class="error-msg">{{ teacherError }}</div>
               <button class="btn btn-primary" style="width:100%" @click="addTeacher">додади</button>
             </div>
           </div>
         </div>
       </div>

       <!-- Scenarios Tab -->
       <div v-if="tab === 'scenarios'">
         <div class="tab-header">
           <h2 class="page-title">Сценарија</h2>
           <button class="btn btn-primary" @click="openAddScenario">+ Додади сценарио</button>
         </div>
         <div class="card mt-16">
           <table class="data-table">
             <thead><tr><th>ID</th><th>наслов</th><th>тип</th><th>поени</th><th>прашање</th><th>опции</th></tr></thead>
             <tbody>
               <tr v-for="s in scenarios" :key="s.id">
                 <td class="bold">{{ s.id }}</td>
                 <td>{{ s.title }}</td>
                 <td><span class="badge badge-blue">тип {{ s.typeOfScenario }}</span></td>
                 <td>{{ s.points }}</td>
                 <td class="truncate">{{ s.question }}</td>
                 <td>
                   <button class="btn-small btn-edit" @click="editScenario(s)">промени</button>
                   <button class="btn-small btn-delete" @click="deleteScenario(s.id)">избриши</button>
                 </td>
               </tr>
               <tr v-if="!scenarios.length"><td colspan="6" class="empty-row">нема сценарија</td></tr>
             </tbody>
           </table>
         </div>

         <!-- Add/Edit Scenario Modal -->
         <div v-if="showScenarioModal" class="modal-overlay" @click.self="closeScenarioModal">
           <div class="modal-box scenario-modal">
             <div class="modal-header">
               <h3>{{ editingScenarioId ? 'Edit Scenario' : 'Add Scenario' }}</h3>
               <button class="close-btn" @click="closeScenarioModal">✕</button>
             </div>
              <div class="modal-body">
                <div class="form-group"><label>Наслов *</label><input v-model="currentScenario.title" /></div>
                <div class="form-group"><label>Тип *</label><input v-model.number="currentScenario.typeOfScenario" type="number" min="1" max="5" /></div>
                <div class="form-group"><label>Поени *</label><input v-model.number="currentScenario.points" type="number" min="0" /></div>
                <div class="form-group"><label>Подесување на пораки (type\text, последната линија завршува со \delayMs)</label><textarea v-model="currentScenario.setupMessagesText" placeholder="bot\Прва линија на порака&#10;Втора линија\1000"></textarea></div>
                <div class="form-group"><label>Прашање *</label><textarea v-model="currentScenario.question"></textarea></div>
                <div class="form-group"><label>Опции (key\text по ред)</label><textarea v-model="currentScenario.optionsText" placeholder="A\прва опција&#10;B\втора опција&#10;C\трета опција"></textarea></div>
                <div class="form-group"><label>Точни одговори (секој одговор во нов ред)</label><textarea v-model="currentScenario.correctAnswersText" placeholder="A&#10;B"></textarea></div>
                <div class="form-group"><label>Објансување за точен одговор*</label><textarea v-model="currentScenario.correctExplanation"></textarea></div>
                <div class="form-group"><label>Објаснување за неточен одговор*</label><textarea v-model="currentScenario.wrongExplanation"></textarea></div>
                <div class="form-group"><label>Тип на последица</label><input v-model="currentScenario.consequenceType" placeholder="пример: ACCOUNT_HACKED" /></div>
                <div class="form-group"><label>Порака од последица (type\text, последната линија завршува со \delayMs)</label><textarea v-model="currentScenario.consequenceMessagesText" placeholder="последица\Го притисна!&#10;Твојата сметка е компромитирана\1500"></textarea></div>
                <div v-if="scenarioError" class="error-msg">{{ scenarioError }}</div>
                <button class="btn btn-primary" style="width:100%" @click="saveScenario">{{ editingScenarioId ? 'промени' : 'додади' }}</button>
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

const scenarios = ref([])
const showScenarioModal = ref(false)
const editingScenarioId = ref(null)
const scenarioError = ref('')
const currentScenario = ref({
  title: '',
  typeOfScenario: 1,
  points: 20,
  question: '',
  correctExplanation: '',
  wrongExplanation: '',
  consequenceType: '',
  setupMessagesText: '',
  optionsText: '',
  correctAnswersText: '',
  consequenceMessagesText: ''
})

onMounted(() => {
  loadStats()
  loadSchools()
  loadTeachers()
  loadScenarios()
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

async function loadScenarios() {
  scenarios.value = (await api.get('/admin/scenarios')).data
  console.log(scenarios.value);
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

function openAddScenario() {
  editingScenarioId.value = null
  currentScenario.value = {
    title: '',
    typeOfScenario: 1,
    points: 20,
    question: '',
    correctExplanation: '',
    wrongExplanation: '',
    consequenceType: '',
    setupMessagesText: '',
    optionsText: '',
    correctAnswersText: '',
    consequenceMessagesText: ''
  }
  scenarioError.value = ''
  showScenarioModal.value = true
}

function editScenario(scenario) {
  editingScenarioId.value = scenario.id

  // Normalize any malformed incoming message arrays (older saved scenarios) and
  // convert messages to multiline-safe blocks:
  // type\first line
  // middle lines
  // last line\delayMs
  const setupMessagesClean = normalizeIncomingMessageArray(scenario.setupMessages || [])
  const setupMessagesText = formatMessageRows(setupMessagesClean)

  // Convert options array to text format (key\text per line)
  const optionsText = (scenario.options || [])
    .map(opt => `${opt.key}\\${opt.text}`)
    .join('\n')

  // Convert correctAnswers to text format (one per line)
  const correctAnswersText = Array.from(scenario.correctAnswers || []).join('\n')

  // Convert consequence messages using the same multiline-safe block format.
  const consequenceMessagesClean = normalizeIncomingMessageArray(scenario.consequenceMessages || [])
  const consequenceMessagesText = formatMessageRows(consequenceMessagesClean)

  currentScenario.value = {
    ...scenario,
    setupMessagesText,
    optionsText,
    correctAnswersText,
    consequenceMessagesText
  }
  scenarioError.value = ''
  showScenarioModal.value = true
}

async function saveScenario() {
  scenarioError.value = ''
  if (!currentScenario.value.title || !currentScenario.value.question) {
    scenarioError.value = 'Наслов и прашање полињата се задолжителни!'
    return
  }

  try {
    // Parse multiline-safe message blocks.
    const setupMessages = parseMessageRows(currentScenario.value.setupMessagesText)

    // Parse options from text format (key\text per line)
    const options = currentScenario.value.optionsText
      .split('\n')
      .map(line => line.trim())
      .filter(line => line.length > 0)
      .map(line => {
        const [key, text] = line.split('\\').map(s => s.trim())
        return { key, text }
      })

    // Parse correct answers from text format (one per line)
    const correctAnswers = currentScenario.value.correctAnswersText
      .split('\n')
      .map(line => line.trim())
      .filter(line => line.length > 0)

    // Parse consequence messages using the same multiline-safe logic.
    const consequenceMessages = parseMessageRows(currentScenario.value.consequenceMessagesText)

    // Build payload
    const payload = {
      title: currentScenario.value.title,
      typeOfScenario: currentScenario.value.typeOfScenario,
      points: currentScenario.value.points,
      question: currentScenario.value.question,
      correctExplanation: currentScenario.value.correctExplanation,
      wrongExplanation: currentScenario.value.wrongExplanation,
      consequenceType: currentScenario.value.consequenceType,
      setupMessages: setupMessages,
      options: options,
      correctAnswers: correctAnswers,
      consequenceMessages: consequenceMessages
    }

    if (editingScenarioId.value) {
      await api.put(`/admin/scenarios/${editingScenarioId.value}`, payload)
    } else {
      await api.post('/admin/scenarios', payload)
    }
    closeScenarioModal()
    await loadScenarios()
  } catch (e) { scenarioError.value = e.response?.data?.error || 'Грешка во зачувување на сценарио' }
}

async function deleteScenario(id) {
  if (!confirm('Дали сте сигурни?')) return
  try {
    await api.delete(`/admin/scenarios/${id}`)
    await loadScenarios()
  } catch (e) { alert('Грешка во бришењето!: ' + (e.response?.data?.error || 'непозната грешка')) }
}

function closeScenarioModal() {
  showScenarioModal.value = false
  editingScenarioId.value = null
}

function formatMessageRows(messages) {
  return (messages || [])
      .map((msg) => {
        const type = String(msg?.type || '').trim()
        const delay = Number.parseInt(msg?.delayMs, 10)
        const delayStr = Number.isFinite(delay) ? delay : 0
        const text = String(msg?.text || '')

        // Directly wrap the entire block. This gracefully formats both
        // single-line and multi-line strings cleanly without extra loops.
        return `${type}\\${text}\\${delayStr}`
      })
      .join('\n')
}

function parseMessageRows(rawText) {
  const lines = String(rawText || '').replace(/\r/g, '').split('\n')
  const blocks = []
  let currentBlock = null

  // Match strict allowed message types at the start of a line
  const typePrefixRegex = /^(bot|system|user|consequence|success)\\(.*)$/

  // Phase 1: Group lines into cohesive type blocks
  for (const line of lines) {
    const match = typePrefixRegex.exec(line)

    if (match) {
      if (currentBlock) {
        blocks.push(currentBlock)
      }
      currentBlock = {
        type: match[1],
        lines: [match[2]]
      }
    } else {
      if (currentBlock) {
        currentBlock.lines.push(line)
      }
    }
  }

  if (currentBlock) {
    blocks.push(currentBlock)
  }

  // Phase 2: Isolate trailing delays from the completed text blocks
  return blocks.map(block => {
    const fullText = block.lines.join('\n')
    const delayMatch = /\\(\d+)$/.exec(fullText)

    let text = fullText
    let delayMs = 0

    if (delayMatch) {
      delayMs = Number.parseInt(delayMatch[1], 10) || 0
      // Strip out the matched delay string from the end of the text
      text = fullText.substring(0, fullText.length - delayMatch[0].length)
    }

    return {
      type: block.type,
      text: text,
      delayMs: delayMs
    }
  })
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

// Reconstruct incoming message arrays that were mis-saved as many single-line
// objects where the `type` field contains the actual line text. This function
// merges continuation lines into the previous message and extracts numeric
// delay values when they appear on their own line or at the end.
function normalizeIncomingMessageArray(list) {
  const allowed = new Set(['bot', 'system', 'success', 'user', 'consequence'])
  const out = []
  for (const raw of (list || [])) {
    const msg = raw || {}
    const t = String(msg.type || '').trim()
    const hasText = msg.text !== null && msg.text !== undefined

    if (allowed.has(t)) {
      out.push({ type: t, text: hasText ? String(msg.text) : '', delayMs: Number(msg.delayMs) || 0, icon: msg.icon || null })
      continue
    }

    if (!out.length) {
      if (hasText && /^\d+$/.test(String(msg.text).trim())) {
        out.push({ type: 'system', text: t, delayMs: parseInt(msg.text, 10) || 0, icon: msg.icon || null })
      } else {
        out.push({ type: 'system', text: t + (hasText ? '\n' + String(msg.text) : ''), delayMs: 0, icon: msg.icon || null })
      }
      continue
    }

    const prev = out[out.length - 1]
    if (hasText && /^\d+$/.test(String(msg.text).trim()) && (!prev.delayMs || prev.delayMs === 0)) {
      prev.delayMs = parseInt(msg.text, 10) || prev.delayMs
      continue
    }

    const append = hasText ? (t + (String(msg.text).trim() ? '\n' + String(msg.text) : '')) : t
    prev.text = (prev.text ? prev.text + '\n' : '') + append
  }
  return out
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
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 32px; padding: 0 8px;
}
.sidebar-logo-img { width: 180px; height: 80px; object-fit: contain; }

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

.btn { padding: 10px 16px; border-radius: 10px; border: none; font-family: inherit; font-size: 0.9rem; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-primary { background: #4f46e5; color: white; }
.btn-primary:hover { background: #4338ca; }

.btn-small { padding: 5px 10px; border-radius: 6px; border: none; font-family: inherit; font-size: 0.75rem; font-weight: 600; cursor: pointer; margin-right: 4px; transition: all 0.2s; }
.btn-edit { background: #dbeafe; color: #1e40af; }
.btn-edit:hover { background: #bfdbfe; }
.btn-delete { background: #fee2e2; color: #991b1b; }
.btn-delete:hover { background: #fecaca; }

.badge { display: inline-block; padding: 4px 10px; border-radius: 99px; font-weight: 700; font-size: 0.75rem; }
.badge-blue { background: #dbeafe; color: #1e40af; }
.badge-green { background: #d1fae5; color: #065f46; }
.badge-yellow { background: #fef3c7; color: #92400e; }

.truncate { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.scenario-modal { max-width: 700px; }

.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-weight: 700; font-size: 0.9rem; margin-bottom: 4px; color: #1e293b; }
.form-group input, .form-group select, .form-group textarea {
  width: 100%; padding: 10px; border: 2px solid #e2e8f0; border-radius: 10px;
  font-family: inherit; font-size: 0.9rem; transition: border-color 0.2s;
}
.form-group input:focus, .form-group select:focus, .form-group textarea:focus {
  outline: none; border-color: #4f46e5;
}
.form-group textarea { resize: vertical; min-height: 80px; }
</style>
