package com.intellihub.aigc.provider.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellihub.aigc.dto.request.ChatRequest;
import com.intellihub.aigc.dto.request.TextGenerationRequest;
import com.intellihub.aigc.dto.response.ChatResponse;
import com.intellihub.aigc.dto.response.TextGenerationResponse;
import com.intellihub.aigc.provider.ModelProviderService;
import com.intellihub.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 腾讯混元大模型提供商
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service("tencentHunyuanProvider")
public class TencentHunyuanProvider implements ModelProviderService {

    @Value("${aigc.tencent.secret-id:}")
    private String secretId;

    @Value("${aigc.tencent.secret-key:}")
    private String secretKey;

    // 方式1：使用OpenAI兼容接口（推荐）
    @Value("${aigc.tencent.use-openai-api:true}")
    private boolean useOpenAIApi;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)  // AI响应可能较慢
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private final Gson gson = new Gson();

    // OpenAI兼容接口（推荐）
    private static final String OPENAI_API_URL = "https://api.hunyuan.cloud.tencent.com/v1/chat/completions";
    
    // 腾讯云标准API（需要TC3签名）
    private static final String API_ENDPOINT = "hunyuan.tencentcloudapi.com";
    private static final String API_URL = "https://" + API_ENDPOINT;
    private static final String SERVICE = "hunyuan";
    private static final String VERSION = "2023-09-01";

    private static final List<String> SUPPORTED_MODELS = Arrays.asList(
            "hunyuan-lite",
            "hunyuan-standard",
            "hunyuan-standard-256K",
            "hunyuan-pro",
            "hunyuan-turbo",
            "hunyuan-turbo-latest"
    );

    @Override
    public TextGenerationResponse generateText(TextGenerationRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            if (useOpenAIApi) {
                // 使用OpenAI兼容接口
                return generateTextWithOpenAI(request, startTime);
            } else {
                // 使用腾讯云标准API
                return generateTextWithTC3(request, startTime);
            }
        } catch (IOException e) {
            log.error("调用腾讯混元API失败(IO) - model: {}, prompt长度: {}, 错误: {}", 
                    request.getModel(), request.getPrompt().length(), e.getMessage(), e);
            throw new BusinessException("调用AI模型失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("调用腾讯混元API失败 - model: {}, prompt长度: {}, 错误类型: {}, 错误: {}", 
                    request.getModel(), request.getPrompt().length(), e.getClass().getName(), e.getMessage(), e);
            throw new BusinessException("调用AI模型失败: " + e.getMessage());
        }
    }

    /**
     * 使用OpenAI兼容接口生成文本（推荐）
     */
    private TextGenerationResponse generateTextWithOpenAI(TextGenerationRequest request, long startTime) throws IOException {
        // 构建请求体
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", request.getModel());
        
        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", request.getPrompt());
        messages.add(userMessage);
        requestBody.add("messages", messages);
        
        if (request.getTemperature() != null) {
            requestBody.addProperty("temperature", request.getTemperature());
        }
        if (request.getTopP() != null) {
            requestBody.addProperty("top_p", request.getTopP());
        }

        String jsonBody = gson.toJson(requestBody);

        // 发送请求
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request httpRequest = new Request.Builder()
                .url(OPENAI_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + secretKey)  // OpenAI兼容接口使用API Key作为Bearer Token
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                log.error("腾讯混元API调用失败: code={}, errorBody={}", response.code(), errorBody);
                throw new BusinessException("腾讯混元API调用失败[" + response.code() + "]: " + errorBody);
            }
            
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            JsonObject firstChoice = choices.get(0).getAsJsonObject();
            JsonObject message = firstChoice.getAsJsonObject("message");
            JsonObject usage = jsonResponse.getAsJsonObject("usage");

            long duration = System.currentTimeMillis() - startTime;

            return TextGenerationResponse.builder()
                    .text(message.get("content").getAsString())
                    .tokensUsed(usage.get("total_tokens").getAsInt())
                    .model(request.getModel())
                    .requestId(jsonResponse.has("id") ? jsonResponse.get("id").getAsString() : null)
                    .duration(duration)
                    .finishReason(firstChoice.get("finish_reason").getAsString())
                    .build();
        }
    }

    /**
     * 使用腾讯云标准API生成文本（TC3签名）
     */
    private TextGenerationResponse generateTextWithTC3(TextGenerationRequest request, long startTime) throws Exception {
        try {
            // 构建消息
            JsonArray messages = new JsonArray();
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("Role", "user");
            userMessage.addProperty("Content", request.getPrompt());
            messages.add(userMessage);

            // 构建请求体
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("Model", request.getModel());
            requestBody.add("Messages", messages);
            requestBody.addProperty("Temperature", request.getTemperature());
            requestBody.addProperty("TopP", request.getTopP());

            String jsonBody = gson.toJson(requestBody);

            // 发送请求
            String responseBody = sendRequest("ChatCompletions", jsonBody);
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            JsonObject response = jsonResponse.getAsJsonObject("Response");
            JsonArray choices = response.getAsJsonArray("Choices");
            JsonObject firstChoice = choices.get(0).getAsJsonObject();
            JsonObject message = firstChoice.getAsJsonObject("Message");

            long duration = System.currentTimeMillis() - startTime;

            return TextGenerationResponse.builder()
                    .text(message.get("Content").getAsString())
                    .tokensUsed(response.getAsJsonObject("Usage").get("TotalTokens").getAsInt())
                    .model(request.getModel())
                    .requestId(response.get("RequestId").getAsString())
                    .duration(duration)
                    .finishReason(firstChoice.get("FinishReason").getAsString())
                    .build();
        } catch (Exception e) {
            log.error("调用腾讯混元API失败", e);
            throw new BusinessException("调用AI模型失败: " + e.getMessage());
        }
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            if (useOpenAIApi) {
                // 使用OpenAI兼容接口
                return chatWithOpenAI(request, startTime);
            } else {
                // 使用腾讯云标准API
                return chatWithTC3(request, startTime);
            }
        } catch (IOException e) {
            log.error("调用腾讯混元对话API失败(IO) - model: {}, message长度: {}, 错误: {}", 
                    request.getModel(), request.getMessage().length(), e.getMessage(), e);
            throw new BusinessException("调用AI对话失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("调用腾讯混元对话API失败 - model: {}, message长度: {}, 错误类型: {}, 错误: {}", 
                    request.getModel(), request.getMessage().length(), e.getClass().getName(), e.getMessage(), e);
            throw new BusinessException("调用AI对话失败: " + e.getMessage());
        }
    }

    /**
     * 使用OpenAI兼容接口进行对话（推荐）
     */
    private ChatResponse chatWithOpenAI(ChatRequest request, long startTime) throws IOException {
        // 构建消息列表
        JsonArray messages = new JsonArray();

        // 添加系统提示词
        if (request.getSystemPrompt() != null) {
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", request.getSystemPrompt());
            messages.add(systemMessage);
        }

        // 添加历史消息
        if (request.getHistory() != null) {
            for (ChatRequest.ChatMessage msg : request.getHistory()) {
                JsonObject historyMessage = new JsonObject();
                historyMessage.addProperty("role", msg.getRole());
                historyMessage.addProperty("content", msg.getContent());
                messages.add(historyMessage);
            }
        }

        // 添加当前用户消息
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", request.getMessage());
        messages.add(userMessage);

        // 构建请求体
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", request.getModel());
        requestBody.add("messages", messages);
        if (request.getTemperature() != null) {
            requestBody.addProperty("temperature", request.getTemperature());
        }

        String jsonBody = gson.toJson(requestBody);

        // 发送请求
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request httpRequest = new Request.Builder()
                .url(OPENAI_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + secretKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                log.error("腾讯混元API调用失败: code={}, errorBody={}", response.code(), errorBody);
                throw new BusinessException("腾讯混元API调用失败[" + response.code() + "]: " + errorBody);
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            JsonObject firstChoice = choices.get(0).getAsJsonObject();
            JsonObject message = firstChoice.getAsJsonObject("message");
            JsonObject usage = jsonResponse.getAsJsonObject("usage");

            long duration = System.currentTimeMillis() - startTime;

            return ChatResponse.builder()
                    .message(message.get("content").getAsString())
                    .conversationId(request.getConversationId())
                    .tokensUsed(usage.get("total_tokens").getAsInt())
                    .model(request.getModel())
                    .requestId(jsonResponse.has("id") ? jsonResponse.get("id").getAsString() : null)
                    .duration(duration)
                    .finishReason(firstChoice.get("finish_reason").getAsString())
                    .build();
        }
    }

    /**
     * 使用腾讯云标准API进行对话（TC3签名）
     */
    private ChatResponse chatWithTC3(ChatRequest request, long startTime) throws Exception {
        try {
            // 构建消息列表
            JsonArray messages = new JsonArray();

            // 添加系统提示词
            if (request.getSystemPrompt() != null) {
                JsonObject systemMessage = new JsonObject();
                systemMessage.addProperty("Role", "system");
                systemMessage.addProperty("Content", request.getSystemPrompt());
                messages.add(systemMessage);
            }

            // 添加历史消息
            if (request.getHistory() != null) {
                for (ChatRequest.ChatMessage msg : request.getHistory()) {
                    JsonObject historyMessage = new JsonObject();
                    historyMessage.addProperty("Role", msg.getRole());
                    historyMessage.addProperty("Content", msg.getContent());
                    messages.add(historyMessage);
                }
            }

            // 添加当前用户消息
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("Role", "user");
            userMessage.addProperty("Content", request.getMessage());
            messages.add(userMessage);

            // 构建请求体
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("Model", request.getModel());
            requestBody.add("Messages", messages);
            requestBody.addProperty("Temperature", request.getTemperature());

            String jsonBody = gson.toJson(requestBody);

            // 发送请求
            String responseBody = sendRequest("ChatCompletions", jsonBody);
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            JsonObject response = jsonResponse.getAsJsonObject("Response");
            JsonArray choices = response.getAsJsonArray("Choices");
            JsonObject firstChoice = choices.get(0).getAsJsonObject();
            JsonObject message = firstChoice.getAsJsonObject("Message");

            long duration = System.currentTimeMillis() - startTime;

            return ChatResponse.builder()
                    .message(message.get("Content").getAsString())
                    .conversationId(request.getConversationId())
                    .tokensUsed(response.getAsJsonObject("Usage").get("TotalTokens").getAsInt())
                    .model(request.getModel())
                    .requestId(response.get("RequestId").getAsString())
                    .duration(duration)
                    .finishReason(firstChoice.get("FinishReason").getAsString())
                    .build();
        } catch (Exception e) {
            log.error("调用腾讯混元对话API失败", e);
            throw new BusinessException("调用AI对话失败: " + e.getMessage());
        }
    }

    /**
     * 发送HTTP请求（使用腾讯云签名v3）
     */
    private String sendRequest(String action, String requestBody) throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        // 构建签名
        String authorization = buildAuthorization(action, requestBody, timestamp, date);

        // 构建HTTP请求
        RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", authorization)
                .addHeader("Content-Type", "application/json")
                .addHeader("Host", API_ENDPOINT)
                .addHeader("X-TC-Action", action)
                .addHeader("X-TC-Timestamp", timestamp)
                .addHeader("X-TC-Version", VERSION)
                .addHeader("X-TC-Region", "ap-guangzhou")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new BusinessException("腾讯混元API调用失败: " + response.code());
            }
            return response.body().string();
        }
    }

    /**
     * 构建腾讯云签名v3
     */
    private String buildAuthorization(String action, String requestBody, String timestamp, String date) throws Exception {
        // 1. 拼接规范请求串
        String httpRequestMethod = "POST";
        String canonicalUri = "/";
        String canonicalQueryString = "";
        String canonicalHeaders = "content-type:application/json\nhost:" + API_ENDPOINT + "\n";
        String signedHeaders = "content-type;host";
        String hashedRequestPayload = sha256Hex(requestBody);
        String canonicalRequest = httpRequestMethod + "\n" + canonicalUri + "\n" + canonicalQueryString + "\n"
                + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedRequestPayload;

        // 2. 拼接待签名字符串
        String algorithm = "TC3-HMAC-SHA256";
        String credentialScope = date + "/" + SERVICE + "/tc3_request";
        String hashedCanonicalRequest = sha256Hex(canonicalRequest);
        String stringToSign = algorithm + "\n" + timestamp + "\n" + credentialScope + "\n" + hashedCanonicalRequest;

        // 3. 计算签名
        byte[] secretDate = hmacSHA256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmacSHA256(secretDate, SERVICE);
        byte[] secretSigning = hmacSHA256(secretService, "tc3_request");
        String signature = bytesToHex(hmacSHA256(secretSigning, stringToSign));

        // 4. 拼接 Authorization
        return algorithm + " Credential=" + secretId + "/" + credentialScope
                + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;
    }

    /**
     * SHA256哈希
     */
    private String sha256Hex(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(d);
    }

    /**
     * HMAC-SHA256
     */
    private byte[] hmacSHA256(byte[] key, String msg) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, mac.getAlgorithm());
        mac.init(secretKeySpec);
        return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public String getProviderName() {
        return "腾讯混元";
    }

    @Override
    public boolean supportsModel(String modelName) {
        return SUPPORTED_MODELS.contains(modelName);
    }
}
