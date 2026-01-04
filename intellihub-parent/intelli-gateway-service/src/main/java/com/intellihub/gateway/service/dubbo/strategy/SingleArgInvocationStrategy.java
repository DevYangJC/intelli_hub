package com.intellihub.gateway.service.dubbo.strategy;

import com.intellihub.gateway.service.dubbo.DubboInvocationContext;
import com.intellihub.gateway.service.dubbo.enums.ParameterType;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.stereotype.Component;

/**
 * 单参数调用策略
 * <p>
 * 处理单个参数的Dubbo方法调用，如 getById(String id)、getAppKeyInfo(String appKey) 等
 * 直接传递参数值，自动推断参数类型
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
public class SingleArgInvocationStrategy implements InvocationStrategy {

    private static final String STRATEGY_NAME = "SingleArgStrategy";

    @Override
    public boolean supports(DubboInvocationContext context) {
        return context.getParameterCount() == 1;
    }

    @Override
    public Object invoke(GenericService genericService, DubboInvocationContext context) {
        String methodName = context.getMethodName();
        Object value = context.getParameters().values().iterator().next();
        String paramName = context.getParameters().keySet().iterator().next();
        String paramType = ParameterType.getJavaTypeFromValue(value);

        log.debug("[{}] 执行单参数调用: method={}, paramName={}, paramType={}, value={}", 
                STRATEGY_NAME, methodName, paramName, paramType, value);

        return genericService.$invoke(methodName, new String[]{paramType}, new Object[]{value});
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }
}
