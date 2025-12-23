<template>
  <div class="models-visual">
    <div class="models-grid">
      <div
        v-for="(model, index) in models"
        :key="index"
        class="model-icon"
        :style="{ animationDelay: `${index * 0.1}s` }"
      >
        <component :is="model.icon" v-if="model.icon" />
        <span v-else class="model-letter">{{ model.letter }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { shallowRef } from 'vue'

const models = shallowRef([
  { letter: 'G', color: '#4285f4' },
  { letter: 'A', color: '#00a67e' },
  { letter: 'C', color: '#d97706' },
  { letter: 'M', color: '#7c3aed' },
  { letter: 'L', color: '#ec4899' },
  { letter: 'D', color: '#06b6d4' },
  { letter: 'Q', color: '#84cc16' },
  { letter: 'P', color: '#f43f5e' },
  { letter: 'X', color: '#1a1a1a' },
  { letter: 'H', color: '#6366f1' },
  { letter: 'O', color: '#10b981' },
  { letter: 'S', color: '#f59e0b' },
])
</script>

<style scoped>
.models-visual {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  box-sizing: border-box;
}

.models-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  max-width: 200px;
}

.model-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #666;
  animation: fadeIn 0.5s ease forwards;
  opacity: 0;
  transition: transform 0.2s, box-shadow 0.2s;
}

.model-icon:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.model-letter {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
