package com.intellihub.aigc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.aigc.entity.AigcRequestLog;
import com.intellihub.aigc.mapper.AigcRequestLogMapper;
import com.intellihub.aigc.service.CostAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

/**
 * 成本分析服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CostAnalysisServiceImpl implements CostAnalysisService {

    private final AigcRequestLogMapper requestLogMapper;

    @Override
    public Map<String, Object> getCostOverview(String tenantId, int days) {
        LocalDateTime startTime = LocalDateTime.of(LocalDate.now().minusDays(days), LocalTime.MIN);

        LambdaQueryWrapper<AigcRequestLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AigcRequestLog::getTenantId, tenantId)
                .ge(AigcRequestLog::getCreatedAt, startTime);

        List<AigcRequestLog> logs = requestLogMapper.selectList(wrapper);

        BigDecimal totalCost = logs.stream()
                .map(AigcRequestLog::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalTokens = logs.stream()
                .mapToInt(AigcRequestLog::getTokensUsed)
                .sum();

        long requestCount = logs.size();

        // 计算平均成本
        BigDecimal avgCost = requestCount > 0 
                ? totalCost.divide(BigDecimal.valueOf(requestCount), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 计算每1000 token成本
        BigDecimal costPer1000Tokens = totalTokens > 0
                ? totalCost.multiply(BigDecimal.valueOf(1000))
                        .divide(BigDecimal.valueOf(totalTokens), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> result = new HashMap<>();
        result.put("totalCost", totalCost);
        result.put("totalTokens", totalTokens);
        result.put("requestCount", requestCount);
        result.put("avgCost", avgCost);
        result.put("costPer1000Tokens", costPer1000Tokens);
        result.put("days", days);

        log.info("成本概览 - tenantId: {}, totalCost: {}, days: {}", tenantId, totalCost, days);
        return result;
    }

    @Override
    public Map<String, Object> getCostByModel(String tenantId, int days) {
        LocalDateTime startTime = LocalDateTime.of(LocalDate.now().minusDays(days), LocalTime.MIN);

        LambdaQueryWrapper<AigcRequestLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AigcRequestLog::getTenantId, tenantId)
                .ge(AigcRequestLog::getCreatedAt, startTime);

        List<AigcRequestLog> logs = requestLogMapper.selectList(wrapper);

        // 按模型统计成本
        Map<String, ModelCostStats> modelCostMap = new HashMap<>();
        for (AigcRequestLog log : logs) {
            String model = log.getModelName();
            ModelCostStats stats = modelCostMap.getOrDefault(model, new ModelCostStats());
            stats.cost = stats.cost.add(log.getCost());
            stats.tokens += log.getTokensUsed();
            stats.requests++;
            modelCostMap.put(model, stats);
        }

        // 转换为列表
        List<Map<String, Object>> costList = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;

        for (Map.Entry<String, ModelCostStats> entry : modelCostMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("model", entry.getKey());
            item.put("cost", entry.getValue().cost);
            item.put("tokens", entry.getValue().tokens);
            item.put("requests", entry.getValue().requests);
            
            // 计算平均成本
            BigDecimal avgCost = entry.getValue().requests > 0
                    ? entry.getValue().cost.divide(BigDecimal.valueOf(entry.getValue().requests), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            item.put("avgCost", avgCost);

            costList.add(item);
            totalCost = totalCost.add(entry.getValue().cost);
        }

        // 计算占比
        for (Map<String, Object> item : costList) {
            BigDecimal cost = (BigDecimal) item.get("cost");
            BigDecimal percentage = totalCost.compareTo(BigDecimal.ZERO) > 0
                    ? cost.multiply(BigDecimal.valueOf(100))
                            .divide(totalCost, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            item.put("percentage", percentage);
        }

        // 按成本降序排序
        costList.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return ((BigDecimal) o2.get("cost")).compareTo((BigDecimal) o1.get("cost"));
            }
        });

        Map<String, Object> result = new HashMap<>();
        result.put("costByModel", costList);
        result.put("totalCost", totalCost);
        result.put("modelCount", modelCostMap.size());

        return result;
    }

    @Override
    public Map<String, Object> getCostByDate(String tenantId, int days) {
        List<Map<String, Object>> dailyCost = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            LambdaQueryWrapper<AigcRequestLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AigcRequestLog::getTenantId, tenantId)
                    .ge(AigcRequestLog::getCreatedAt, startTime)
                    .le(AigcRequestLog::getCreatedAt, endTime);

            List<AigcRequestLog> logs = requestLogMapper.selectList(wrapper);

            BigDecimal cost = logs.stream()
                    .map(AigcRequestLog::getCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int tokens = logs.stream().mapToInt(AigcRequestLog::getTokensUsed).sum();

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("cost", cost);
            dayData.put("tokens", tokens);
            dayData.put("requests", logs.size());

            dailyCost.add(dayData);
            totalCost = totalCost.add(cost);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dailyCost", dailyCost);
        result.put("totalCost", totalCost);
        result.put("days", days);

        return result;
    }

    @Override
    public Map<String, Object> getCostForecast(String tenantId) {
        // 获取本月已用天数
        LocalDate today = LocalDate.now();
        int dayOfMonth = today.getDayOfMonth();
        
        // 本月总天数
        YearMonth yearMonth = YearMonth.from(today);
        int totalDaysInMonth = yearMonth.lengthOfMonth();

        // 获取本月已产生的成本
        LocalDateTime monthStart = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
        
        LambdaQueryWrapper<AigcRequestLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AigcRequestLog::getTenantId, tenantId)
                .ge(AigcRequestLog::getCreatedAt, monthStart);

        List<AigcRequestLog> logs = requestLogMapper.selectList(wrapper);

        BigDecimal currentCost = logs.stream()
                .map(AigcRequestLog::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算日均成本
        BigDecimal avgDailyCost = dayOfMonth > 0
                ? currentCost.divide(BigDecimal.valueOf(dayOfMonth), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 预测月底成本
        BigDecimal forecastCost = avgDailyCost.multiply(BigDecimal.valueOf(totalDaysInMonth));

        // 剩余预算（假设月预算为1000元）
        BigDecimal monthlyBudget = new BigDecimal("1000.00");
        BigDecimal remainingBudget = monthlyBudget.subtract(currentCost);

        Map<String, Object> result = new HashMap<>();
        result.put("currentCost", currentCost);
        result.put("avgDailyCost", avgDailyCost);
        result.put("forecastCost", forecastCost);
        result.put("monthlyBudget", monthlyBudget);
        result.put("remainingBudget", remainingBudget);
        result.put("daysUsed", dayOfMonth);
        result.put("daysRemaining", totalDaysInMonth - dayOfMonth);
        result.put("budgetUsagePercentage", 
                monthlyBudget.compareTo(BigDecimal.ZERO) > 0
                        ? currentCost.multiply(BigDecimal.valueOf(100))
                                .divide(monthlyBudget, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);

        log.info("成本预测 - tenantId: {}, currentCost: {}, forecastCost: {}", 
                tenantId, currentCost, forecastCost);
        return result;
    }

    @Override
    public Map<String, Object> exportCostReport(String tenantId, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        LocalDateTime startTime = LocalDateTime.of(start, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        LambdaQueryWrapper<AigcRequestLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AigcRequestLog::getTenantId, tenantId)
                .ge(AigcRequestLog::getCreatedAt, startTime)
                .le(AigcRequestLog::getCreatedAt, endTime)
                .orderByDesc(AigcRequestLog::getCreatedAt);

        List<AigcRequestLog> logs = requestLogMapper.selectList(wrapper);

        // 汇总数据
        BigDecimal totalCost = logs.stream()
                .map(AigcRequestLog::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalTokens = logs.stream()
                .mapToInt(AigcRequestLog::getTokensUsed)
                .sum();

        // 构建报表数据
        List<Map<String, Object>> details = new ArrayList<>();
        for (AigcRequestLog log : logs) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("requestId", log.getRequestId());
            detail.put("model", log.getModelName());
            detail.put("provider", log.getProvider());
            detail.put("tokens", log.getTokensUsed());
            detail.put("cost", log.getCost());
            detail.put("duration", log.getDuration());
            detail.put("status", log.getStatus());
            detail.put("createdAt", log.getCreatedAt().toString());
            details.add(detail);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("tenantId", tenantId);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("totalCost", totalCost);
        result.put("totalTokens", totalTokens);
        result.put("requestCount", logs.size());
        result.put("details", details);
        result.put("exportTime", LocalDateTime.now().toString());

        log.info("导出成本报表 - tenantId: {}, startDate: {}, endDate: {}, count: {}", 
                tenantId, startDate, endDate, logs.size());
        return result;
    }

    /**
     * 模型成本统计
     */
    private static class ModelCostStats {
        BigDecimal cost = BigDecimal.ZERO;
        int tokens = 0;
        long requests = 0;
    }
}
