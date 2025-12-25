# IntelliHub - 企业级 API 开放平台

<div align="center">

![IntelliHub Logo](https://img.shields.io/badge/IntelliHub-API%20Platform-blue?style=for-the-badge)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2021.0.9-blue.svg)](https://spring.io/projects/spring-cloud)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![JDK](https://img.shields.io/badge/JDK-1.8-orange.svg)](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html)

**一个基于 Spring Cloud 微服务架构的企业级 API 开放平台**

[功能特性](#功能特性) • [技术架构](#技术架构) • [快速开始](#快速开始) • [文档](#文档) • [贡献指南](#贡献指南)

</div>

---

## 📖 项目简介

IntelliHub 是一个企业级 API 开放平台，旨在帮助企业内部开发者将微服务接口安全、高效地对外开放。通过统一的 API 网关、完善的认证授权体系和多租户管理，实现内部服务的安全暴露和统一治理。

### 核心价值

- 🔐 **统一认证授权** - 基于 JWT 的无状态认证，支持多租户隔离和 RBAC 权限管理
- 🚀 **API 统一管理** - 提供 API 注册、发布、版本管理和生命周期管理
- 🌐 **高性能网关** - 基于 Spring Cloud Gateway 的响应式网关，支持限流、熔断、路由转发
- 📊 **智能监控** - API 调用统计、性能监控、日志审计
- 🔧 **灵活扩展** - 支持多种后端协议（HTTP/Dubbo/gRPC），易于集成和扩展

---

## ✨ 功能特性

### 核心功能

| 功能模块 | 描述 |
|---------|------|
| **API 管理** | API 注册、配置、发布、版本管理、状态流转（草稿→发布→下线→废弃） |
| **认证授权** | JWT Token 认证、API Key 认证、角色权限控制（RBAC）、多租户隔离 |
| **网关服务** | 路由转发、请求认证、限流控制、跨域处理、日志记录 |
| **用户管理** | 租户管理、用户管理、角色管理、权限管理 |
| **监控统计** | API 调用统计、性能监控、操作日志、登录日志 |

### 技术亮点

- ✅ **响应式架构** - 基于 WebFlux 的高并发非阻塞网关
- ✅ **服务治理** - Nacos 服务注册与发现、动态配置管理
- ✅ **安全加固** - BCrypt 密码加密、HS512 签名算法、请求签名验证
- ✅ **多租户支持** - 数据隔离、租户级配额管理
- ✅ **灵活限流** - 基于 Redis 的滑动窗口限流算法
- ✅ **协议转换** - 支持 HTTP/Dubbo/gRPC 协议转换（规划中）

---

## 🏗️ 技术架构

### 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **Spring Boot** | 2.7.18 | 核心应用框架 |
| **Spring Cloud** | 2021.0.9 | 微服务治理 |
| **Spring Cloud Alibaba** | 2021.0.6.0 | Nacos 注册中心/配置中心 |
| **Spring Cloud Gateway** | 2021.0.9 | 响应式 API 网关 |
| **MyBatis Plus** | 3.5.5 | ORM 框架 |
| **MySQL** | 8.0.33 | 关系型数据库 |
| **Redis** | - | 分布式缓存、限流 |
| **Dubbo** | 3.1.11 | RPC 框架 |
| **JWT (jjwt)** | 0.11.5 | Token 认证 |
| **Hutool** | 5.8.22 | Java 工具库 |

### 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        客户端层                              │
│              前端应用 / 外部调用者                            │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                     API 网关层                               │
│   Spring Cloud Gateway - 认证/限流/路由/跨域                 │
└────────────┬───────────────┬───────────────┬────────────────┘
             │               │               │
    ┌────────▼────┐   ┌─────▼─────┐   ┌─────▼─────┐
    │  Nacos 注册  │   │ Nacos 配置 │   │   Redis   │
    │    中心      │   │    中心    │   │  缓存/限流 │
    └─────────────┘   └───────────┘   └───────────┘
             │
    ┌────────┴───────────────────────────────────────┐
    │                  微服务层                       │
    ├────────────────────────────────────────────────┤
    │  ┌─────────────────┐  ┌─────────────────┐      │
    │  │  认证授权服务    │  │  API 平台服务   │      │
    │  │  :8081          │  │  :8082         │      │
    │  └─────────────────┘  └─────────────────┘      │
    │  ┌─────────────────┐  ┌─────────────────┐      │
    │  │  应用中心服务    │  │  AIGC 服务      │      │
    │  └─────────────────┘  └─────────────────┘      │
    │  ┌─────────────────┐  ┌─────────────────┐      │
    │  │  治理服务        │  │  日志审计服务    │      │
    │  └─────────────────┘  └─────────────────┘      │
    └────────────────────────────────────────────────┘
             │
    ┌────────▼──────┐
    │   MySQL 数据库 │
    └───────────────┘
```

### 项目结构

```
intellihub-parent/
├── inner-intergration/                 # 内部集成模块
│   ├── common-helper/                  # 公共工具类
│   ├── common-dubbo-api/               # Dubbo 公共接口
│   ├── aop-spring-boot-starter/        # AOP 切面自动配置
│   ├── mybatis-helper-spring-boot-starter/  # MyBatis 增强
│   ├── redis-cache-spring-boot-starter/     # Redis 缓存
│   └── kafka-spring-boot-starter/      # Kafka 消息
├── intelli-gateway-service/            # API 网关服务 (8080)
├── intelli-auth-iam-service/           # 认证授权服务 (8081)
├── intelli-api-platform-service/       # API 管理服务 (8082)
├── intelli-app-center-service/         # 应用中心服务
├── intelli-aigc-service/               # AIGC 服务
├── intelli-governance-service/         # 治理服务
├── intelli-search-service/             # 搜索服务
├── intelli-event-service/              # 事件服务
└── intelli-log-audit-service/          # 日志审计服务
```

---

## 🚀 快速开始

### 环境要求

- **JDK**: 1.8+
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Redis**: 5.0+
- **Nacos**: 2.0+ (可选)

### 安装步骤

#### 1. 克隆项目

```bash
git clone https://github.com/your-org/intellihub-parent.git
cd intellihub-parent
```

#### 2. 配置数据库

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE intellihub_iam DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE intellihub_api DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入初始化脚本
mysql -u root -p intellihub_iam < intelli-auth-iam-service/src/main/resources/db/init.sql
mysql -u root -p intellihub_api < intelli-api-platform-service/src/main/resources/db/init.sql
```

#### 3. 修改配置文件

修改各服务 `application.yml` 中的数据库和 Redis 配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/intellihub_iam?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
```

#### 4. 启动 Nacos（可选）

```bash
# 下载 Nacos
wget https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.zip
unzip nacos-server-2.2.3.zip
cd nacos/bin

# 启动 Nacos（单机模式）
# Linux/Mac
sh startup.sh -m standalone

# Windows
startup.cmd -m standalone
```

访问 Nacos 控制台：http://localhost:8848/nacos (默认账号密码：nacos/nacos)

#### 5. 编译项目

```bash
mvn clean install -DskipTests
```

#### 6. 启动服务

按以下顺序启动各服务：

```bash
# 1. 启动认证服务
cd intelli-auth-iam-service
mvn spring-boot:run

# 2. 启动 API 平台服务
cd ../intelli-api-platform-service
mvn spring-boot:run

# 3. 启动网关服务
cd ../intelli-gateway-service
mvn spring-boot:run
```

#### 7. 验证服务

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 获取验证码
curl http://localhost:8080/api/iam/v1/auth/captcha

# 用户登录
curl -X POST http://localhost:8080/api/iam/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "captchaKey": "xxx",
    "captcha": "1234"
  }'
```

### Docker 部署（推荐）

```bash
# 构建镜像
docker-compose build

# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f
```

---

## 📚 文档

### 核心文档

- [开发问答文档](./API管理服务开发问答.md) - 项目常见问题和解答
- [技术分析报告](./项目技术分析报告.md) - 技术选型和架构分析
- [开发笔记](./DEVELOPMENT_NOTES.md) - 开发过程中的问题记录
- [网关分析报告](./网关模块分析报告.md) - 网关模块详细分析
- [多租户设计](./intelli-auth-iam-service/docs/MultiTenant.md) - 多租户隔离方案

### API 文档

启动服务后访问 Swagger 文档：

- 认证服务：http://localhost:8081/swagger-ui/index.html
- API 平台：http://localhost:8082/swagger-ui/index.html

### 核心接口

#### 认证相关

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/iam/v1/auth/login` | POST | 用户登录 |
| `/api/iam/v1/auth/register` | POST | 用户注册 |
| `/api/iam/v1/auth/captcha` | GET | 获取验证码 |
| `/api/iam/v1/auth/refresh` | POST | 刷新 Token |
| `/api/iam/v1/auth/logout` | POST | 退出登录 |

#### API 管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/apis` | GET | 获取 API 列表 |
| `/api/v1/apis` | POST | 创建 API |
| `/api/v1/apis/{id}` | PUT | 更新 API |
| `/api/v1/apis/{id}/publish` | POST | 发布 API |
| `/api/v1/apis/{id}/offline` | POST | 下线 API |

---

## 🔐 安全说明

### 认证流程

1. **用户登录** → 后端验证用户名密码（BCrypt）
2. **签发 Token** → 使用 HS512 算法生成 JWT Token
3. **客户端存储** → Token 保存在 localStorage
4. **请求携带** → 请求头添加 `Authorization: Bearer {token}`
5. **网关验证** → 网关调用认证服务验证 Token
6. **下游传递** → 网关添加用户信息到请求头（X-User-Id, X-Tenant-Id）

### 密码安全

- 使用 BCrypt 加密算法，自动加盐
- 密码强度要求：长度 >= 8，包含字母和数字
- 支持密码重置和修改

### Token 管理

- Access Token 有效期：2 小时
- Refresh Token 有效期：7 天
- 支持 Token 刷新和注销

---

## 🧪 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify

# 生成测试报告
mvn surefire-report:report
```

---

## 📈 性能优化

### 限流策略

基于 Redis 滑动窗口算法：

- IP 限流：100 次/分钟
- 路径限流：可配置
- 组合限流：IP + 路径

### 缓存策略

- 用户信息缓存：30 分钟
- API 路由缓存：10 分钟
- 验证码缓存：5 分钟

### 数据库优化

- 添加索引：username, email, path 等高频查询字段
- 分页查询：使用 MyBatis Plus 分页插件
- 读写分离：支持主从配置（规划中）

---

## 🛣️ 路线图

### v1.0（当前版本）

- ✅ 基础认证授权功能
- ✅ API 管理基础功能
- ✅ 网关路由转发
- ✅ 多租户支持
- ✅ 限流控制

### v1.1（规划中）

- ⏳ API Key 认证
- ⏳ 签名验证
- ⏳ Dubbo/gRPC 协议支持
- ⏳ API 调用统计
- ⏳ 服务熔断降级

### v2.0（未来规划）

- 📋 API 市场
- 📋 在线 API 测试
- 📋 自动生成 API 文档
- 📋 智能推荐
- 📋 分布式链路追踪

---

## 🤝 贡献指南

我们欢迎所有形式的贡献！

### 贡献方式

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

### 代码规范

- Java 代码遵循阿里巴巴 Java 开发手册
- 使用 4 空格缩进
- 提交信息格式：`[模块] 简短描述`
- 添加必要的单元测试

---

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。

---

## 👥 团队

- **核心开发者** - [Your Name](https://github.com/yourname)

---

## 📞 联系我们

- **问题反馈**: [GitHub Issues](https://github.com/your-org/intellihub-parent/issues)
- **邮件**: support@intellihub.com
- **文档**: [在线文档](https://docs.intellihub.com)

---

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Cloud](https://spring.io/projects/spring-cloud)
- [MyBatis Plus](https://baomidou.com/)
- [Nacos](https://nacos.io/)
- [Hutool](https://hutool.cn/)

---

<div align="center">

**如果这个项目对你有帮助，请给一个 ⭐️ Star！**

Made with ❤️ by IntelliHub Team

</div>
