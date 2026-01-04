package com.intellihub.gateway.service.dubbo.strategy;

import com.intellihub.gateway.service.dubbo.DubboInvocationContext;
import org.apache.dubbo.rpc.service.GenericService;

/**
 * Dubbo调用策略接口
 * <p>
 * 策略模式：定义不同参数数量场景下的泛化调用策略
 * - 无参数调用
 * - 单参数调用
 * - 多参数调用
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface InvocationStrategy {

    /**
     * 判断当前策略是否支持处理该上下文
     *
     * @param context Dubbo调用上下文
     * @return true表示支持，false表示不支持
     */
    boolean supports(DubboInvocationContext context);

    /**
     * 执行Dubbo泛化调用
     *
     * @param genericService Dubbo泛化服务
     * @param context        调用上下文
     * @return 调用结果
     */
    Object invoke(GenericService genericService, DubboInvocationContext context);

    /**
     * 获取策略名称（用于日志）
     *
     * @return 策略名称
     */
    String getStrategyName();
}
