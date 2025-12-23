<template>
  <div class="security-visual">
    <div class="lock-row">
      <div class="lock-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
          <path d="M7 11V7a5 5 0 0 1 10 0v4" />
        </svg>
      </div>
      <div class="check-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="20 6 9 17 4 12" />
        </svg>
      </div>
      <div class="lock-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
          <path d="M7 11V7a5 5 0 0 1 10 0v4" />
        </svg>
      </div>
    </div>
    <div class="shield-icon">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
        <circle cx="12" cy="10" r="3" />
        <path d="M12 13v3" />
        <circle cx="9" cy="10" r="1" fill="currentColor" />
        <circle cx="15" cy="10" r="1" fill="currentColor" />
      </svg>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.security-visual {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 16px;
  box-sizing: border-box;
}

.lock-row {
  display: flex;
  align-items: center;
  gap: 24px;
}

.lock-icon {
  width: 32px;
  height: 32px;
  color: #9ca3af;
}

.check-icon {
  width: 28px;
  height: 28px;
  color: #10b981;
  background: #ecfdf5;
  border-radius: 50%;
  padding: 4px;
}

.shield-icon {
  width: 48px;
  height: 48px;
  color: #6366f1;
}
</style>
