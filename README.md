<div align="center">

# 🚀 IntelliHub

**企业级智能API开放平台**

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7+-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.x-brightgreen.svg)](https://vuejs.org/)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/yourusername/intelli_hub/pulls)

[简体中文](README.md) | [English](README_EN.md)

</div>

---

## 📖 项目简介

IntelliHub 是一个面向企业与开发者的**智能API开放平台**，旨在以 **统一入口、统一安全、统一治理** 的方式对外开放企业内部微服务能力。平台提供完整的API生命周期管理、多租户体系、智能治理和实时监控能力。

### ✨ 核心特性

- 🌐 **统一网关治理** - 认证/鉴权/限流/租户校验/动态路由/日志上报
- 🔐 **IAM多租户与RBAC** - 完善的身份认证与权限管理体系
- 📊 **API全生命周期管理** - 从创建、发布到下线的完整管理
- 🔑 **应用中心** - AppKey/AppSecret管理与API订阅授权
- 📈 **智能治理中心** - 调用日志消费、统计聚合、实时告警
- 🎯 **多协议支持** - HTTP/Dubbo泛化调用/Mock
- 🔒 **企业级安全** - JWT认证、签名验证、防重放攻击
- 📱 **现代化前端** - Vue3 + TypeScript + Element Plus

### 🎯 适用场景

- 企业微服务能力统一对外开放
- API集市与开放平台建设
- 多租户SaaS服务API管理
- 内部API统一治理与监控

> 📚 **详细架构说明**：[项目架构文档](docs/项目架构文档.md)

---

## 🏗️ 系统架构

### 架构总览

```
┌─────────────────────────────────────────────────────────────────┐
│  客户端层 - Console前端 / 第三方系统 / SDK                        │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP/HTTPS
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│  网关层 - Spring Cloud Gateway (WebFlux)                        │
│  • JWT鉴权 / AppKey+签名  • 限流/熔断  • 动态路由                │
└────────────────────────────┬────────────────────────────────────┘
         │                   │                   │
         ▼                   ▼                   ▼
    ┌─────────┐         ┌─────────┐        ┌─────────┐
    │  Nacos  │         │  Kafka  │        │  Redis  │
    │ 注册/配置│         │  消息队列│        │ 缓存/限流│
    └─────────┘         └─────────┘        └─────────┘
         │                   │                   │
         ▼                   ▼                   ▼
┌─────────────────────────────────────────────────────────────────┐
│  业务服务层 - Spring Boot 微服务集群                             │
│  • IAM认证  • API平台  • 应用中心  • 治理中心  • 扩展服务        │
└────────────────────────────┬────────────────────────────────────┘
                             ▼
                        ┌─────────┐
                        │  MySQL  │
                        └─────────┘
```

### 模块清单

| 模块 | 端口 | 状态 | 说明 |
|------|------|:----:|------|
| `intelli-gateway-service` | 8080 | ✅ | Spring Cloud Gateway，统一入口与治理链路 |
| `intelli-auth-iam-service` | 8081 | ✅ | 多租户、登录与JWT签发、RBAC权限 |
| `intelli-api-platform-service` | 8082 | ✅ | API生命周期与路由配置、公告管理 |
| `intelli-governance-service` | 8083 | ✅ | 调用日志消费、统计聚合、告警检测 |
| `intelli-app-center-service` | 8085 | ✅ | App、AppKey/AppSecret、订阅授权 |
| `intelli-sdk` | - | ✅ | Java SDK（签名生成、HTTP客户端） |
| `intellihub-frontend` | 5173 | ✅ | Vue3控制台，开发代理到网关 |
| `intelli-aigc-service` | 8084 | 📦 | AIGC能力（规划中） |
| `intelli-search-service` | 8086 | ✅ | 聚合搜索（规划中） |
| `intelli-event-service` | 8087 | 📦 | 事件中心（规划中） |
| `intelli-log-audit-service` | 8088 | 📦 | 日志审计（规划中） |

### 核心流程

#### 管理后台流量（JWT认证）
```
前端Console → Gateway (JWT验证) → 微服务 (用户/租户上下文) → 业务处理
```

#### 开放API流量（AppKey+签名）
```
第三方系统 → Gateway → 路由匹配 → 签名验证 → 订阅校验 → HTTP/Dubbo转发 → 返回结果
                ↓                                       ↓
             日志上报 → Kafka → 治理服务 → 统计/告警
```

> 📖 **详细流程说明**：
> - [网关技术流程说明书](docs/网关技术流程说明书.md)
> - [API开放平台架构设计指南](docs/API开放平台架构设计指南.md)
> - [告警系统流程说明书](docs/告警系统流程说明书.md)

---

## 🛠️ 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7+ | 微服务基础框架 |
| Spring Cloud | 2021.x | 微服务治理 |
| Spring Cloud Gateway | - | 响应式网关 |
| MyBatis Plus | 3.5+ | ORM增强工具 |
| Nacos | 2.x | 注册中心与配置中心 |
| Dubbo | 3.x | RPC框架（泛化调用） |
| Kafka | 2.x | 消息队列（调用日志） |
| MySQL | 8.0+ | 主数据库 |
| Redis | 6.0+ | 缓存/限流/统计 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.x | 渐进式框架 |
| Vite | 4.x | 构建工具 |
| TypeScript | 5.x | 类型系统 |
| Element Plus | 2.x | UI组件库 |
| Pinia | 2.x | 状态管理 |
| Axios | 1.x | HTTP客户端 |
| ECharts | 5.x | 数据可视化 |

---

## 🚀 快速开始

### 环境要求

- **JDK**: 8+ (推荐 11)
- **Maven**: 3.6+
- **Node.js**: ^20.19.0 || >=22.12.0
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Nacos**: 2.x (可选，本地可使用默认配置)
- **Kafka**: 2.x (可选，但推荐启用以支持完整的治理链路)

### 1️⃣ 准备中间件

#### 快速启动Kafka（Docker Compose）

```bash
# 使用 bitnami 镜像
cd docker
docker-compose -f docker-compose-kafka.yml up -d

# 或使用 wurstmeister 镜像（单机版）
docker-compose -f docker-compose-kafka-standalone.yml up -d
```

#### 启动MySQL和Redis

请确保MySQL和Redis已启动，并根据各服务的 `application.yml` 配置连接信息。

### 2️⃣ 启动后端服务

进入后端父工程目录：

```bash
cd intellihub-parent
```

按以下顺序启动服务：

```bash
# 1. IAM认证服务
cd intelli-auth-iam-service
mvn spring-boot:run

# 2. API平台服务
cd ../intelli-api-platform-service
mvn spring-boot:run

# 3. 应用中心服务
cd ../intelli-app-center-service
mvn spring-boot:run

# 4. 治理中心服务
cd ../intelli-governance-service
mvn spring-boot:run

# 5. 网关服务（最后启动）
cd ../intelli-gateway-service
mvn spring-boot:run
```

> 💡 **提示**：各服务的配置文件位于 `src/main/resources/application.yml`，请根据实际环境调整数据库、Redis、Kafka等连接信息。

### 3️⃣ 启动前端控制台

```bash
cd intellihub-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端将在 `http://localhost:5173` 启动，所有 `/api` 请求会自动代理到网关 `http://localhost:8080`。

### 4️⃣ 访问系统

- **控制台**: http://localhost:5173
- **网关**: http://localhost:8080
- **默认账号**: 请查看 IAM 服务的初始化脚本

---

## 📚 文档中心

### 核心文档

| 文档 | 说明 |
|------|------|
| [项目架构文档](docs/项目架构文档.md) | 总体架构、模块职责、关键链路、技术选型 |
| [网关技术流程说明书](docs/网关技术流程说明书.md) | 网关执行链路、Filter顺序、鉴权、路由、日志 |
| [API开放平台架构设计指南](docs/API开放平台架构设计指南.md) | 开放API平台架构设计原则与最佳实践 |
| [告警系统流程说明书](docs/告警系统流程说明书.md) | 告警规则、状态机、抑制策略与故障排查 |
| [架构设计-实体关系说明](docs/架构设计-实体关系说明.md) | 核心实体关系与ER图 |
| [需求文档](docs/需求文档.md) | 功能需求与范围边界 |
| [功能开发计划](docs/功能开发计划.md) | 项目现状与后续开发计划 |

### SDK使用

查看 [Java SDK README](intellihub-parent/intelli-sdk/README.md) 了解如何使用SDK调用开放API。

---

## 💡 核心特性说明

### 认证架构

**双流量认证模型**：
- **管理后台** (`/api/**`)：JWT Token认证，网关本地验签，性能更优
- **开放API** (`/open/**`)：AppKey + HMAC-SHA256签名，支持防重放攻击

### 动态路由

网关支持根据配置动态路由到不同后端：
- **HTTP转发**：支持服务名负载均衡
- **Dubbo泛化调用**：无需依赖业务接口JAR包
- **Mock响应**：便于测试和演示

### 治理能力

- **限流**：支持IP/Path/IP+Path多维度限流
- **调用统计**：基于Kafka异步采集，Redis实时统计
- **告警检测**：支持错误率、延迟、QPS多种告警规则
- **租户隔离**：全链路租户上下文透传

---

## 🤝 参与贡献

我们欢迎任何形式的贡献，无论是新功能、Bug修复还是文档改进。

### 提交Issue

请按照以下格式提交Issue：
- **问题背景**：描述遇到的场景
- **复现步骤**：详细的操作步骤
- **期望行为**：期望看到的结果
- **实际行为**：实际看到的结果
- **环境信息**：版本、操作系统等

### 提交Pull Request

请按照以下格式描述PR：
- **模块**：涉及的模块或服务
- **改动点**：具体修改了什么
- **风险点**：可能的影响范围
- **验证方式**：如何验证改动有效

### 开发规范

- 遵循现有代码风格
- 添加必要的注释和文档
- 确保单元测试通过
- 更新相关文档

---

## 📄 开源协议

Apache License 2.0

---

## 🙏 致谢

感谢所有为本项目做出贡献的开发者！

---

## 📞 联系方式

- **Issues**: [GitHub Issues](https://github.com/yourusername/intelli_hub/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/intelli_hub/discussions)

---

<div align="center">

**如果这个项目对你有帮助，请给一个 ⭐️ Star ⭐️**

</div>
