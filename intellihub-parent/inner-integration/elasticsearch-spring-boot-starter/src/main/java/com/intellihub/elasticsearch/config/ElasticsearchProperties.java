package com.intellihub.elasticsearch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Elasticsearch 配置属性
 *
 * @author IntelliHub
 */
@Data
@ConfigurationProperties(prefix = "intellihub.elasticsearch")
public class ElasticsearchProperties {

    /**
     * 是否启用 Elasticsearch
     */
    private boolean enabled = true;

    /**
     * ES 节点地址列表（格式：host:port）
     */
    private List<String> hosts = new ArrayList<>();

    /**
     * 用户名（可选）
     */
    private String username;

    /**
     * 密码（可选）
     */
    private String password;

    /**
     * 连接超时时间
     */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /**
     * Socket 超时时间
     */
    private Duration socketTimeout = Duration.ofSeconds(30);

    /**
     * 最大连接数
     */
    private int maxConnections = 100;

    /**
     * 每个路由的最大连接数
     */
    private int maxConnectionsPerRoute = 20;

    /**
     * 是否使用 SSL
     */
    private boolean useSsl = false;

    /**
     * 索引前缀（用于区分环境）
     */
    private String indexPrefix = "";

    /**
     * 默认分片数
     */
    private int defaultShards = 1;

    /**
     * 默认副本数
     */
    private int defaultReplicas = 0;

    /**
     * 批量操作大小
     */
    private int bulkBatchSize = 1000;

    /**
     * 刷新策略：true=立即刷新, false=等待刷新, wait_for=等待刷新完成
     */
    private String refreshPolicy = "false";
}
