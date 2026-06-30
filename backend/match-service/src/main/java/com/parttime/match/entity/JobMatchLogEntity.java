package com.parttime.match.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_job_match_log")
public class JobMatchLogEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("student_id")
    private String studentId;

    @TableField("job_id")
    private String jobId;

    @TableField("match_score")
    private Integer matchScore;

    @TableField("skill_score")
    private Integer skillScore;

    @TableField("time_score")
    private Integer timeScore;

    @TableField("distance_score")
    private Integer distanceScore;

    @TableField("distance_km")
    private Double distanceKm;

    @TableField("created_at")
    private LocalDateTime createdAt;
}