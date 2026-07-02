package com.parttime.job.entity;

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
@TableName("t_job_favorite")
public class JobFavoriteEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("student_id")
    private String studentId;

    @TableField("job_id")
    private String jobId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}