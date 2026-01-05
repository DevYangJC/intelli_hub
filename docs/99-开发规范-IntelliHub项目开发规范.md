# IntelliHub 项目开发规范

> 版本：1.0.0 | 更新日期：2026-01-05
> 本规范适用于 IntelliHub API 开放平台的所有开发人员

---

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 技术栈规范](#2-技术栈规范)
- [3. 项目结构规范](#3-项目结构规范)
- [4. 后端开发规范](#4-后端开发规范)
- [5. 前端开发规范](#5-前端开发规范)
- [6. API 设计规范](#6-api-设计规范)
- [7. 数据库规范](#7-数据库规范)
- [8. Git 工作流规范](#8-git-工作流规范)
- [9. 代码审查规范](#9-代码审查规范)
- [10. 部署与运维规范](#10-部署与运维规范)

---

## 1. 项目概述

### 1.1 项目定位

IntelliHub 是企业级 API 开放平台，提供：
- **统一入口**：所有请求通过网关统一处理
- **统一安全**：JWT/AppKey 双认证体系
- **统一治理**：限流、熔断、监控、告警

### 1.2 核心模块

| 模块 | 端口 | 职责 |
|------|:----:|------|
| intelli-gateway-service | 8080 | 统一网关 |
| intelli-auth-iam-service | 8081 | IAM 认证与权限 |
| intelli-api-platform-service | 8082 | API 生命周期管理 |
| intelli-governance-service | 8083 | 治理中心（统计/告警） |
| intelli-app-center-service | 8085 | 应用中心（AppKey/订阅） |
| intelli-search-service | 8086 | 聚合搜索 |
| intelli-event-service | 8087 | 事件中心 |
| intellihub-frontend | 5173 | Vue3 管理控制台 |

---

## 2. 技术栈规范

### 2.1 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| **JDK** | 1.8+ | 运行环境 |
| **Spring Boot** | 2.7.18 | 微服务基础框架 |
| **Spring Cloud** | 2021.0.9 | 微服务治理 |
| **Spring Cloud Alibaba** | 2021.0.6.0 | 阿里云微服务套件 |
| **Spring Cloud Gateway** | - | 响应式网关（WebFlux） |
| **Dubbo** | 3.1.11 | RPC 框架 |
| **MyBatis Plus** | 3.5.5 | ORM 增强 |
| **MySQL** | 8.0+ | 主数据库 |
| **Redis** | 6.0+ | 缓存/限流/统计 |
| **Kafka** | 2.x | 异步消息 |
| **Nacos** | 2.x | 注册/配置中心 |
| **Lombok** | 1.18.24 | 代码简化 |
| **Hutool** | 5.8.22 | 工具库 |

### 2.2 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| **Node.js** | ^20.19.0 或 >=22.12.0 | 运行环境 |
| **Vue** | 3.5+ | 前端框架 |
| **Vite** | 7.x | 构建工具 |
| **TypeScript** | 5.9+ | 类型系统 |
| **Element Plus** | 2.12+ | UI 组件库 |
| **Pinia** | 3.x | 状态管理 |
| **Vue Router** | 4.x | 路由管理 |
| **Axios** | 1.x | HTTP 客户端 |
| **ECharts** | 6.x | 数据可视化 |
| **ESLint** | 9.x | 代码检查 |
| **Prettier** | 3.x | 代码格式化 |

---

## 3. 项目结构规范

### 3.1 后端模块结构

```
intellihub-parent/
├── pom.xml                           # 父 POM（依赖版本管理）
├── inner-intergration/               # 内部支撑模块
│   ├── common-helper/                # 通用工具与响应体
│   ├── common-dubbo-api/             # Dubbo 公共接口与 DTO
│   ├── mybatis-helper-spring-boot-starter/
│   ├── redis-cache-spring-boot-starter/
│   ├── kafka-spring-boot-starter/
│   ├── elasticsearch-spring-boot-starter/
│   └── aop-spring-boot-starter/
├── intelli-sdk/                      # Java SDK
└── intelli-*-service/                # 业务服务模块
```

### 3.2 单个服务模块结构

```
intelli-xxx-service/
├── src/main/java/com/intellihub/xxx/
│   ├── XxxApplication.java           # 启动类
│   ├── config/                       # 配置类
│   ├── controller/                   # REST 控制器
│   ├── service/                      # 业务服务接口
│   │   └── impl/                     # 服务实现
│   ├── mapper/                       # MyBatis Mapper
│   ├── entity/                       # 数据库实体
│   ├── dto/                          # 数据传输对象
│   │   ├── request/                  # 请求 DTO
│   │   └── response/                 # 响应 DTO
│   ├── dubbo/                        # Dubbo 服务实现
│   └── util/                         # 工具类
├── src/main/resources/
│   ├── application.yml               # 配置文件
│   └── mapper/                       # MyBatis XML
└── pom.xml
```

### 3.3 前端项目结构

```
intellihub-frontend/
├── src/
│   ├── main.ts                       # 应用入口
│   ├── App.vue                       # 根组件
│   ├── api/                          # API 接口定义
│   ├── assets/                       # 静态资源
│   ├── components/                   # 公共组件
│   ├── layouts/                      # 布局组件
│   ├── router/                       # 路由配置
│   ├── stores/                       # Pinia 状态管理
│   └── views/                        # 页面视图
│       └── console/                  # 控制台页面
│           ├── api/                  # API 管理
│           ├── app/                  # 应用管理
│           ├── alert/                # 告警管理
│           ├── gateway/              # 网关管理
│           ├── stats/                # 统计分析
│           ├── tenant/               # 租户管理
│           └── user/                 # 用户管理
├── public/                           # 公共静态资源
├── package.json
├── vite.config.ts
├── tsconfig.json
└── eslint.config.ts
```

---

## 4. 后端开发规范

### 4.1 命名规范

#### 包命名
```
com.intellihub.{模块名}.{层级}
例：com.intellihub.auth.controller
```

#### 类命名
| 类型 | 规范 | 示例 |
|------|------|------|
| 控制器 | `{业务}Controller` | `AuthController` |
| 服务接口 | `{业务}Service` | `AuthService` |
| 服务实现 | `{业务}ServiceImpl` | `AuthServiceImpl` |
| Mapper | `{实体}Mapper` | `UserMapper` |
| 实体 | `{业务名}` | `User`, `ApiInfo` |
| 请求 DTO | `{操作}{业务}Request` | `CreateUserRequest` |
| 响应 DTO | `{业务}Response` | `LoginResponse` |
| 枚举 | `{业务}{类型}` | `ApiStatus`, `AppType` |
| 工具类 | `{功能}Util` | `SignatureUtil` |
| 常量类 | `{范围}Constants` | `RedisKeyConstants` |

#### 方法命名
```java
// Service 层
list{业务}()          // 列表查询
get{业务}ById()       // 单个查询
create{业务}()        // 创建
update{业务}()        // 更新
delete{业务}()        // 删除

// Controller 层 REST 风格
@GetMapping("/xxx")      // 查询
@PostMapping("/xxx")     // 创建
@PutMapping("/xxx/{id}") // 更新
@DeleteMapping("/xxx/{id}") // 删除
```

### 4.2 代码风格

#### 类结构顺序
```java
public class XxxController {
    // 1. 常量（static final）
    // 2. 静态变量
    // 3. 实例变量（依赖注入）
    // 4. 构造器
    // 5. 公共方法
    // 6. 私有方法
}
```

#### 依赖注入
```java
// ✅ 推荐：构造器注入 + Lombok
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
}

// ❌ 不推荐：字段注入
@Autowired
private AuthService authService;
```

#### Lombok 使用
```java
// 实体类
@Data
public class User { }

// 构建器模式
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRouteDTO { }
```

### 4.3 统一响应规范

#### 响应体结构
```java
{
    "code": 200,           // 状态码
    "message": "操作成功",  // 消息
    "data": { },           // 数据
    "timestamp": 1704412800000  // 时间戳
}
```

#### 使用方式
```java
// 成功响应
return ApiResponse.success(data);
return ApiResponse.success("操作成功", data);

// 失败响应
return ApiResponse.error(ResultCode.PARAM_ERROR);
return ApiResponse.failed(ResultCode.UNAUTHORIZED, "Token已过期");
```

#### 状态码定义
| 范围 | 类型 | 示例 |
|------|------|------|
| 200 | 成功 | 操作成功 |
| 400-499 | 客户端错误 | 参数错误、未授权 |
| 500-599 | 服务端错误 | 系统异常 |
| 1000+ | 业务错误 | 业务逻辑错误 |

### 4.4 异常处理规范

#### 异常类型
```java
// 业务异常
throw new BusinessException(ResultCode.PARAM_ERROR, "参数不能为空");

// 认证异常
throw new UnauthorizedException("Token无效");
```

#### 全局异常处理
- 使用 `GlobalExceptionHandler` 统一处理
- 所有服务必须配置 `scanBasePackages = {"com.intellihub"}` 以加载全局异常处理器

```java
@SpringBootApplication(scanBasePackages = {"com.intellihub"})
public class IntelliAuthIamServiceApplication { }
```

### 4.5 日志规范

```java
// 使用 Slf4j
@Slf4j
public class AuthServiceImpl {
    
    public void login() {
        log.info("用户登录: username={}", username);
        log.warn("登录失败: username={}, reason={}", username, reason);
        log.error("系统异常", exception);
    }
}
```

#### 日志级别
| 级别 | 用途 |
|------|------|
| ERROR | 系统异常、需要告警的错误 |
| WARN | 潜在问题、业务异常 |
| INFO | 关键业务流程、重要操作 |
| DEBUG | 调试信息（生产环境关闭） |

### 4.6 参数校验

```java
// DTO 校验注解
public class CreateUserRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String password;
    
    @Email(message = "邮箱格式不正确")
    private String email;
}

// Controller 使用 @Valid
@PostMapping("/users")
public ApiResponse<Void> create(@Valid @RequestBody CreateUserRequest request) { }
```

### 4.7 MyBatis Plus 规范

#### 实体类
```java
@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
```

#### 分页查询
```java
Page<User> page = new Page<>(pageNum, pageSize);
IPage<User> result = userMapper.selectPage(page, queryWrapper);
```

### 4.8 Dubbo 服务规范

#### 接口定义（common-dubbo-api）
```java
public interface OpenApiRouteService {
    ApiRouteDTO matchRouteByPath(String path, String method);
}
```

#### 服务提供者
```java
@DubboService
public class OpenApiRouteServiceImpl implements OpenApiRouteService {
    @Override
    public ApiRouteDTO matchRouteByPath(String path, String method) { }
}
```

#### 服务消费者
```java
@DubboReference
private OpenApiRouteService openApiRouteService;
```

---

## 5. 前端开发规范

### 5.1 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 组件文件 | PascalCase | `ApiListPage.vue` |
| 页面组件 | `{功能}Page.vue` | `UserListPage.vue` |
| 公共组件 | `{功能}.vue` | `SearchBar.vue` |
| API 文件 | camelCase | `apiManage.ts` |
| Store 文件 | camelCase | `userStore.ts` |
| 变量/函数 | camelCase | `handleSubmit` |
| 常量 | UPPER_SNAKE_CASE | `API_BASE_URL` |

### 5.2 Vue 组件规范

#### 组件结构
```vue
<template>
  <!-- 模板内容 -->
</template>

<script setup lang="ts">
// 1. 导入
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 2. Props & Emits
const props = defineProps<{
  id: number
}>()
const emit = defineEmits<{
  (e: 'update', value: string): void
}>()

// 3. 响应式状态
const loading = ref(false)
const formData = reactive({ })

// 4. 计算属性
const computedValue = computed(() => { })

// 5. 方法
const handleSubmit = async () => { }

// 6. 生命周期
onMounted(() => { })
</script>

<style scoped>
/* 样式 */
</style>
```

#### 组件原则
- 使用 `<script setup>` 语法
- 使用 Composition API
- 组件职责单一，保持简洁
- Props 必须定义类型

### 5.3 TypeScript 规范

#### 类型定义
```typescript
// 接口定义
interface ApiInfoResponse {
  id: number
  name: string
  path: string
  method: string
  status: 'draft' | 'published' | 'offline' | 'deprecated'
}

// 类型导出
export type { ApiInfoResponse }
```

#### API 调用
```typescript
// src/api/apiManage.ts
import request from './request'

export const apiManageApi = {
  list(params: ApiListParams): Promise<ApiResponse<PageData<ApiInfoResponse>>> {
    return request.get('/api/v1/apis', { params })
  },
  
  create(data: CreateApiRequest): Promise<ApiResponse<void>> {
    return request.post('/api/v1/apis', data)
  }
}
```

### 5.4 状态管理（Pinia）

```typescript
// src/stores/userStore.ts
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userInfo: null as UserInfo | null,
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
  },
  
  actions: {
    async login(params: LoginParams) {
      const res = await authApi.login(params)
      this.token = res.data.token
      this.userInfo = res.data.userInfo
    },
    
    logout() {
      this.token = ''
      this.userInfo = null
    },
  },
})
```

### 5.5 路由规范

```typescript
// 路由懒加载
{
  path: '/console/api',
  name: 'ApiList',
  component: () => import('@/views/console/api/ApiListPage.vue'),
  meta: {
    title: 'API列表',
    requiresAuth: true,
  }
}
```

### 5.6 样式规范

```vue
<style scoped>
/* 使用 scoped 避免样式污染 */
.page-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

/* 使用 :deep() 修改子组件样式 */
:deep(.el-table) {
  --el-table-header-bg-color: #f5f7fa;
}
</style>
```

#### 样式原则
- 优先使用 Element Plus 组件
- 使用 `scoped` 限制样式作用域
- 颜色使用 CSS 变量或 Element Plus 变量
- 间距统一使用 8px 倍数

### 5.7 代码检查

```bash
# ESLint 检查并修复
npm run lint

# Prettier 格式化
npm run format

# 类型检查
npm run type-check
```

---

## 6. API 设计规范

### 6.1 RESTful 规范

| 操作 | HTTP 方法 | 路径示例 |
|------|-----------|----------|
| 列表查询 | GET | `/api/v1/apis` |
| 详情查询 | GET | `/api/v1/apis/{id}` |
| 创建 | POST | `/api/v1/apis` |
| 更新 | PUT | `/api/v1/apis/{id}` |
| 删除 | DELETE | `/api/v1/apis/{id}` |
| 特定操作 | POST | `/api/v1/apis/{id}/publish` |

### 6.2 路径规范

```
/api/{服务简称}/v{版本号}/{资源}

示例：
/api/iam/v1/users        # IAM 服务用户接口
/api/platform/v1/apis    # API 平台接口
/api/governance/v1/stats # 治理服务统计接口
```

### 6.3 请求/响应规范

#### 分页请求
```json
{
  "page": 1,
  "size": 10,
  "keyword": "搜索关键词",
  "status": "published"
}
```

#### 分页响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

### 6.4 开放 API 规范

#### 认证头
```
X-App-Key: {appKey}
X-Timestamp: {timestamp}
X-Nonce: {nonce}
X-Signature: {signature}
```

#### 签名算法
```
signature = HMAC-SHA256(appSecret, sortedParams + timestamp + nonce)
```

---

## 7. 数据库规范

### 7.1 表命名

| 前缀 | 含义 | 示例 |
|------|------|------|
| `sys_` | 系统表 | `sys_user`, `sys_role` |
| `api_` | API 相关 | `api_info`, `api_group` |
| `app_` | 应用相关 | `app_info`, `app_subscription` |
| `log_` | 日志表 | `log_api_call` |
| `cfg_` | 配置表 | `cfg_alert_rule` |

### 7.2 字段规范

#### 必备字段
```sql
id              BIGINT PRIMARY KEY AUTO_INCREMENT,
tenant_id       BIGINT NOT NULL COMMENT '租户ID',
created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
created_by      BIGINT COMMENT '创建人',
updated_by      BIGINT COMMENT '更新人',
deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除'
```

#### 命名规则
- 使用小写字母 + 下划线（snake_case）
- 主键统一为 `id`
- 外键命名：`{关联表}_id`
- 状态字段：`status`
- 布尔字段：`is_xxx` 或 `xxx_flag`

### 7.3 索引规范

```sql
-- 普通索引
INDEX idx_{表名}_{字段名} (字段名)

-- 组合索引
INDEX idx_{表名}_{字段1}_{字段2} (字段1, 字段2)

-- 唯一索引
UNIQUE INDEX uk_{表名}_{字段名} (字段名)
```

### 7.4 Redis 规范

#### Key 命名
```
{业务}:{子业务}:{标识}

示例：
auth:token:{userId}
api:route:{path}:{method}
alert:stats:{apiId}:{date}
rate_limit:ip:{ip}
```

#### 数据库分配
| DB | 用途 |
|----|------|
| 0 | Gateway（告警统计/限流/缓存） |
| 1 | IAM / API Platform |
| 2 | App Center |

---

## 8. Git 工作流规范

### 8.1 分支规范

| 分支 | 用途 | 命名示例 |
|------|------|----------|
| `main` | 生产分支 | - |
| `develop` | 开发分支 | - |
| `feature/*` | 功能分支 | `feature/user-management` |
| `bugfix/*` | 缺陷修复 | `bugfix/login-error` |
| `hotfix/*` | 紧急修复 | `hotfix/security-patch` |
| `release/*` | 发布分支 | `release/v1.0.0` |

### 8.2 Commit 规范

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Type 类型
| 类型 | 说明 |
|------|------|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `docs` | 文档变更 |
| `style` | 代码格式（不影响逻辑） |
| `refactor` | 重构（非新功能/非修复） |
| `perf` | 性能优化 |
| `test` | 测试相关 |
| `chore` | 构建/工具变更 |

#### 示例
```
feat(auth): 添加用户登录功能

- 实现 JWT Token 签发
- 添加登录接口 /iam/v1/auth/login
- 集成验证码校验

Closes #123
```

### 8.3 PR 规范

```markdown
### 变更说明
- 描述本次变更的内容

### 涉及模块
- [ ] Gateway
- [ ] IAM
- [ ] API Platform
- [ ] Frontend

### 测试验证
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 手动测试验证

### 风险评估
- 影响范围：
- 兼容性：
```

---

## 9. 代码审查规范

### 9.1 审查要点

#### 功能正确性
- [ ] 业务逻辑是否正确
- [ ] 边界条件是否处理
- [ ] 异常是否正确处理

#### 代码质量
- [ ] 命名是否清晰
- [ ] 是否遵循现有风格
- [ ] 是否有重复代码
- [ ] 是否有魔法数字

#### 安全性
- [ ] SQL 注入风险
- [ ] 权限校验是否完整
- [ ] 敏感信息是否脱敏

#### 性能
- [ ] N+1 查询问题
- [ ] 是否需要缓存
- [ ] 是否有大事务

### 9.2 审查流程

1. 提交 PR
2. 自动化检查（Lint/Test）
3. 至少 1 人 Code Review
4. 解决所有评论
5. 合并到目标分支

---

## 10. 部署与运维规范

### 10.1 环境配置

```yaml
# application.yml 配置分离
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  config:
    import: optional:nacos:${spring.application.name}.yml
```

#### 环境变量
```bash
SPRING_PROFILES_ACTIVE=prod
MYSQL_HOST=localhost
MYSQL_PORT=3306
REDIS_HOST=localhost
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
NACOS_SERVER_ADDR=localhost:8848
```

### 10.2 服务启动顺序

1. 基础设施：MySQL / Redis / Kafka / Nacos
2. IAM 认证服务
3. API 平台服务
4. 应用中心服务
5. 治理中心服务
6. 网关服务（最后启动）
7. 前端服务

### 10.3 健康检查

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when_authorized
```

### 10.4 日志配置

```yaml
logging:
  level:
    root: INFO
    com.intellihub: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

---

## 附录

### A. 常用命令

```bash
# 后端构建
cd intellihub-parent
mvn clean package -DskipTests

# 单服务启动
cd intelli-auth-iam-service
mvn spring-boot:run

# 前端开发
cd intellihub-frontend
npm install
npm run dev

# 前端构建
npm run build
```

### B. 常用端口

| 服务 | 端口 |
|------|------|
| Gateway | 8080 |
| IAM | 8081 |
| API Platform | 8082 |
| Governance | 8083 |
| AIGC | 8084 |
| App Center | 8085 |
| Search | 8086 |
| Event | 8087 |
| Log Audit | 8088 |
| Frontend | 5173 |
| MySQL | 3306 |
| Redis | 6379 |
| Kafka | 9092 |
| Nacos | 8848 |

### C. 参考文档

- [项目架构文档](./10-架构设计-项目架构文档.md)
- [网关技术流程说明书](./20-技术流程-网关技术流程说明书.md)
- [告警系统流程说明书](./21-技术流程-告警系统流程说明书.md)
- [API 下发指南](./22-技术流程-API下发指南.md)

---

*本规范由项目组共同维护，如有疑问或建议请提交 Issue 讨论。*
