package com.atchen.AISearch.service.impl;
import com.atchen.AISearch.entity.Order;
import com.atchen.AISearch.service.IOrderService;
import com.atchen.AISearch.service.OrderSummaryFunction;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
/*
    * @description 封装订单汇总函数函数式接口实现类
    * @author atchen
    * @date 2024/8/20 20:23
*/
@Component
public class OrderSummaryFunctionImpl<T> implements OrderSummaryFunction<T> {
    @Resource
    private IOrderService orderService;
    @Resource
    @Override
    public List<Map<String, Object>> apply(Date startDate, Date endDate,  Function<Order, T> groupByFunction) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("create_time", startDate, endDate);
        List<Order> workOrders = orderService.list(queryWrapper);

        Map<T, Map<String, Integer>> summary = new HashMap<>();

        for (Order workOrder : workOrders) {
            T key = groupByFunction.apply(workOrder);
            summary.putIfAbsent(key, new HashMap<>());
            Map<String, Integer> stats = summary.get(key);
            stats.put("total", stats.getOrDefault("total", 0) + 1);
            if (workOrder.getIsOverdue() == 1) {
                stats.put("overdue", stats.getOrDefault("overdue", 0) + 1);
            }
        }
        return summary.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("key", entry.getKey());
                    result.put("total", entry.getValue().get("total"));
                    int total = entry.getValue().get("total");
                    int overdue = entry.getValue().getOrDefault("overdue", 0);
                    result.put("overdueRate", total == 0 ? 0 : (double) overdue / total);
                    return result;
                })
                .collect(Collectors.toList());
    }
}
