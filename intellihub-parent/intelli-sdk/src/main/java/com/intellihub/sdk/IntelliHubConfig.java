package com.intellihub.sdk;

/**
 * IntelliHub SDK 配置类
 *
 * @author intellihub
 * @since 1.0.0
 */
public class IntelliHubConfig {

    private String baseUrl;
    private String appKey;
    private String appSecret;
    private int connectTimeout = 10000;
    private int readTimeout = 30000;
    private int writeTimeout = 30000;
    private boolean enableLog = false;

    private IntelliHubConfig() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public boolean isEnableLog() {
        return enableLog;
    }

    public static class Builder {
        private final IntelliHubConfig config = new IntelliHubConfig();

        public Builder baseUrl(String baseUrl) {
            config.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            return this;
        }

        public Builder appKey(String appKey) {
            config.appKey = appKey;
            return this;
        }

        public Builder appSecret(String appSecret) {
            config.appSecret = appSecret;
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {
            config.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            config.readTimeout = readTimeout;
            return this;
        }

        public Builder writeTimeout(int writeTimeout) {
            config.writeTimeout = writeTimeout;
            return this;
        }

        public Builder enableLog(boolean enableLog) {
            config.enableLog = enableLog;
            return this;
        }

        public IntelliHubConfig build() {
            if (config.baseUrl == null || config.baseUrl.isEmpty()) {
                throw new IllegalArgumentException("baseUrl is required");
            }
            if (config.appKey == null || config.appKey.isEmpty()) {
                throw new IllegalArgumentException("appKey is required");
            }
            if (config.appSecret == null || config.appSecret.isEmpty()) {
                throw new IllegalArgumentException("appSecret is required");
            }
            return config;
        }
    }
}
