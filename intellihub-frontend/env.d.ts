/// <reference types="vite/client" />

import type { DefineComponent } from 'vue'

declare module '*.svg?component' {
  const component: DefineComponent<Record<string, unknown>, Record<string, unknown>, unknown>
  export default component
}
