# AIGC 服务环境变量配置指南

## 📝 概述

为了保护 API Key 等敏感信息，AIGC 服务使用环境变量来管理配置。本文档说明如何配置这些环境变量。

## 🔑 需要配置的环境变量

### 数据库配置
```bash
DB_HOST=localhost              # 数据库地址
DB_PORT=3306                   # 数据库端口
DB_USERNAME=root               # 数据库用户名
DB_PASSWORD=your-db-password   # 数据库密码
```

### Redis 配置
```bash
REDIS_HOST=localhost           # Redis 地址
REDIS_PORT=6379               # Redis 端口
REDIS_PASSWORD=               # Redis 密码（如无密码则留空）
```

### AIGC API Key 配置

#### 阿里通义千问
```bash
ALIYUN_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```
- 获取地址：https://dashscope.console.aliyun.com/apiKey
- 注册阿里云账号并开通 DashScope 服务
- 在控制台创建 API Key

#### 百度文心一言
```bash
BAIDU_API_KEY=your-baidu-api-key
```
- 获取地址：https://console.bce.baidu.com/qianfan/
- 注册百度智能云账号并开通千帆大模型平台
- 在控制台-安全认证-API Key 创建 API Key

#### 腾讯混元
```bash
TENCENT_SECRET_ID=your-tencent-secret-id
TENCENT_SECRET_KEY=your-tencent-secret-key
```
- 获取地址：https://console.cloud.tencent.com/hunyuan
- 注册腾讯云账号并开通混元大模型服务
- 推荐使用 OpenAI 兼容接口模式

## 🚀 配置方法

### 方法一：在 IDEA 中配置（推荐用于开发）

1. 打开 Run/Debug Configurations
2. 选择你的启动配置
3. 找到 "Environment variables" 字段
4. 添加所需的环境变量，格式：`KEY1=value1;KEY2=value2`

例如：
```
ALIYUN_API_KEY=sk-xxx;BAIDU_API_KEY=bce-xxx;DB_PASSWORD=root123
```

### 方法二：使用 application-local.yml（推荐用于本地开发）

1. 复制 `application-example.yml` 文件
2. 重命名为 `application-local.yml`
3. 填入你的真实配置（该文件已在 .gitignore 中，不会被提交）
4. 启动时指定 profile：
   ```bash
   --spring.profiles.active=local
   ```

### 方法三：在系统中设置环境变量（推荐用于生产环境）

#### Linux / macOS
编辑 `~/.bashrc` 或 `~/.zshrc`：
```bash
export ALIYUN_API_KEY=sk-xxx
export BAIDU_API_KEY=bce-xxx
export DB_PASSWORD=root123
# ... 其他环境变量
```

然后执行：
```bash
source ~/.bashrc  # 或 source ~/.zshrc
```

#### Windows PowerShell
```powershell
$env:ALIYUN_API_KEY="sk-xxx"
$env:BAIDU_API_KEY="bce-xxx"
$env:DB_PASSWORD="root123"
```

或在系统环境变量中永久设置：
1. 右键"此电脑" -> 属性 -> 高级系统设置
2. 环境变量 -> 用户变量或系统变量
3. 新建变量名和值

### 方法四：使用 Docker 启动时传入

```bash
docker run -d \
  -e ALIYUN_API_KEY=sk-xxx \
  -e BAIDU_API_KEY=bce-xxx \
  -e DB_HOST=mysql \
  -e DB_PASSWORD=root123 \
  your-image-name
```

### 方法五：使用 .env 文件（推荐用于团队开发）

1. 在项目根目录创建 `.env` 文件（已在 .gitignore 中）
2. 添加环境变量：
   ```
   ALIYUN_API_KEY=sk-xxx
   BAIDU_API_KEY=bce-xxx
   DB_PASSWORD=root123
   ```
3. 使用工具加载（如 Spring Boot 的 `spring-dotenv` 插件）

## ⚠️ 安全注意事项

1. **永远不要提交包含真实 API Key 的配置文件到 Git**
2. **定期轮换 API Key**
3. **不同环境使用不同的 API Key**
4. **限制 API Key 的权限和配额**
5. **监控 API Key 的使用情况**

## 🔍 验证配置

启动服务后，查看日志输出，确认环境变量已正确加载：
```
[INFO] AIGC Service started successfully
[INFO] Aliyun API Key loaded: sk-xxxx...xxxx (已脱敏显示)
[INFO] Baidu API Key loaded: bce-xxxx...xxxx (已脱敏显示)
```

## 📞 问题排查

### 问题1：服务启动失败，提示 API Key 无效
- 检查环境变量是否正确设置
- 确认 API Key 是否有效且未过期
- 检查 API Key 是否有足够的权限和配额

### 问题2：环境变量未生效
- 确认环境变量名称拼写正确
- 重启 IDE 或终端
- 检查 profile 是否正确激活

### 问题3：Docker 容器中环境变量未生效
- 使用 `docker exec -it container_name env` 查看容器内环境变量
- 确认启动命令中 `-e` 参数正确
