package com.parttime.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskDashboardResponse {

    private Integer totalComplaints;

    private Integer pendingComplaints;

    private Integer handledComplaints;

    private BigDecimal totalCompensationAmount;

    private List<ComplaintTypeStat> complaintTypeStats;

    private List<MonthlyStat> monthlyStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplaintTypeStat {
        private String type;
        private Integer count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStat {
        private String month;
        private Integer complaintCount;
        private Integer handledCount;
    }
}