<template>
  <div class="consequence-overlay" @click.self="handleDone">
    <div class="consequence-panel" :class="'type-' + typeClass">
      <div class="consequence-header">
        <div class="consequence-icon">{{ icon }}</div>
        <h2>⚠️ What Happened...</h2>
      </div>

      <div class="consequence-feed">
        <div v-for="(msg, i) in visibleMsgs" :key="i"
             class="feed-item" :class="'feed-' + msg.type">
          {{ msg.text }}
        </div>
        <div v-if="feedTyping" class="feed-typing">
          <span></span><span></span><span></span>
        </div>
      </div>

      <div v-if="allShown" class="consequence-footer">
        <p class="learned">Now you know! Let's keep going. 💪</p>
        <button class="btn btn-primary continue-btn" @click="handleDone">
          Continue →
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const props = defineProps({
  type: String,
  messages: Array
})
const emit = defineEmits(['done'])

const visibleMsgs = ref([])
const feedTyping = ref(false)
const allShown = ref(false)

const icon = computed(() => {
  const icons = {
    ACCOUNT_HACKED: '💸', GAME_HACKED: '🎮',
    DATA_STOLEN: '💳', IDENTITY_STOLEN: '🔑',
    VIRUS_INSTALLED: '🦠'
  }
  return icons[props.type] || '⚠️'
})

const typeClass = computed(() => (props.type || '').toLowerCase().replace('_', '-'))

onMounted(async () => {
  await showMessages()
})

async function showMessages() {
  feedTyping.value = true
  for (const msg of props.messages) {
    await delay(Math.min(msg.delayMs > 0 ? msg.delayMs : 400, 700))
    feedTyping.value = false
    visibleMsgs.value.push(msg)
    feedTyping.value = true
  }
  feedTyping.value = false
  allShown.value = true
}

function handleDone() {
  if (allShown.value) emit('done')
}

function delay(ms) {
  return new Promise(r => setTimeout(r, Math.max(ms, 150)))
}
</script>

<style scoped>
.consequence-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.85);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

.consequence-panel {
  background: #0f172a;
  border-radius: 20px;
  padding: 0;
  max-width: 500px;
  width: calc(100% - 32px);
  max-height: 85vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border: 2px solid #ef4444;
  animation: slideIn 0.4s ease;
}
@keyframes slideIn {
  from { transform: scale(0.9) translateY(20px); opacity: 0; }
  to { transform: scale(1) translateY(0); opacity: 1; }
}

.consequence-header {
  background: linear-gradient(135deg, #7f1d1d, #991b1b);
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}
.consequence-icon { font-size: 2.5rem; animation: shake 0.5s ease 0.3s; }
@keyframes shake {
  0%, 100% { transform: rotate(0); }
  25% { transform: rotate(-10deg); }
  75% { transform: rotate(10deg); }
}
.consequence-header h2 { color: white; font-size: 1.2rem; font-weight: 900; }

.consequence-feed {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.feed-item {
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 0.9rem;
  line-height: 1.5;
  animation: fadeUp 0.3s ease;
  font-weight: 600;
}
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}
.feed-consequence { background: #1e293b; color: #f87171; }
.feed-bot { background: #1e293b; color: #94a3b8; }
.feed-system { background: #0c1a2e; color: #38bdf8; border-left: 3px solid #38bdf8; }

.feed-typing {
  display: flex; gap: 5px; padding: 10px 14px;
}
.feed-typing span {
  width: 7px; height: 7px; background: #475569; border-radius: 50%;
  animation: bounce 1.2s infinite;
}
.feed-typing span:nth-child(2) { animation-delay: 0.2s; }
.feed-typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-5px); }
}

.consequence-footer {
  padding: 20px 24px;
  border-top: 1px solid #1e293b;
  background: #0a0f1e;
}
.learned { color: #94a3b8; font-size: 0.9rem; margin-bottom: 12px; font-weight: 600; }
.continue-btn { width: 100%; justify-content: center; padding: 12px; font-size: 1rem; border-radius: 12px; }
</style>
