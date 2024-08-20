package com.atchen.AISearch.service;

import com.atchen.AISearch.entity.Order;
import com.atchen.AISearch.util.ResultEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 工单表 服务类
 * </p>
 *
 * @author atchen
 * @since 2024-08-20
 */
public interface IOrderService extends IService<Order> {
    boolean checkOrderNoExists(String orderNo, Long id);
    ResultEntity fenpai(Long id, Long deptId, String deptName);

    List<Map<String, Object>> getDailySummaryForMonth(Date month);

    List<Map<String, Object>> getDeptSummaryForMonth(Date month);

    List<Map<String, Object>> getTypeSummaryForMonth(Date month);
}
