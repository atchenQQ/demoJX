package com.atchen.AISearch.controller;
import com.atchen.AISearch.entity.Order;
import com.atchen.AISearch.service.IOrderService;
import com.atchen.AISearch.util.GlobalUtil;
import com.atchen.AISearch.util.ResultEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.atchen.AISearch.util.GlobalUtil.EMPTY_RESULT;

/**
 * <p>
 * 工单表 前端控制器
 * </p>
 *
 * @author atchen
 * @since 2024-08-20
 */
@Controller
@RequestMapping("/order")
public class OrderController {
    @Resource
    private IOrderService orderService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RedisTemplate redisTemplate;


    //TODO:  swagger接口文档 略


    /*
        * @description  并发场景设计，按月份查询每个部门的工单总量、超期率
        * 注: month参数为输入的月份,由前端传递，若为7，则返回7月份的数据
        * @author atchen
        * @date 2024/8/21
    */
    public ResultEntity getDeptSummary(@RequestParam @DateTimeFormat(pattern = "yyyy-MM") Date month) {
        if (month == null) {
            return ResultEntity.fail("请输入月份");
        }
        String redisKey = "deptSummary:" + new SimpleDateFormat("yyyy-MM").format(month);
        String REDIS_WRITE_LOCK_KEY = redisKey + "REDIS_WRITE_LOCK_KEY";  // 缓存写入标识 value为1表示有线程在写入缓存

        // 先从redis缓存中获取数据
        List<Map<String, Object>> cachedResult = (List<Map<String, Object>>)
                redisTemplate.opsForValue().get(redisKey);  // json数据可能需要解析
        // 如果缓存中有数据，直接返回缓存数据
        if (cachedResult != null) {
            if (GlobalUtil.EMPTY_RESULT.equals(cachedResult)) {
                return ResultEntity.fail("该月份没有数据");
            }
            return ResultEntity.success(cachedResult);
        }
        // 如果缓存中没有数据，查询数据库并缓存
        // 防止缓存击穿
        List<Map<String, Object>> deptSummaryForMonth;
        synchronized (this) {
            // 再次检查缓存中是否有数据
            cachedResult = (List<Map<String, Object>>) redisTemplate.opsForValue().get(redisKey);
            if (cachedResult != null) {
                if (EMPTY_RESULT.equals(cachedResult)) {
                    return ResultEntity.fail("该月份没有数据");
                }
                return ResultEntity.success(cachedResult);
            }
            // 查询数据库
            deptSummaryForMonth = orderService.getDeptSummaryForMonth(month);
            if (deptSummaryForMonth.isEmpty()) {
                // 防止缓存穿透
                redisTemplate.opsForValue().set(redisKey, EMPTY_RESULT, 1, TimeUnit.DAYS); // 设置过期时间为1天
                return ResultEntity.fail("该月份没有数据");
            }else {
                 /*数据量大时，写入操作会影响并发性能,但安全性较好
                 提升性能的方法是异步写入缓存，先返回提示信息，
                 然后可以使用MQ或者异步线程异步写入缓存,保证缓存和数据库的最终一致性 */
                redisTemplate.opsForValue()
                        .set(redisKey, deptSummaryForMonth.toString(), 1, TimeUnit.DAYS);
            }
        }


        /*
            双检加锁优化：异步方案
            三检加锁，保证只有一个线程可以获取资源，其他线程等待，直到获取到资源后再写入缓存
            此方式实现加锁，可以实现异步写入，并且可以保证始终有一个线程可以获取资源，不会影响mysql压力
            可以使用信号量控制更多的并发线程查询mysql
          */
       /* synchronized (this) {
            // 再次检查缓存中是否有数据
            cachedResult = (List<Map<String, Object>>) redisTemplate.opsForValue().get(redisKey);
            if (cachedResult != null) {
                if (EMPTY_RESULT.equals(cachedResult)) {
                    return ResultEntity.fail("该月份没有数据");
                }
                return ResultEntity.success(cachedResult);
            }
            // 查询数据库
            deptSummaryForMonth = orderService.getDeptSummaryForMonth(month);
            if (deptSummaryForMonth.isEmpty()) {
                // 防止缓存穿透
                redisTemplate.opsForValue().set(redisKey, GlobalUtil.EMPTY_RESULT, 1, TimeUnit.DAYS); // 设置过期时间为1天
                return ResultEntity.fail("该月份没有数据");
            }else {
                if ("1".equals(redisTemplate.opsForValue().get(REDIS_WRITE_LOCK_KEY).toString())){
                    // 缓存写入标识为1表示有线程在写入缓存
                    return ResultEntity.success(deptSummaryForMonth);
                }
                // 三检防止外部线程修改缓存写入标识
                cachedResult = (List<Map<String, Object>>) redisTemplate.opsForValue().get(redisKey);
                if (cachedResult != null) {
                    return ResultEntity.success(cachedResult);
                }
                // 缓存写入标识为0表示没有线程在写入缓存，可以写入缓存
                redisTemplate.opsForValue().set(REDIS_WRITE_LOCK_KEY, "1");
                // 开启异步线程写入缓存
                asyncWriteCache(redisTemplate,deptSummaryForMonth,redisKey);
                // 返回自己的数据
                return ResultEntity.success(deptSummaryForMonth);
            }
        }*/
        return ResultEntity.success(deptSummaryForMonth);
    }


    // 按月份查询每个部门的工单总量、超期率
    @GetMapping("/dailySummary")
    public ResultEntity getDailySummary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") Date month) {
        if (month == null) {
            return ResultEntity.fail("请输入月份");
        }
        List<Map<String, Object>> dailySummaryForMonth = orderService.getDailySummaryForMonth(month);
        if (dailySummaryForMonth.isEmpty()){
            return ResultEntity.success("该月份没有数据");
        }
        return ResultEntity.success(dailySummaryForMonth);
    }

    // 按月份查询每个工单类型的工单总量、超期率
    @GetMapping("/typeSummary")
    public ResultEntity getTypeSummary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") Date month) {
        if (month == null) {
            return ResultEntity.fail("请输入月份");
        }

        List<Map<String, Object>> typeSummaryForMonth = orderService.getTypeSummaryForMonth(month);
        if (typeSummaryForMonth.isEmpty()){
            return ResultEntity.fail("该月份没有数据");
        }
        return ResultEntity.success(typeSummaryForMonth);
    }

    // 新增工单
    @PostMapping("/save")
    public ResultEntity save(@Validated @RequestBody Order order) {
        if (orderService.checkOrderNoExists(order.getOrderNo(), null)) {
             return ResultEntity.fail("工单编号已存在");
        }
        order.setCreateTime(new Date());
        boolean save = orderService.save(order);
        if (save){
            return ResultEntity.success(save);
        }
        return ResultEntity.fail("保存失败");
    }

    // 删除工单
    @PostMapping("/delete")
    public ResultEntity delete(@RequestParam Long id) {
        if (id == null || id <= 0) {
            return ResultEntity.fail("参数错误");
        }
        Order order = orderService.getById(id);
        Optional<Order> optional = Optional.ofNullable(order);
        if (!optional.isPresent()){
            return ResultEntity.success("删除成功");   // 做一个幂等判断
        }
        boolean result = orderService.removeById(id);
        if (result){
            return ResultEntity.success(result);
        }
        return ResultEntity.fail("删除失败");
    }
    // 更新工单
    @PostMapping("/update")
    public ResultEntity update(@Validated @RequestBody Order order) {
        if (orderService.checkOrderNoExists(order.getOrderNo(), order.getId())) {
            return ResultEntity.fail("工单编号已存在");
        }
        boolean result = orderService.updateById(order);
        if (result){
            return ResultEntity.success(result);
        }
        return ResultEntity.fail("更新失败");
    }

    // 分页查询工单列表
    @PostMapping("/search")
    public ResultEntity search(@RequestParam int page, @RequestParam int size) {
        if (page<=0){page = 1;}
        if (size<=0){size = 10;}
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");    // 最新拍前面，id不需要回表，提升性能
        Page<Order> result = orderService.page(new Page<>(page, size), queryWrapper);
        return ResultEntity.success(result);
    }

    // 分派工单
    @PostMapping("/fenpai")
    public ResultEntity fenpai(@RequestParam Long id, @RequestParam Long deptId, @RequestParam String deptName) {
         // 参数校验
        if (id == null || id <= 0) {
            return ResultEntity.fail("参数错误");
        }
        if (deptId == null || deptId <= 0) {
            return ResultEntity.fail("参数错误");
        }
        if (deptName == null || deptName.isEmpty()) {
            return ResultEntity.fail("参数错误");
        }
        return orderService.fenpai(id, deptId, deptName);
    }

    // 异步写入缓存方法
    @Async
    public void asyncWriteCache(RedisTemplate redisTemplate, List<Map<String, Object>> deptSummaryForMonth, String redisKey) {
        redisTemplate.opsForValue()
                .set(redisKey, deptSummaryForMonth.toString(), 1, TimeUnit.DAYS);
    }



}


