package com.atchen.AISearch.util;
import lombok.Data;
import java.io.Serializable;
/**
 * @Author: atchen
 * @CreateTime: 2024-08-20
 * @Description: 统一返回结果类
 * @Version: 1.0
 */
@Data
public class ResultEntity implements Serializable {
    private static final long serialVersionUID = 112313333311L;  // 序列化标识
    private int code;  // 状态码
    private String message;  // 信息
    private Object data;  // 数据

    public static ResultEntity success(Object data) {
        ResultEntity resultEntity = new ResultEntity();
        resultEntity.setCode(200);
        resultEntity.setMessage("success");
        resultEntity.setData(data);
        return resultEntity;
    }

    public static ResultEntity fail(String message) {
        ResultEntity resultEntity = new ResultEntity();
        resultEntity.setCode(500);
        resultEntity.setMessage(message);
        resultEntity.setData(null);
        return resultEntity;
    }
}
    