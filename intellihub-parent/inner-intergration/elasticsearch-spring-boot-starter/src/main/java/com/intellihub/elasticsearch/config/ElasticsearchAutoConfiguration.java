package com.intellihub.elasticsearch.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.intellihub.elasticsearch.core.ElasticsearchTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Elasticsearch 自动配置
 *
 * @author IntelliHub
 */
@Slf4j
@Configuration
@ConditionalOnClass(ElasticsearchClient.class)
@ConditionalOnProperty(prefix = "intellihub.elasticsearch", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ElasticsearchProperties.class)
public class ElasticsearchAutoConfiguration {

    /**
     * 创建 RestClient
     */
    @Bean
    @ConditionalOnMissingBean
    public RestClient restClient(ElasticsearchProperties properties) {
        List<String> hosts = properties.getHosts();
        if (CollectionUtils.isEmpty(hosts)) {
            hosts = List.of("localhost:9200");
            log.warn("未配置 ES 地址，使用默认地址: localhost:9200");
        }

        HttpHost[] httpHosts = hosts.stream()
                .map(this::parseHost)
                .toArray(HttpHost[]::new);

        RestClientBuilder builder = RestClient.builder(httpHosts)
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout((int) properties.getConnectTimeout().toMillis())
                        .setSocketTimeout((int) properties.getSocketTimeout().toMillis()))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.setMaxConnTotal(properties.getMaxConnections());
                    httpClientBuilder.setMaxConnPerRoute(properties.getMaxConnectionsPerRoute());

                    // 认证配置
                    if (StringUtils.hasText(properties.getUsername()) 
                            && StringUtils.hasText(properties.getPassword())) {
                        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                        credentialsProvider.setCredentials(AuthScope.ANY,
                                new UsernamePasswordCredentials(
                                        properties.getUsername(),
                                        properties.getPassword()));
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }

                    return httpClientBuilder;
                });

        log.info("初始化 Elasticsearch RestClient: hosts={}", hosts);
        return builder.build();
    }

    /**
     * 创建 ElasticsearchTransport
     */
    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper));
    }

    /**
     * 创建 ElasticsearchClient
     */
    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        log.info("初始化 ElasticsearchClient");
        return new ElasticsearchClient(transport);
    }

    /**
     * 创建 ElasticsearchTemplate
     */
    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchTemplate elasticsearchTemplate(ElasticsearchClient client,
                                                       ElasticsearchProperties properties) {
        log.info("初始化 ElasticsearchTemplate, indexPrefix={}", properties.getIndexPrefix());
        return new ElasticsearchTemplate(client, properties);
    }

    /**
     * 解析主机地址
     */
    private HttpHost parseHost(String hostString) {
        String[] parts = hostString.split(":");
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;
        return new HttpHost(host, port, "http");
    }
}
