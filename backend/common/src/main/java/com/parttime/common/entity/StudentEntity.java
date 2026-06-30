package com.parttime.common.entity;

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
@TableName("t_student")
public class StudentEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("real_name")
    private String realName;

    @TableField("student_no")
    private String studentNo;

    @TableField("school_id")
    private String schoolId;

    @TableField("school_name")
    private String schoolName;

    @TableField("id_card_encrypt")
    private String idCardEncrypt;

    @TableField("phone_encrypt")
    private String phoneEncrypt;

    @TableField("verify_status")
    private Integer verifyStatus;

    @TableField("credit_score")
    private Integer creditScore;

    @TableField("available_time")
    private String availableTime;

    @TableField("skill_tags")
    private String skillTags;

    @TableField("longitude")
    private java.math.BigDecimal longitude;

    @TableField("latitude")
    private java.math.BigDecimal latitude;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}