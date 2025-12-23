# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

IntelliHub is an enterprise-grade intelligent API management platform built with Vue 3, TypeScript, and Element Plus. The project serves as a unified gateway for API management, providing multi-tenant support, lifecycle management, and intelligent governance capabilities.

## Development Commands

### Common Development Tasks
- `npm install` - Install dependencies
- `npm run dev` - Start development server with hot reload
- `npm run build` - Build for production (runs type check and build)
- `npm run build-only` - Build without type checking
- `npm run type-check` - Run TypeScript type checking
- `npm run lint` - Run ESLint with auto-fix
- `npm run format` - Format code with Prettier
- `npm run preview` - Preview production build locally

### Node Version Requirement
- Node.js: ^20.19.0 || >=22.12.0

## Technology Stack

- **Frontend Framework**: Vue 3 with Composition API
- **Language**: TypeScript
- **Build Tool**: Vite
- **UI Library**: Element Plus
- **State Management**: Pinia
- **Routing**: Vue Router 4
- **Linting**: ESLint with Vue and TypeScript configs
- **Formatting**: Prettier

## Project Architecture

### Directory Structure
```
src/
├── assets/         # Static assets (CSS, images)
├── components/     # Reusable Vue components
│   └── icons/      # Icon components
├── router/         # Vue Router configuration
├── stores/         # Pinia state management
└── views/          # Page-level components
```

### Key Architectural Patterns
- **Vue 3 Composition API**: All components should use `<script setup>` syntax
- **TypeScript Integration**: Strict typing throughout the application
- **Modular Components**: Keep components focused and reusable
- **Centralized Routing**: Router configuration in `src/router/index.ts`
- **State Management**: Use Pinia stores for global state

### Important Files
- `src/main.ts` - Application entry point with Element Plus integration
- `src/router/index.ts` - Route definitions with lazy loading
- `vite.config.ts` - Vite configuration with path aliases (`@` -> `src/`)
- `eslint.config.ts` - ESLint configuration for Vue and TypeScript

## Development Guidelines

### Component Development
- Use `<script setup>` syntax for all Vue components
- Follow Vue 3 Composition API patterns
- Implement TypeScript interfaces for props and data
- Leverage Element Plus components for consistent UI

### Code Style
- ESLint runs on `*.ts`, `*.mts`, `*.tsx`, and `*.vue` files
- Prettier formatting is enforced on save
- Editor is configured to auto-fix ESLint issues
- Use single quotes for strings and 2-space indentation

### Path Aliases
- Use `@/` prefix for imports from `src/` directory (e.g., `@/components/HelloWorld.vue`)

### Browser DevTools
- Install Vue.js devtools for debugging
- Enable Custom Object Formatter in Chrome DevTools
- Vue DevTools plugin is integrated in development build

## Documentation

The project includes comprehensive documentation in the `doc/` directory:
- `01_需求文档.md` - Requirements and project overview
- `02_项目规划与设计文档.md` - Architecture and design specifications
- `03_开发与实现文档.md` - Development guidelines and implementation details

## Testing and Quality

- Type checking is mandatory before production builds
- ESLint is configured with Vue and TypeScript specific rules
- Prettier ensures consistent code formatting
- Use Vue DevTools for component inspection during development