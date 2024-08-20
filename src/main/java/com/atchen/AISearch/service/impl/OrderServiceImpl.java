package com.atchen.AISearch.service.impl;
import com.atchen.AISearch.entity.Department;
import com.atchen.AISearch.entity.Order;
import com.atchen.AISearch.mapper.OrderMapper;
import com.atchen.AISearch.service.IDepartmentService;
import com.atchen.AISearch.service.IOrderService;
import com.atchen.AISearch.util.ResultEntity;
import com.atchen.AISearch.util.SelectDateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
/**
 * <p>
 * 工单表 服务实现类
 * </p>
 *
 * @author atchen
 * @since 2024-08-20
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    @Resource
    private IDepartmentService departmentService;
    @Resource
    private IOrderService orderService;
    @Resource
    RedissonClient redissonClient;
    @Resource
    private OrderSummaryFunctionImpl<String> dailySummaryFunction;
    @Resource
    private OrderSummaryFunctionImpl<Long> deptSummaryFunction;
    @Resource
    private OrderSummaryFunctionImpl<Integer> typeSummaryFunction;

    // 检查orderno是否存在
    @Override
    public boolean checkOrderNoExists(String orderNo, Long id) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        if (id != null) {
            queryWrapper.ne("id", id);
        }
        return this.count(queryWrapper) > 0;
    }

    // 分派工单
    @Transactional
    @Override
    public ResultEntity fenpai(Long id, Long deptId, String deptName) {
        Department  re =  departmentService.getById(deptId);
        Optional<Department> department = Optional.ofNullable(re);
        if (!department.isPresent()) {
           return  ResultEntity.fail("部门ID无效");
        }
        Order order = this.getById(id);
        Optional<Order> optional = Optional.ofNullable(order);
        if (!optional.isPresent()){
            return ResultEntity.fail("工单ID无效");
        }else {
            // 加锁防止多人同时操作同一分派工单
            String lockKey = "order-fenpai-lock:"+ order.getId().toString();
            boolean res = false;
            RLock lock = redissonClient.getLock(lockKey);
            try {
                boolean tryLock = lock.tryLock(30, TimeUnit.SECONDS); // 尝试获取锁，最多等待30秒 watchDog
//                boolean tryLock = lock.tryLock(30, 10,TimeUnit.SECONDS);  重试加等待时间 不同业务场景不同方案
                if (!tryLock) {
                    return ResultEntity.fail("操作过于频繁，请稍后再试！");
                }
                order.setHandleDeptId(deptId);
                order.setFenpaiTime(new Date());
                res = this.updateById(order);
            }catch (Exception e){}
            finally {
                lock.unlock();
            }
            if (res){
                return ResultEntity.success(res);
            }
            return  ResultEntity.fail("订单无效");
        }
    }


    /*
        * @description 按月份查询每日工单总量、超期率
        *          优化代码，使用函数式接口使得业务代码更加简洁
        * @author atchen
        * @date 2024/8/20 20:31
    */
    @Override
    public List<Map<String, Object>> getDailySummaryForMonth(Date month) {
        List<Date> dateList = SelectDateUtil.selectDate(month);    //使用了自定义的日期选择封装类 util包下
        Date startDate = dateList.get(0);
        Date endDate = dateList.get(1);
        Function<Order, String> groupByFunction =
                workOrder -> new SimpleDateFormat("yyyy-MM-dd").format(workOrder.getCreateTime());
        return dailySummaryFunction.apply(startDate, endDate, groupByFunction);
    }
    
    /*
        * @description 按月份查询每个部门的工单总量、超期率
        * @author atchen
        * @date 2024/8/20 20:40
    */
   @Override
    public List<Map<String, Object>> getDeptSummaryForMonth(Date month) {
        List<Date> dateList = SelectDateUtil.selectDate(month);
        Date startDate = dateList.get(0);
        Date endDate = dateList.get(1);
        Function<Order, Long> groupByFunction = Order::getHandleDeptId;
        return deptSummaryFunction.apply(startDate, endDate, groupByFunction);
    }

    /*
        * @description 按月份查询每个部门的工单总量、超期率
        * @author atchen
        * @date 2024/8/20 20:44
    */
    @Override
    public List<Map<String, Object>> getTypeSummaryForMonth(Date month) {
        List<Date> dateList = SelectDateUtil.selectDate(month);
        Date startDate = dateList.get(0);
        Date endDate = dateList.get(1);
        Function<Order, Integer> groupByFunction = Order::getOrderType;
        return typeSummaryFunction.apply(startDate, endDate, groupByFunction);
    }
}


/*
 * @description 按月份查询每日工单总量、超期率  原始代码
 * @author atchen
 * @date 2024/8/20 19:40
 */
    /*public List<Map<String, Object>> getDailySummaryForMonth(Date month) {
        List<Date> dateList = SelectDateUtil.selectDate(month);
        Date startDate = dateList.get(0);
        Date endDate = dateList.get(1);
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("create_time", startDate, endDate);
        List<Order> workOrders = orderService.list(queryWrapper);  // 获取该月份的所有工单
        Map<String, Map<String, Integer>> dailySummary = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // total 工单总数，overdue 超期工单数
        // 遍历工单，按日期分组统计
        for (Order workOrder : workOrders) {
            String date = dateFormat.format(workOrder.getCreateTime());
            dailySummary.putIfAbsent(date, new HashMap<>());
            Map<String, Integer> summary = dailySummary.get(date);
            summary.put("total", summary.getOrDefault("total", 0) + 1);
            if (workOrder.getIsOverdue() == 1) {
                summary.put("overdue", summary.getOrDefault("overdue", 0) + 1);
            }
        }
        // 计算每日工单的超期率
        return dailySummary.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("date", entry.getKey());
                    result.put("total", entry.getValue().get("total"));
                    int total = entry.getValue().get("total");
                    int overdue = entry.getValue().getOrDefault("overdue", 0);
                    result.put("overdueRate", total == 0 ? 0 : (double) overdue / total);
                    return result;
                })
                .collect(Collectors.toList());
    }*/
