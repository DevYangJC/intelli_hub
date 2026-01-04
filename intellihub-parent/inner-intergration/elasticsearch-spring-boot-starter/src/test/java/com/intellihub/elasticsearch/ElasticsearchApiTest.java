package com.intellihub.elasticsearch;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;

/**
 * Elasticsearch API 测试
 * 用于验证 Elasticsearch 7.4.2 的 API 调用
 */
public class ElasticsearchApiTest {

    public static void main(String[] args) {
        System.out.println("Testing Elasticsearch 7.4.2 API...");
        
        // 测试 TimeValue 类的方法
        TimeValue timeValue = TimeValue.timeValueMillis(100);
        
        // 尝试不同的方法
        try {
            long millis1 = timeValue.millis();
            System.out.println("timeValue.millis() works: " + millis1);
        } catch (Exception e) {
            System.out.println("timeValue.millis() failed: " + e.getMessage());
        }
        
        try {
            long millis2 = timeValue.getMillis();
            System.out.println("timeValue.getMillis() works: " + millis2);
        } catch (Exception e) {
            System.out.println("timeValue.getMillis() failed: " + e.getMessage());
        }
        
        // 检查 SearchResponse 类是否存在
        try {
            Class<?> clazz = SearchResponse.class;
            System.out.println("SearchResponse class found: " + clazz.getName());
            
            // 检查 getTook 方法
            java.lang.reflect.Method method = clazz.getMethod("getTook");
            System.out.println("getTook() method found, return type: " + method.getReturnType().getName());
        } catch (Exception e) {
            System.out.println("SearchResponse.getTook() check failed: " + e.getMessage());
        }
    }
}
