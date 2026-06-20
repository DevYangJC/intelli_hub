package com.intellihub.elasticsearch.config;

import com.intellihub.elasticsearch.core.ElasticsearchTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
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
@ConditionalOnClass(RestHighLevelClient.class)
@ConditionalOnProperty(prefix = "intellihub.elasticsearch", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ElasticsearchProperties.class)
public class ElasticsearchAutoConfiguration {

    /**
     * 创建 RestHighLevelClient
     */
    @Bean
    @ConditionalOnMissingBean
    public RestHighLevelClient restHighLevelClient(ElasticsearchProperties properties) {
        List<String> hosts = properties.getHosts();
        if (CollectionUtils.isEmpty(hosts)) {
            hosts.add("localhost:9200");
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

        log.info("初始化 RestHighLevelClient: hosts={}", hosts);
        return new RestHighLevelClient(builder);
    }

    /**
     * 创建 ElasticsearchTemplate
     */
    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchTemplate elasticsearchTemplate(RestHighLevelClient client,
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
