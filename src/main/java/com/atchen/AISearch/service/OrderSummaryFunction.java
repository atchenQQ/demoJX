package com.atchen.AISearch.service;
import com.atchen.AISearch.entity.Order;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
/*
    * @description 汇总函数的函数式接口
    * @author atchen
    * @date 2024/8/20 20:21
*/
public interface OrderSummaryFunction<T> {
    List<Map<String, Object>> apply(Date startDate, Date endDate, Function<Order, T> groupByFunction);
}
