<template>
  <router-view />
</template>

<style>
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

body {
  font-family: 'Nunito', sans-serif;
  background: #f0f4ff;
  color: #1a1a2e;
  min-height: 100vh;
}

:root {
  --primary: #4f46e5;
  --primary-light: #818cf8;
  --secondary: #06b6d4;
  --success: #10b981;
  --danger: #ef4444;
  --warning: #f59e0b;
  --bg: #f0f4ff;
  --card: #ffffff;
  --text: #1a1a2e;
  --muted: #64748b;
  --border: #e2e8f0;
  --radius: 16px;
  --shadow: 0 4px 24px rgba(79,70,229,0.10);
}

button { cursor: pointer; font-family: inherit; }

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 22px;
  border-radius: 12px;
  border: none;
  font-weight: 700;
  font-size: 0.95rem;
  transition: all 0.2s;
}
.btn-primary { background: var(--primary); color: white; }
.btn-primary:hover { background: #4338ca; transform: translateY(-1px); }
.btn-secondary { background: var(--border); color: var(--text); }
.btn-secondary:hover { background: #cbd5e1; }
.btn-success { background: var(--success); color: white; }
.btn-danger { background: var(--danger); color: white; }
.btn-sm { padding: 6px 14px; font-size: 0.85rem; }

.card {
  background: var(--card);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 24px;
}

input, select, textarea {
  width: 100%;
  padding: 10px 14px;
  border: 2px solid var(--border);
  border-radius: 10px;
  font-family: inherit;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.2s;
}
input:focus, select:focus, textarea:focus { border-color: var(--primary); }

label { display: block; font-weight: 700; margin-bottom: 6px; font-size: 0.9rem; color: var(--muted); }

.form-group { margin-bottom: 16px; }

.badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 700;
}
.badge-green { background: #d1fae5; color: #065f46; }
.badge-red { background: #fee2e2; color: #991b1b; }
.badge-blue { background: #dbeafe; color: #1e40af; }
.badge-yellow { background: #fef3c7; color: #92400e; }
</style>
