package com.intellihub.search.health;

import com.intellihub.elasticsearch.core.ElasticsearchTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Elasticsearch健康检查
 *
 * @author IntelliHub
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchHealthIndicator implements HealthIndicator {

    private final ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public Health health() {
        try {
            // 检查ES连接
            boolean isAvailable = checkElasticsearchConnection();
            
            if (isAvailable) {
                return Health.up()
                        .withDetail("status", "Elasticsearch is available")
                        .build();
            } else {
                return Health.down()
                        .withDetail("status", "Elasticsearch is not available")
                        .build();
            }
        } catch (Exception e) {
            log.error("Elasticsearch健康检查失败: {}", e.getMessage());
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    /**
     * 检查Elasticsearch连接
     */
    private boolean checkElasticsearchConnection() {
        try {
            // 尝试检查索引是否存在来验证连接
            elasticsearchTemplate.indexExists("_test_health_check");
            return true;
        } catch (Exception e) {
            log.warn("Elasticsearch连接检查失败: {}", e.getMessage());
            return false;
        }
    }
}
