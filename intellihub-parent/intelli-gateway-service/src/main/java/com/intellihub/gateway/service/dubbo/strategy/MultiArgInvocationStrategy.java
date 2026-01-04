package com.intellihub.gateway.service.dubbo.strategy;

import com.intellihub.gateway.service.dubbo.DubboInvocationContext;
import com.intellihub.gateway.service.dubbo.enums.ParameterType;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

/**
 * 多参数调用策略
 * <p>
 * 处理多个参数的Dubbo方法调用，如 createUser(String name, Integer age) 等
 * 按参数顺序依次传递，自动推断每个参数的类型
 * 注意：参数顺序依赖于 LinkedHashMap 的插入顺序
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
public class MultiArgInvocationStrategy implements InvocationStrategy {

    private static final String STRATEGY_NAME = "MultiArgStrategy";

    @Override
    public boolean supports(DubboInvocationContext context) {
        return context.getParameterCount() > 1;
    }

    @Override
    public Object invoke(GenericService genericService, DubboInvocationContext context) {
        String methodName = context.getMethodName();
        Map<String, Object> params = context.getParameters();
        
        int size = params.size();
        String[] paramTypes = new String[size];
        Object[] paramValues = new Object[size];
        
        int i = 0;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            paramTypes[i] = ParameterType.getJavaTypeFromValue(entry.getValue());
            paramValues[i] = entry.getValue();
            i++;
        }

        log.debug("[{}] 执行多参数调用: method={}, paramCount={}, paramNames={}, paramTypes={}", 
                STRATEGY_NAME, methodName, size, params.keySet(), Arrays.toString(paramTypes));

        return genericService.$invoke(methodName, paramTypes, paramValues);
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }
}
