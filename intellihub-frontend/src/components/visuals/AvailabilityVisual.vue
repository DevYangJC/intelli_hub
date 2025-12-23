<template>
  <div class="availability-visual">
    <div class="provider-tag">
      <span class="provider-name">gateway/api-service-v2</span>
    </div>
    <div class="status-indicators">
      <div class="indicator active"></div>
      <div class="indicator active"></div>
      <div class="indicator fallback"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.availability-visual {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding: 16px;
  box-sizing: border-box;
}

.provider-tag {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 13px;
  color: #333;
  font-family: monospace;
}

.status-indicators {
  display: flex;
  gap: 16px;
}

.indicator {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  position: relative;
}

.indicator.active {
  background: #10b981;
  animation: pulse 2s infinite;
}

.indicator.fallback {
  background: #3b82f6;
  animation: pulse 2s infinite 0.5s;
}

.indicator::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8px;
  height: 8px;
  background: #fff;
  border-radius: 50%;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
}
</style>
