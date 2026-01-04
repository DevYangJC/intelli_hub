package com.intellihub.gateway.service.dubbo.strategy;

import com.intellihub.gateway.service.dubbo.DubboInvocationContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.stereotype.Component;

/**
 * 无参数调用策略
 * <p>
 * 处理无参数的Dubbo方法调用，如 getAll()、count() 等
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
public class NoArgInvocationStrategy implements InvocationStrategy {

    private static final String STRATEGY_NAME = "NoArgStrategy";

    @Override
    public boolean supports(DubboInvocationContext context) {
        return !context.hasParameters();
    }

    @Override
    public Object invoke(GenericService genericService, DubboInvocationContext context) {
        String methodName = context.getMethodName();
        
        log.debug("[{}] 执行无参数调用: method={}", STRATEGY_NAME, methodName);
        
        return genericService.$invoke(methodName, new String[]{}, new Object[]{});
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }
}
