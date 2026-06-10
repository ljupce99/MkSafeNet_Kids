<template>
  <div class="chat-app">

    <!-- Header -->
    <div class="chat-header">
      <div class="bot-avatar"><img src="../assets/logo.png" alt="MkSafeNet" class="header-logo" /></div>
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
            <span class="bubble-avatar">🤖</span>
            <div class="bubble" :class="msg.type">{{ msg.text }}</div>
          </div>
          <div v-else-if="msg.type === 'system'" class="system-box">
            <pre class="email-display">{{ msg.text }}</pre>
          </div>
        </div>

        <div v-if="typing" class="msg-wrap msg-bot">
          <div class="bot-bubble">
            <img src="../assets/logo.png" alt="" class="bubble-avatar-img" />
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
  // Normalize incoming messages to handle any malformed entries created by the admin editor
  messages = normalizeIncomingMessages(messages || [])
  console.log(messages)
  // Keep typing indicator visible while messages are being revealed
  typing.value = true
  for (const msg of messages) {
    // Final sanitize + split pass to ensure no stray backslash artifacts remain
    const parts = sanitizeAndSplit(msg)
    for (const e of parts) {
      const ms = computeDisplayDelay(e)
      await delay(ms)
      visibleMessages.value.push(e)
      await scrollBottom()
    }
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
    const msgs = normalizeIncomingMessages(data.messages || [])
    for (const msg of msgs) {
      const parts = sanitizeAndSplit(msg)
      for (const e of parts) {
        const ms = computeDisplayDelay(e, { complete: true })
        await delay(ms)
        visibleMessages.value.push(e)
      }
    }
  }
}

function delay(ms) {
  // ms = 0
  return new Promise(resolve => setTimeout(resolve, Math.max(ms, 150)))
}

// Estimate an appropriate display delay for a message so messages appear at a readable pace.
// - If msg.delayMs is present and > 0: use it but bound it to a reasonable range.
// - Otherwise estimate from message text length (chars * 40ms) with min/max bounds.
function computeDisplayDelay(msg, opts = {}) {
  const text = (msg && (msg.text || msg.title || '')) || ''
  const explicit = Number(msg?.delayMs) || 0

  // Reading speed estimate: ~40ms per character (fast). Use a more conservative multiplier for readability.
  const perChar = 45
  const estimated = Math.min(3000, Math.max(700, Math.floor(text.length * perChar)))

  // If an explicit delay is provided, respect it but clamp to [300, 3000]
  if (explicit > 0) {
    return Math.min(3000, Math.max(300, explicit))
  }

  // For complete-screen messages we can allow slightly shorter delays but keep readable
  if (opts.complete) {
    return Math.min(1800, Math.max(500, Math.floor(estimated * 0.85)))
  }

  return estimated
}

async function scrollBottom() {
  await nextTick()
  if (chatBody.value) {
    chatBody.value.scrollTop = chatBody.value.scrollHeight
  }
}

// Normalize message arrays coming from the backend or admin editor.
// Some older/broken scenarios may have been saved as multiple short message objects
// where each line was treated as a separate message and the `type` field contains
// the line text. This function merges such stray lines into the previous message
// text and, if a stray line contains only digits, treats it as a delay value for
// the previous message.
function normalizeIncomingMessages(list) {
  const allowed = new Set(['bot', 'system', 'success', 'user', 'consequence'])
  const out = []

  function pushMsg(msg) {
    // normalize type
    const t = String(msg.type || '').toLowerCase()
    const type = allowed.has(t) ? t : 'system'
    msg.type = type
    // merge consecutive system messages
    if (type === 'system' && out.length && out[out.length - 1].type === 'system') {
      const prev = out[out.length - 1]
      prev.text = (prev.text ? prev.text + '\n' : '') + String(msg.text || '')
      if (!prev.delayMs && msg.delayMs) prev.delayMs = msg.delayMs
    } else {
      out.push({ type, text: String(msg.text || ''), delayMs: Number(msg.delayMs) || 0, icon: msg.icon || null })
    }
  }

  for (const raw of (list || [])) {
    const m = raw || {}
    let rawType = m.type === undefined || m.type === null ? '' : String(m.type)
    let rawText = m.text === undefined || m.text === null ? '' : String(m.text)
    const delay = Number(m.delayMs) || 0

    // cleanup common artifacts
    rawType = rawType.replace(/\\0/g, '').trim()
    rawText = rawText.replace(/\\0/g, '').trim()

    const typeLower = rawType.toLowerCase()
    // Case 1: valid explicit type
    if (allowed.has(typeLower)) {
      pushMsg({ type: typeLower, text: rawText, delayMs: delay, icon: m.icon || null })
      continue
    }

    // Case 2: numeric-only text -> treat as delay for previous message
    if (/^\d+$/.test(rawText) && out.length) {
      const prev = out[out.length - 1]
      if (!prev.delayMs || prev.delayMs === 0) prev.delayMs = parseInt(rawText, 10)
      continue
    }

    // Case 3: rawText present but no valid type -> default to bot (typical incoming message)
    if (rawText) {
      pushMsg({ type: 'bot', text: rawText, delayMs: delay, icon: m.icon || null })
      continue
    }

    // Case 4: rawType present but not a known token -> likely a continuation line (from broken save)
    if (rawType) {
      if (out.length) {
        const prev = out[out.length - 1]
        prev.text = (prev.text ? prev.text + '\n' : '') + rawType
        if (!prev.delayMs && delay) prev.delayMs = delay
      } else {
        // no previous message: create a system message
        pushMsg({ type: 'system', text: rawType, delayMs: delay, icon: m.icon || null })
      }
      continue
    }

    // Fallback: push empty system
    pushMsg({ type: 'system', text: '', delayMs: delay, icon: m.icon || null })
  }

  return out
}

// Expand a message object if its text contains embedded message headers.
// Returns an array of message objects to render in sequence.
function expandMessageObject(msg) {
  // Simple expansion: do not attempt to split text into additional messages here.
  // Return the msg itself (sanitized). This avoids accidental splitting/merging.
  const text = String(msg.text || '').replace(/\\0/g, '').trim()
  return [{ type: msg.type || 'system', text: text.replace(/\\\d+\s*$/, '').trim(), delayMs: Number(msg.delayMs) || 0, icon: msg.icon || null }]
}

// Sanitize a normalized message and split it into parts safe for display.
// This is a final defensive pass: removes '\0' markers, extracts trailing delays,
// splits any embedded headers, and ensures every returned part has a valid type.
function sanitizeAndSplit(msg) {
  if (!msg) return []
  const allowed = new Set(['bot', 'system', 'success', 'user', 'consequence'])
  const type = allowed.has(String(msg.type || '').toLowerCase()) ? String(msg.type).toLowerCase() : 'system'

  // Clean the raw text first
  let text = String(msg.text || '')
    .replace(/\\0/g, '')
    .replace(/\\\d+\s*$/, '')

  // Split embedded message headers inside the text, e.g.
  // "📦 title\n bot\next message\n system\..."
  const lines = text.split('\n')
  const out = []
  let current = { type, text: '', delayMs: Number(msg.delayMs) || 0, icon: msg.icon || null }

  function flush() {
    const cleaned = String(current.text || '').trim()
    if (cleaned) out.push({ type: current.type, text: cleaned, delayMs: Number(current.delayMs) || 0, icon: current.icon || null })
    current = { type: 'system', text: '', delayMs: 0, icon: msg.icon || null }
  }

  for (const rawLine of lines) {
    const line = String(rawLine || '').trimEnd()
    if (!line.trim()) continue

    // Detect header-like lines: "bot\..." or "system\..."
    const headerMatch = line.match(/^(bot|system|success|user|consequence)\\(.*)$/i)
    if (headerMatch) {
      flush()
      const hType = headerMatch[1].toLowerCase()
      const hText = (headerMatch[2] || '').replace(/\\0/g, '').trim()
      current = { type: hType, text: hText, delayMs: 0, icon: msg.icon || null }
      continue
    }

    // Detect line endings that contain a delay suffix: "...\2400"
    const delayMatch = line.match(/^(.*)\\(\d+)$/)
    if (delayMatch) {
      current.text = (current.text ? current.text + '\n' : '') + delayMatch[1].trim()
      current.delayMs = Number(delayMatch[2]) || current.delayMs || 0
      flush()
      continue
    }

    // Normal text line: append to current message
    current.text = (current.text ? current.text + '\n' : '') + line.trim()
  }

  flush()
  return out.length ? out : [{ type, text: text.trim(), delayMs: Number(msg.delayMs) || 0, icon: msg.icon || null }]
}

// Final cleanup: split any leftover embedded headers inside message texts and
// remove stray '\0' and trailing '\digits' artifacts.
// finalizeNormalizedMessages not needed with the simplified normalizer; keep small wrapper
function finalizeNormalizedMessages(list) {
  const allowed = new Set(['bot', 'system', 'success', 'user', 'consequence'])
  return (list || []).map(m => ({ type: (m.type && allowed.has(m.type)) ? m.type : 'system', text: String(m.text || '').replace(/\\0/g, '').trim(), delayMs: Number(m.delayMs) || 0, icon: m.icon || null }))
}

// Lightweight embedded header parser: recognizes 'type\text' or 'type\text\delay'
function parseEmbeddedHeader(s) {
  if (!s || !s.includes('\\')) return null
  const m = s.match(/^([^\\]+)\\([\s\S]*?)(?:\\(\d+))?$/)
  if (!m) return null
  const type = m[1].trim()
  const text = (m[2] || '').trim()
  const delayMs = m[3] ? parseInt(m[3], 10) : 0
  const allowed = new Set(['bot', 'system', 'success', 'user', 'consequence'])
  if (!allowed.has(type)) return null
  return { type, text, delayMs }
}

// We no longer need a complex embedded splitter. If embedded headers exist, parseEmbeddedHeader
// will detect and handle them when necessary. Keep this function minimal and safe.
function splitEmbeddedMessagesInText(raw) {
  if (!raw) return []
  const lines = String(raw).replace(/\r/g, '').split('\n')
  const out = []
  let current = null
  for (const line of lines) {
    const m = parseEmbeddedHeader(line)
    if (m) {
      if (current) out.push(current)
      out.push(m)
      current = null
      continue
    }
    // if current exists, append; else create system
    if (!current) current = { type: 'system', text: line.trim(), delayMs: 0 }
    else current.text = (current.text ? current.text + '\n' : '') + line.trim()
  }
  if (current) out.push(current)
  return out.map(m => ({ type: m.type || 'system', text: String(m.text || '').replace(/\\0/g, '').trim(), delayMs: Number(m.delayMs) || 0, icon: m.icon || null }))
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
.header-logo { width: 46px; height: 46px; object-fit: contain; border-radius: 8px; }
.bubble-avatar-img { width: 34px; height: 34px; object-fit: contain; border-radius: 6px; flex-shrink: 0; }
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
  /* allow the card to grow but not overflow viewport; enable internal scrolling when content is long */
  display: flex;
  flex-direction: column;
  align-items: center;
  max-height: calc(100vh - 96px);
  overflow: auto;
}
.score-circle {
  width: 120px; height: 120px;
  border-radius: 50%;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  margin: 0 auto 16px;
  border: 6px solid;
  box-sizing: border-box;
  flex: 0 0 120px;
  aspect-ratio: 1 / 1;
  overflow: hidden;
}
.score-great { border-color: #10b981; background: #d1fae5; color: #065f46; }
.score-ok { border-color: #f59e0b; background: #fef3c7; color: #92400e; }
.score-low { border-color: #ef4444; background: #fee2e2; color: #991b1b; }
.score-num { font-size: 2.2rem; font-weight: 900; line-height: 1; white-space: nowrap; }
.score-label { font-size: 0.8rem; font-weight: 700; line-height: 1; white-space: nowrap; }
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

/* Make result table and rows full width inside the card and ensure long titles wrap */
.results-table { width: 100%; }
.result-row { width: 100%; }
.result-title { min-width: 0; overflow-wrap: anywhere; white-space: normal; }

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
