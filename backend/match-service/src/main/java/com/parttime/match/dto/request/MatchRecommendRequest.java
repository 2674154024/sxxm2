package com.parttime.match.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchRecommendRequest {

    @NotBlank(message = "学生ID不能为空")
    private String studentId;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Integer distance = 5;

    private Integer limit = 20;
}