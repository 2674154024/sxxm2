package com.parttime.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_job")
public class JobEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("enterprise_id")
    private String enterpriseId;

    @TableField("job_title")
    private String jobTitle;

    @TableField("job_type")
    private String jobType;

    @TableField("industry_tag")
    private String industryTag;

    @TableField("salary_type")
    private Integer salaryType;

    @TableField("salary_amount")
    private BigDecimal salaryAmount;

    @TableField("settlement_type")
    private Integer settlementType;

    @TableField("work_address")
    private String workAddress;

    @TableField("longitude")
    private BigDecimal longitude;

    @TableField("latitude")
    private BigDecimal latitude;

    @TableField("work_time")
    private String workTime;

    @TableField("skill_require")
    private String skillRequire;

    @TableField("recruit_num")
    private Integer recruitNum;

    @TableField("status")
    private Integer status;

    @TableField("is_insured")
    private Integer isInsured;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("apply_count")
    private Integer applyCount;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}