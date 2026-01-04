package com.intellihub.gateway.service.dubbo.extractor;

import com.intellihub.gateway.service.dubbo.DubboInvocationContext;
import org.springframework.web.server.ServerWebExchange;

/**
 * 参数提取器接口
 * <p>
 * 责任链模式：定义从HTTP请求中提取Dubbo调用参数的标准接口
 * 不同的实现类负责从不同来源（路径、Query、Body等）提取参数
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface ParameterExtractor {

    /**
     * 获取提取器顺序（数值越小越先执行）
     *
     * @return 顺序值
     */
    int getOrder();

    /**
     * 从HTTP请求中提取参数并添加到上下文
     *
     * @param exchange HTTP交换对象
     * @param context  Dubbo调用上下文（参数将被添加到此对象）
     */
    void extract(ServerWebExchange exchange, DubboInvocationContext context);

    /**
     * 判断当前提取器是否支持处理该请求
     *
     * @param exchange HTTP交换对象
     * @param context  Dubbo调用上下文
     * @return true表示支持，false表示跳过
     */
    default boolean supports(ServerWebExchange exchange, DubboInvocationContext context) {
        return true;
    }
}
