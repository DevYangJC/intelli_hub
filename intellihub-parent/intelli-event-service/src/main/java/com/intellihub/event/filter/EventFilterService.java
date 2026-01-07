package com.intellihub.event.filter;

import com.intellihub.event.model.EventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件过滤服务
 * <p>
 * 使用 Spring Expression Language (SpEL) 评估过滤表达式，
 * 决定事件是否应该被订阅者处理。
 * </p>
 * 
 * <h3>支持的表达式变量：</h3>
 * <ul>
 *   <li><b>#eventCode</b> - 事件编码</li>
 *   <li><b>#eventId</b> - 事件ID</li>
 *   <li><b>#source</b> - 事件来源</li>
 *   <li><b>#tenantId</b> - 租户ID</li>
 *   <li><b>#data</b> - 事件数据（Map类型）</li>
 * </ul>
 * 
 * <h3>表达式示例：</h3>
 * <pre>
 * // 只处理来自特定来源的事件
 * #source == 'order-service'
 * 
 * // 只处理 data 中 type 为 VIP 的事件
 * #data['type'] == 'VIP'
 * 
 * // 只处理金额大于100的订单事件
 * #data['amount'] > 100
 * 
 * // 组合条件
 * #source == 'order-service' and #data['amount'] > 100
 * 
 * // 检查字段是否存在
 * #data.containsKey('userId') and #data['userId'] != null
 * 
 * // 字符串匹配
 * #eventCode.startsWith('order.')
 * </pre>
 *
 * @author IntelliHub
 */
@Slf4j
@Service
public class EventFilterService {

    private final ExpressionParser parser = new SpelExpressionParser();
    
    /**
     * 表达式缓存，避免重复解析
     * Key: 表达式字符串, Value: 解析后的Expression对象
     */
    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    /**
     * 评估过滤表达式
     * <p>
     * 返回 true 表示事件应该被处理，false 表示事件应该被过滤掉。
     * 如果表达式为空或评估出错，默认返回 true（不过滤）。
     * </p>
     *
     * @param eventMessage     事件消息
     * @param filterExpression SpEL 过滤表达式
     * @return true-通过过滤（处理事件），false-被过滤掉（跳过事件）
     */
    public boolean evaluate(EventMessage eventMessage, String filterExpression) {
        // 空表达式默认通过
        if (!StringUtils.hasText(filterExpression)) {
            return true;
        }

        try {
            // 获取或解析表达式
            Expression expression = getExpression(filterExpression);
            
            // 构建评估上下文
            EvaluationContext context = buildContext(eventMessage);
            
            // 评估表达式
            Boolean result = expression.getValue(context, Boolean.class);
            
            if (result == null) {
                log.warn("过滤表达式返回 null，默认通过: expression={}", filterExpression);
                return true;
            }
            
            log.debug("过滤表达式评估完成: expression={}, eventCode={}, result={}", 
                    filterExpression, eventMessage.getEventCode(), result);
            
            return result;
        } catch (Exception e) {
            // 表达式评估出错，记录日志但不阻止事件处理
            log.error("过滤表达式评估失败，默认通过: expression={}, eventCode={}, error={}", 
                    filterExpression, eventMessage.getEventCode(), e.getMessage());
            return true;
        }
    }

    /**
     * 验证过滤表达式是否有效
     *
     * @param filterExpression SpEL 过滤表达式
     * @return true-有效，false-无效
     */
    public boolean isValidExpression(String filterExpression) {
        if (!StringUtils.hasText(filterExpression)) {
            return true;
        }
        
        try {
            parser.parseExpression(filterExpression);
            return true;
        } catch (Exception e) {
            log.warn("无效的过滤表达式: expression={}, error={}", filterExpression, e.getMessage());
            return false;
        }
    }

    /**
     * 获取或解析表达式（带缓存）
     */
    private Expression getExpression(String filterExpression) {
        return expressionCache.computeIfAbsent(filterExpression, parser::parseExpression);
    }

    /**
     * 构建 SpEL 评估上下文
     * <p>
     * 将事件消息的各个字段注册为变量，供表达式访问。
     * </p>
     */
    private EvaluationContext buildContext(EventMessage eventMessage) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        // 注册事件基本信息
        context.setVariable("eventCode", eventMessage.getEventCode());
        context.setVariable("eventId", eventMessage.getEventId());
        context.setVariable("source", eventMessage.getSource());
        context.setVariable("tenantId", eventMessage.getTenantId());
        context.setVariable("timestamp", eventMessage.getTimestamp());
        
        // 注册事件数据（Map类型）
        Map<String, Object> data = eventMessage.getData();
        context.setVariable("data", data != null ? data : Map.of());
        
        return context;
    }

    /**
     * 清除表达式缓存
     * <p>
     * 当订阅配置更新时，可以调用此方法清除缓存。
     * </p>
     */
    public void clearCache() {
        expressionCache.clear();
        log.info("过滤表达式缓存已清除");
    }

    /**
     * 清除指定表达式的缓存
     *
     * @param filterExpression 要清除的表达式
     */
    public void removeFromCache(String filterExpression) {
        if (StringUtils.hasText(filterExpression)) {
            expressionCache.remove(filterExpression);
        }
    }
}
