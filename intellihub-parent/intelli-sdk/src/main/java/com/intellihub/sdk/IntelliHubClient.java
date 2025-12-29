package com.intellihub.sdk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.sdk.exception.IntelliHubException;
import com.intellihub.sdk.model.ApiResponse;
import com.intellihub.sdk.util.SignatureUtils;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * IntelliHub API 客户端
 * <p>
 * 用于调用 IntelliHub 开放平台 API
 * </p>
 *
 * <pre>
 * // 使用示例
 * IntelliHubClient client = IntelliHubClient.create(
 *     IntelliHubConfig.builder()
 *         .baseUrl("https://api.intellihub.com")
 *         .appKey("your-app-key")
 *         .appSecret("your-app-secret")
 *         .build()
 * );
 *
 * // 调用 API
 * ApiResponse&lt;Map&gt; response = client.get("/open/v1/users", Map.class);
 * </pre>
 *
 * @author intellihub
 * @since 1.0.0
 */
public class IntelliHubClient {

    private static final Logger log = LoggerFactory.getLogger(IntelliHubClient.class);
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String HEADER_APP_KEY = "X-App-Key";
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String HEADER_NONCE = "X-Nonce";
    private static final String HEADER_SIGNATURE = "X-Signature";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private final IntelliHubConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private IntelliHubClient(IntelliHubConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * 创建客户端实例
     *
     * @param config 配置信息
     * @return 客户端实例
     */
    public static IntelliHubClient create(IntelliHubConfig config) {
        return new IntelliHubClient(config);
    }

    /**
     * 发送 GET 请求
     *
     * @param path         API 路径
     * @param responseType 响应数据类型
     * @param <T>          响应数据泛型
     * @return API 响应
     */
    public <T> ApiResponse<T> get(String path, Class<T> responseType) {
        return get(path, null, responseType);
    }

    /**
     * 发送 GET 请求（带查询参数）
     *
     * @param path         API 路径
     * @param queryParams  查询参数
     * @param responseType 响应数据类型
     * @param <T>          响应数据泛型
     * @return API 响应
     */
    public <T> ApiResponse<T> get(String path, Map<String, String> queryParams, Class<T> responseType) {
        String fullPath = buildPathWithQuery(path, queryParams);
        Request request = buildRequest("GET", fullPath, null);
        return execute(request, responseType);
    }

    /**
     * 发送 POST 请求
     *
     * @param path         API 路径
     * @param body         请求体
     * @param responseType 响应数据类型
     * @param <T>          响应数据泛型
     * @return API 响应
     */
    public <T> ApiResponse<T> post(String path, Object body, Class<T> responseType) {
        Request request = buildRequest("POST", path, body);
        return execute(request, responseType);
    }

    /**
     * 发送 PUT 请求
     *
     * @param path         API 路径
     * @param body         请求体
     * @param responseType 响应数据类型
     * @param <T>          响应数据泛型
     * @return API 响应
     */
    public <T> ApiResponse<T> put(String path, Object body, Class<T> responseType) {
        Request request = buildRequest("PUT", path, body);
        return execute(request, responseType);
    }

    /**
     * 发送 DELETE 请求
     *
     * @param path         API 路径
     * @param responseType 响应数据类型
     * @param <T>          响应数据泛型
     * @return API 响应
     */
    public <T> ApiResponse<T> delete(String path, Class<T> responseType) {
        Request request = buildRequest("DELETE", path, null);
        return execute(request, responseType);
    }

    /**
     * 发送原始请求（返回原始响应字符串）
     *
     * @param method 请求方法
     * @param path   API 路径
     * @param body   请求体（可为null）
     * @return 原始响应字符串
     */
    public String executeRaw(String method, String path, Object body) {
        Request request = buildRequest(method, path, body);
        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IntelliHubException("响应体为空");
            }
            return responseBody.string();
        } catch (IOException e) {
            throw new IntelliHubException("请求执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建请求
     */
    private Request buildRequest(String method, String path, Object body) {
        String url = config.getBaseUrl() + (path.startsWith("/") ? path : "/" + path);
        String timestamp = SignatureUtils.getTimestamp();
        String nonce = SignatureUtils.generateNonce();
        String signature = SignatureUtils.generateSignature(method, path, timestamp, nonce, config.getAppSecret());

        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader(HEADER_APP_KEY, config.getAppKey())
                .addHeader(HEADER_TIMESTAMP, timestamp)
                .addHeader(HEADER_NONCE, nonce)
                .addHeader(HEADER_SIGNATURE, signature)
                .addHeader(HEADER_CONTENT_TYPE, "application/json");

        RequestBody requestBody = null;
        if (body != null) {
            try {
                String json = objectMapper.writeValueAsString(body);
                requestBody = RequestBody.create(json, JSON);
            } catch (Exception e) {
                throw new IntelliHubException("序列化请求体失败", e);
            }
        }

        switch (method.toUpperCase()) {
            case "GET":
                builder.get();
                break;
            case "POST":
                builder.post(requestBody != null ? requestBody : RequestBody.create("", JSON));
                break;
            case "PUT":
                builder.put(requestBody != null ? requestBody : RequestBody.create("", JSON));
                break;
            case "DELETE":
                if (requestBody != null) {
                    builder.delete(requestBody);
                } else {
                    builder.delete();
                }
                break;
            default:
                throw new IntelliHubException("不支持的请求方法: " + method);
        }

        if (config.isEnableLog()) {
            log.info("IntelliHub SDK Request: {} {} | AppKey: {} | Timestamp: {} | Nonce: {}",
                    method, url, config.getAppKey(), timestamp, nonce);
        }

        return builder.build();
    }

    /**
     * 执行请求并解析响应
     */
    private <T> ApiResponse<T> execute(Request request, Class<T> responseType) {
        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IntelliHubException("响应体为空");
            }

            String bodyString = responseBody.string();

            if (config.isEnableLog()) {
                log.info("IntelliHub SDK Response: {} | Body: {}", response.code(), bodyString);
            }

            // 解析响应
            ApiResponse<T> apiResponse = objectMapper.readValue(bodyString,
                    objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, responseType));

            if (!apiResponse.isSuccess()) {
                throw new IntelliHubException(apiResponse.getCode(), apiResponse.getMessage());
            }

            return apiResponse;
        } catch (IntelliHubException e) {
            throw e;
        } catch (IOException e) {
            throw new IntelliHubException("请求执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建带查询参数的路径
     */
    private String buildPathWithQuery(String path, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return path;
        }

        StringBuilder sb = new StringBuilder(path);
        sb.append(path.contains("?") ? "&" : "?");
        
        boolean first = true;
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (!first) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        
        return sb.toString();
    }

    /**
     * 获取配置信息
     */
    public IntelliHubConfig getConfig() {
        return config;
    }
}
