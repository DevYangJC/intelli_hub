# IntelliHub Java SDK

IntelliHub 开放平台 Java SDK，提供简单易用的 API 调用能力。

## 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>com.intellihub</groupId>
    <artifactId>intelli-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 初始化客户端

```java
import com.intellihub.sdk.IntelliHubClient;
import com.intellihub.sdk.IntelliHubConfig;

// 创建配置
IntelliHubConfig config = IntelliHubConfig.builder()
    .baseUrl("https://api.intellihub.com")  // API 网关地址
    .appKey("your-app-key")                  // 应用 Key
    .appSecret("your-app-secret")            // 应用 Secret
    .connectTimeout(10000)                   // 连接超时（毫秒，可选）
    .readTimeout(30000)                      // 读取超时（毫秒，可选）
    .enableLog(true)                         // 启用日志（可选）
    .build();

// 创建客户端
IntelliHubClient client = IntelliHubClient.create(config);
```

## 使用示例

### GET 请求

```java
import com.intellihub.sdk.model.ApiResponse;
import java.util.Map;
import java.util.HashMap;

// 简单 GET 请求
ApiResponse<Map> response = client.get("/open/v1/users/info", Map.class);
if (response.isSuccess()) {
    Map data = response.getData();
    System.out.println("用户信息: " + data);
}

// 带查询参数的 GET 请求
Map<String, String> params = new HashMap<>();
params.put("page", "1");
params.put("size", "10");
ApiResponse<Map> listResponse = client.get("/open/v1/apis", params, Map.class);
```

### POST 请求

```java
// 创建请求体
Map<String, Object> body = new HashMap<>();
body.put("name", "测试API");
body.put("description", "这是一个测试API");

// 发送 POST 请求
ApiResponse<Map> response = client.post("/open/v1/apis", body, Map.class);
if (response.isSuccess()) {
    System.out.println("创建成功: " + response.getData());
}
```

### PUT 请求

```java
Map<String, Object> updateBody = new HashMap<>();
updateBody.put("name", "更新后的名称");

ApiResponse<Map> response = client.put("/open/v1/apis/123", updateBody, Map.class);
```

### DELETE 请求

```java
ApiResponse<Void> response = client.delete("/open/v1/apis/123", Void.class);
if (response.isSuccess()) {
    System.out.println("删除成功");
}
```

### 原始请求

```java
// 获取原始响应字符串
String rawResponse = client.executeRaw("GET", "/open/v1/apis", null);
System.out.println(rawResponse);
```

## 异常处理

```java
import com.intellihub.sdk.exception.IntelliHubException;

try {
    ApiResponse<Map> response = client.get("/open/v1/apis", Map.class);
} catch (IntelliHubException e) {
    System.err.println("错误码: " + e.getCode());
    System.err.println("错误信息: " + e.getErrorMessage());
}
```

## 认证机制

SDK 自动处理 API 认证，每次请求会自动添加以下请求头：

| 请求头 | 说明 |
|--------|------|
| X-App-Key | 应用 Key |
| X-Timestamp | 请求时间戳（毫秒） |
| X-Nonce | 随机字符串（防重放） |
| X-Signature | 请求签名 |

### 签名算法

```
签名 = HMAC-SHA256(METHOD + PATH + TIMESTAMP + NONCE, AppSecret)
```

示例：
```
METHOD = "GET"
PATH = "/open/v1/users"
TIMESTAMP = "1704067200000"
NONCE = "abc123def456"
AppSecret = "your-secret"

SignData = "GET/open/v1/users1704067200000abc123def456"
Signature = Base64(HMAC-SHA256(SignData, AppSecret))
```

## 配置选项

| 配置项 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| baseUrl | String | 是 | - | API 网关地址 |
| appKey | String | 是 | - | 应用 Key |
| appSecret | String | 是 | - | 应用 Secret |
| connectTimeout | int | 否 | 10000 | 连接超时（毫秒） |
| readTimeout | int | 否 | 30000 | 读取超时（毫秒） |
| writeTimeout | int | 否 | 30000 | 写入超时（毫秒） |
| enableLog | boolean | 否 | false | 是否启用日志 |

## 常见问题

### 1. 签名验证失败

- 检查 AppKey 和 AppSecret 是否正确
- 确保服务器时间与客户端时间同步（误差不超过5分钟）

### 2. 连接超时

- 检查网络连接
- 适当增加 connectTimeout 配置

### 3. 请求被拒绝

- 检查应用是否已订阅该 API
- 检查 API 调用配额是否用尽

## 更新日志

### v1.0.0
- 初始版本发布
- 支持 GET/POST/PUT/DELETE 请求
- 自动签名认证
- 异常处理

## 技术支持

如有问题，请联系：support@intellihub.com
