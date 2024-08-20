package com.atchen.AISearch.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 工单表
 * </p>
 *
 * @author atchen
 * @since 2024-08-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order")
public class Order implements Serializable {

    private static final long serialVersionUID = 14444444444L;

    /**
     * 工单id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 工单编号
     */
    @NotBlank(message = "工单编号不能为空")
    private String orderNo;

    /**
     * 工单类型 0交办 1直接答复 3无效工单
     */
    @NotNull(message = "工单类型不能为空")
    private Integer orderType;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 内容
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 处理部门
     */
    private Long handleDeptId;

    /**
     * 创建时间
     */
    @NotNull(message = "创建时间不能为空")
    private Date createTime;

    /**
     * 分派时间
     */
    private Date fenpaiTime;

    /**
     * 是否超期 0否 1是
     */
    private Integer isOverdue;


}
