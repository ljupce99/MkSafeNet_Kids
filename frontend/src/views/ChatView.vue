<template>
  <div class="chat-app">

    <!-- Header -->
    <div class="chat-header">
      <div class="bot-avatar">🤖</div>
      <div class="bot-info">
        <span class="bot-name">SafeBot</span>
        <span class="bot-status">{{ sessionInfo?.sessionName || 'Вежбаме, учиме и препознаваме „Фишинг" напади.' }} · {{ sessionInfo?.schoolName }}</span>
      </div>
      <div v-if="phase !== 'COMPLETE'" class="progress-wrap">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progressPct + '%' }"></div>
        </div>
        <span class="progress-label">{{ scenarioProgress }}</span>
      </div>
    </div>


    <!-- Join Screen -->
    <div v-if="phase === 'JOIN'" class="join-screen">
      <div class="join-card">
        <div class="join-logo">
          <img src="../assets/logo.png" alt="MkSafeNet Logo" class="logo-img" />
        </div>
        <h2>{{ sessionInfo?.sessionName || 'Phishing Challenge' }}</h2>
        <p>{{ sessionInfo?.schoolName }}</p>
        <br>
        <p class="welcome-text">Ќе научиш како да препознаеш измами на интернет и како да се заштитиш!</p>
        <div v-if="joinError" class="error-msg">{{ joinError }}</div>
        <div class="form-group">
          <label>Твоето име</label>
          <input v-model="studentName" type="text" placeholder="Внеси го твоето име" maxlength="40" @keyup.enter="startChat" />
        </div>
        <button class="btn btn-primary start-btn" @click="startChat" :disabled="!studentName.trim() || starting">
          {{ starting ? 'Започнува...' : '🚀 ЗАПОЧНИ' }}
        </button>
      </div>
    </div>

    <!-- Invalid Session -->
    <div v-if="phase === 'INVALID'" class="join-screen">
      <div class="join-card error-card">
        <div class="join-emoji">❌</div>
        <h2>Невалидна сесија</h2>
        <p>Овој QR код е истечен или невалиден. Прашај го наставникот за нов код.</p>
      </div>
    </div>

    <!-- Chat Area -->
    <div v-if="phase !== 'JOIN' && phase !== 'INVALID' && phase !== 'COMPLETE'" class="chat-body" ref="chatBody">
      <div class="chat-header">
        <div>Сценарио {{ currentScenarioId }} од 5</div>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progressPct + '%' }"></div>
        </div>
      </div>
      <div class="messages">
        <div v-for="(msg, i) in visibleMessages" :key="i"
             class="msg-wrap" :class="'msg-' + msg.type">
          <div v-if="msg.type === 'bot' || msg.type === 'success'" class="bot-bubble">
            <div class="bubble" :class="msg.type">{{ msg.text }}</div>
          </div>
          <div v-else-if="msg.type === 'system'" class="system-box">
            <pre class="email-display">{{ msg.text }}</pre>
          </div>
        </div>

        <div v-if="typing" class="msg-wrap msg-bot">
          <div class="bot-bubble">
            <div class="bubble typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- Question Area -->
      <div v-if="currentQuestion && !typing && !waitingForConsequence" class="question-area">
        <div class="question-text">{{ currentQuestion }}</div>
        <div class="options">
          <button v-for="opt in currentOptions" :key="opt.key"
                  class="option-btn"
                  :class="{ selected: selectedAnswer === opt.key, disabled: answered }"
                  @click="selectAnswer(opt.key)"
                  :disabled="answered">
            <span class="opt-key">{{ opt.key }}</span>
            <span class="opt-text">{{ opt.text }}</span>
          </button>
        </div>
        <button v-if="selectedAnswer && !answered"
                class="btn btn-primary submit-btn"
                @click="submitAnswer">
          Испрати одговор ✓
        </button>
      </div>
    </div>

    <!-- Consequence Modal -->
    <ConsequenceModal
      v-if="showConsequence"
      :type="consequenceType"
      :messages="consequenceMessages"
      @done="onConsequenceDone"
    />

    <!-- Completion Screen -->
    <div v-if="phase === 'COMPLETE'" class="complete-screen">
      <div class="complete-card">
        <div class="score-circle" :class="scoreClass">
          <span class="score-num">{{ finalScore }}</span>
          <span class="score-label">/ 100</span>
        </div>
        <div class="grade-badge">{{ grade }}</div>
        <h2>{{ finalScore >= 60 ? '🏆 Предизвикот е завршен!' : '📚 Продолжи со учење!' }}</h2>
        <p class="passed-text" :class="finalScore >= 60 ? 'text-success' : 'text-warn'">
          {{ finalScore >= 60 ? 'Положивте! Ти си одличен препознавач на „фишинг“ напади.' : 'Го заврши предизвикот!. Вежбањето се исплати.' }}
        </p>

        <div class="badges">
          <span v-for="badge in badges" :key="badge" class="badge-item">{{ badge }}</span>
        </div>

        <div class="results-table">
          <h3>Твоите резултати</h3>
          <div v-for="r in scenarioResults" :key="r.scenarioId" class="result-row">
            <span class="result-icon">{{ r.correct ? '✅' : '❌' }}</span>
            <span class="result-title">{{ r.scenarioTitle }}</span>
            <span class="result-pts">{{ r.pointsEarned }}/20 поени</span>
          </div>
        </div>

        <div class="complete-messages">
          <div v-for="(m, i) in visibleMessages" :key="i" class="complete-msg" :class="m.type">
            {{ m.text }}
          </div>
        </div>

        <p class="finish-note">Сега можеш да го затвориш прозорецот. Браво!</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api/index.js'
import ConsequenceModal from '../components/ConsequenceModal.vue'

const route = useRoute()
const token = computed(() => route.query.token)

const phase = ref('JOIN')
const sessionInfo = ref(null)
const studentName = ref('')
const studentId = ref(null)
const starting = ref(false)
const joinError = ref('')

const visibleMessages = ref([])
const typing = ref(false)
const currentQuestion = ref('')
const currentOptions = ref([])
const currentScenarioId = ref(0)
const selectedAnswer = ref('')
const answered = ref(false)
const waitingForConsequence = ref(false)

const showConsequence = ref(false)
const consequenceType = ref('')
const consequenceMessages = ref([])
const pendingNextData = ref(null)

const finalScore = ref(0)
const grade = ref('')
const badges = ref([])
const scenarioResults = ref([])

const chatBody = ref(null)

onMounted(async () => {
  if (!token.value) { phase.value = 'INVALID'; return }
  try {
    const res = await api.get(`/chat/session/${token.value}`)
    sessionInfo.value = res.data
  } catch {
    phase.value = 'INVALID'
  }
})

const progressPct = computed(() => {
  if (!currentScenarioId.value) return 0
  return Math.min(((currentScenarioId.value - 1) / 5) * 100, 100)
})

const scenarioProgress = computed(() => {
  if (!currentScenarioId.value) return ''
  return `Сценарио ${currentScenarioId.value} од 5`
})

const scoreClass = computed(() => {
  if (finalScore.value >= 80) return 'score-great'
  if (finalScore.value >= 60) return 'score-ok'
  return 'score-low'
})

async function startChat() {
  if (!studentName.value.trim()) return
  starting.value = true
  joinError.value = ''
  try {
    const res = await api.post('/chat/start', {
      sessionToken: token.value,
      studentName: studentName.value.trim()
    })
    studentId.value = res.data.studentId
    phase.value = 'SCENARIO'
    await displayMessages(res.data.messages, res.data)
  } catch (e) {
    joinError.value = e.response?.data?.error || 'Грешка. Обиди се повторно.'
  } finally {
    starting.value = false
  }
}

async function displayMessages(messages, responseData) {
  typing.value = true
  for (const msg of messages) {
    await delay(msg.delayMs > 0 ? Math.min(msg.delayMs, 800) : 200)
    visibleMessages.value.push(msg)
    await scrollBottom()
  }
  typing.value = false

  if (responseData && responseData.scenarioId) {
    currentScenarioId.value = responseData.scenarioId
    currentQuestion.value = responseData.question
    currentOptions.value = responseData.options || []
    selectedAnswer.value = ''
    answered.value = false
    waitingForConsequence.value = false
  }
}

function selectAnswer(key) {
  if (answered.value) return
  selectedAnswer.value = key
}

async function submitAnswer() {
  if (!selectedAnswer.value || answered.value) return
  answered.value = true
  currentQuestion.value = ''
  currentOptions.value = []

  const userMsg = { type: 'user', text: `Го одбрав: ${selectedAnswer.value}` }
  visibleMessages.value.push(userMsg)
  await scrollBottom()

  try {
    const res = await api.post('/chat/respond', {
      studentId: studentId.value,
      answer: selectedAnswer.value
    })

    if (res.data.phase === 'COMPLETE') {
      await handleComplete(res.data)
      return
    }

    if (!res.data.correct && res.data.consequenceMessages?.length) {
      waitingForConsequence.value = true
      consequenceType.value = res.data.consequenceType
      consequenceMessages.value = res.data.consequenceMessages
      pendingNextData.value = res.data
      showConsequence.value = true
    } else {
      await displayMessages(res.data.messages || [], res.data)
    }
  } catch (e) {
    console.error(e)
    answered.value = false
  }
}

async function onConsequenceDone() {
  showConsequence.value = false
  const data = pendingNextData.value
  pendingNextData.value = null
  await displayMessages(data.messages || [], data)
}

async function handleComplete(data) {
  if (!data.correct && data.consequenceMessages?.length) {
    consequenceType.value = data.consequenceType
    consequenceMessages.value = data.consequenceMessages
    showConsequence.value = true
    await new Promise(resolve => {
      const stop = setInterval(() => {
        if (!showConsequence.value) { clearInterval(stop); resolve() }
      }, 200)
    })
  }
  finalScore.value = data.score
  grade.value = data.grade
  badges.value = data.badges || []
  scenarioResults.value = data.scenarioResults || []
  visibleMessages.value = []
  phase.value = 'COMPLETE'
  if (data.messages) {
    for (const msg of data.messages) {
      await delay(Math.min(msg.delayMs > 0 ? msg.delayMs : 400, 800))
      visibleMessages.value.push(msg)
    }
  }
}

function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, Math.max(ms, 100)))
}

async function scrollBottom() {
  await nextTick()
  if (chatBody.value) {
    chatBody.value.scrollTop = chatBody.value.scrollHeight
  }
}
</script>

<style scoped>
.chat-app {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 780px;
  margin: 0 auto;
  background: #f8faff;
}
.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: white;
  border-bottom: 2px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.bot-avatar { font-size: 2.4rem; }
.bot-info { flex: 1; }
.bot-name { display: block; font-weight: 900; font-size: 1.1rem; color: #4f46e5; }
.bot-status { font-size: 0.78rem; color: #64748b; }

.progress-wrap { display: flex; align-items: center; gap: 8px; }
.progress-bar { width: 120px; height: 8px; background: #e2e8f0; border-radius: 99px; overflow: hidden; }
.progress-fill { height: 100%; background: linear-gradient(90deg, #4f46e5, #06b6d4); border-radius: 99px; transition: width 0.5s ease; }
.progress-label { font-size: 0.78rem; color: #64748b; white-space: nowrap; }

/* Join Screen */
.join-screen {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
.join-card {
  background: white;
  border-radius: 24px;
  padding: 48px 40px;
  max-width: 440px;
  width: 100%;
  text-align: center;
  box-shadow: 0 8px 40px rgba(79,70,229,0.15);
}
.error-card { border: 3px solid #ef4444; }
.join-logo { margin-bottom: 24px; }
.logo-img { width: 150px; height: 150px; object-fit: contain; }
.join-card h2 { font-size: 1.5rem; font-weight: 900; margin-bottom: 6px; color: #4f46e5; }
.join-card p { color: #64748b; font-size: 0.9rem; margin-bottom: 8px; }
.welcome-text { margin-bottom: 24px !important; font-size: 0.95rem; }
.join-card .form-group { text-align: left; }
.start-btn { width: 100%; justify-content: center; padding: 14px; font-size: 1.05rem; margin-top: 8px; border-radius: 14px; }
.start-btn:disabled { opacity: 0.6; cursor: not-allowed; }

/* Chat Header */
.chat-header {
  padding: 12px 20px;
  background: white;
  border-bottom: 2px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 0.85rem;
  color: #64748b;
  font-weight: 600;
}
.progress-bar { flex: 1; height: 6px; background: #e2e8f0; border-radius: 99px; overflow: hidden; }
.progress-fill { height: 100%; background: linear-gradient(90deg, #4f46e5, #06b6d4); border-radius: 99px; transition: width 0.5s ease; }

/* Chat Body */
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 0;
  display: flex;
  flex-direction: column;
}
.messages { flex: 1; display: flex; flex-direction: column; gap: 8px; padding: 20px 20px 0; }

.bot-bubble { display: flex; align-items: flex-end; gap: 8px; }
.bubble-avatar { font-size: 1.4rem; flex-shrink: 0; }

.bubble {
  background: white;
  border-radius: 18px 18px 18px 4px;
  padding: 12px 16px;
  max-width: 78%;
  font-size: 0.97rem;
  line-height: 1.55;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07);
  animation: fadeUp 0.3s ease;
}
.bubble.success {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
  color: #064e3b;
  font-weight: 700;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.msg-user { display: flex; justify-content: flex-end; }
.msg-user .bubble {
  background: linear-gradient(135deg, #4f46e5, #818cf8);
  color: white;
  border-radius: 18px 18px 4px 18px;
  margin-right: 0;
}

.system-box {
  margin: 8px 0;
  animation: fadeUp 0.3s ease;
}
.email-display {
  background: #1e1e2e;
  color: #a9b1d6;
  border-radius: 12px;
  padding: 16px 20px;
  font-size: 0.85rem;
  line-height: 1.7;
  white-space: pre-wrap;
  border-left: 4px solid #ef4444;
  font-family: 'Courier New', monospace;
  max-width: 100%;
  overflow-x: auto;
}

/* Typing indicator */
.typing-indicator { display: flex; gap: 5px; align-items: center; padding: 14px 20px; }
.typing-indicator span {
  width: 8px; height: 8px; background: #94a3b8; border-radius: 50%;
  animation: bounce 1.2s infinite;
}
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-6px); }
}

/* Question Area */
.question-area {
  background: white;
  border-top: 2px solid #e2e8f0;
  padding: 20px;
  position: sticky;
  bottom: 0;
  animation: slideUp 0.3s ease;
  margin: 20px 0 0 0;
}
@keyframes slideUp {
  from { transform: translateY(20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
.question-text {
  font-weight: 800;
  font-size: 1rem;
  color: #1a1a2e;
  margin-bottom: 14px;
}
.options { display: flex; flex-direction: column; gap: 8px; }
.option-btn {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  background: #f8faff;
  text-align: left;
  font-size: 0.9rem;
  transition: all 0.2s;
  font-family: inherit;
}
.option-btn:hover:not(.disabled) { border-color: #4f46e5; background: #eef2ff; }
.option-btn.selected { border-color: #4f46e5; background: #eef2ff; font-weight: 700; }
.option-btn.disabled { opacity: 0.7; cursor: not-allowed; }
.opt-key {
  min-width: 26px; height: 26px;
  background: #4f46e5; color: white;
  border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  font-weight: 900; font-size: 0.85rem; flex-shrink: 0;
}
.option-btn.selected .opt-key { background: #4338ca; }
.opt-text { line-height: 1.45; padding-top: 2px; }
.submit-btn { width: 100%; justify-content: center; margin-top: 12px; padding: 12px; font-size: 1rem; border-radius: 12px; }

/* Complete Screen */
.complete-screen {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  justify-content: center;
}
.complete-card {
  background: white;
  border-radius: 24px;
  padding: 40px 36px;
  max-width: 560px;
  width: 100%;
  text-align: center;
  box-shadow: 0 8px 40px rgba(79,70,229,0.15);
}
.score-circle {
  width: 120px; height: 120px;
  border-radius: 50%;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  margin: 0 auto 16px;
  border: 6px solid;
}
.score-great { border-color: #10b981; background: #d1fae5; color: #065f46; }
.score-ok { border-color: #f59e0b; background: #fef3c7; color: #92400e; }
.score-low { border-color: #ef4444; background: #fee2e2; color: #991b1b; }
.score-num { font-size: 2.2rem; font-weight: 900; line-height: 1; }
.score-label { font-size: 0.8rem; font-weight: 700; }
.grade-badge {
  display: inline-block;
  background: #4f46e5; color: white;
  border-radius: 99px;
  padding: 4px 18px;
  font-weight: 900;
  font-size: 1.2rem;
  margin-bottom: 16px;
}
.complete-card h2 { font-size: 1.5rem; font-weight: 900; margin-bottom: 8px; }
.passed-text { font-weight: 700; margin-bottom: 20px; }
.text-success { color: #059669; }
.text-warn { color: #d97706; }

.badges { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; margin-bottom: 28px; }
.badge-item {
  background: linear-gradient(135deg, #eef2ff, #e0e7ff);
  color: #3730a3; font-weight: 700;
  padding: 6px 14px; border-radius: 99px;
  font-size: 0.85rem;
}

.results-table { text-align: left; margin-bottom: 24px; }
.results-table h3 { font-weight: 800; margin-bottom: 12px; font-size: 1rem; }
.result-row {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 0; border-bottom: 1px solid #f1f5f9;
  font-size: 0.9rem;
}
.result-icon { font-size: 1.1rem; flex-shrink: 0; }
.result-title { flex: 1; font-weight: 600; }
.result-pts { color: #64748b; font-weight: 700; }

.complete-messages { display: flex; flex-direction: column; gap: 8px; margin-bottom: 20px; }
.complete-msg {
  padding: 10px 16px; border-radius: 12px;
  font-size: 0.9rem; font-weight: 600;
  animation: fadeUp 0.3s ease;
}
.complete-msg.bot { background: #f1f5f9; }
.complete-msg.success { background: #d1fae5; color: #065f46; }

.finish-note { color: #94a3b8; font-size: 0.85rem; }

.error-msg {
  background: #fee2e2; color: #991b1b;
  padding: 10px 14px; border-radius: 10px;
  font-size: 0.9rem; margin-bottom: 12px;
}

/* Responsive for mobile/tablet */
@media (max-width: 768px) {
  .join-card {
    padding: 36px 28px;
  }
  .logo-img { width: 180px; height: 100px; }
  .join-card h2 { font-size: 1.3rem; }
  .join-card p { font-size: 0.85rem; }
  .chat-header {
    padding: 10px 16px;
    font-size: 0.8rem;
  }
  .progress-bar { height: 5px; }
  .chat-app { max-width: 100%; }
}
</style>
