package com.atchen.AISearch.util;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
/**
 * @Author: atchen
 * @CreateTime: 2024-08-20
 * @Description: 选择日期工具类
 * @Version: 1.0
 */
public class SelectDateUtil {
    /*
        * @description 选择月份区间的util  list0 为开始时间  list1 为结束时间
        * @author atchen
        * @date 2024/8/20
    */
    public static List<Date> selectDate(Date month) {
        Calendar calendar = Calendar.getInstance();  // 获取日历实例
        calendar.setTime(month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);  // 该月份第一天
        Date startDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH,0);   // 该月份最后一天
        Date endDate = calendar.getTime();
        List<Date> dateList = new ArrayList<>();
        dateList.add(startDate);
        dateList.add(endDate);
        return dateList;
    }
}
    