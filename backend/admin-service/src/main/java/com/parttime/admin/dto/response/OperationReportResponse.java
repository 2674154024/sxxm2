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
public class OperationReportResponse {

    private Integer totalStudents;

    private Integer totalEnterprises;

    private Integer totalJobs;

    private Integer totalApplies;

    private Integer totalComplaints;

    private Integer dailyNewStudents;

    private Integer dailyNewEnterprises;

    private Integer dailyNewJobs;

    private Integer dailyNewApplies;

    private BigDecimal totalSalaryPaid;

    private List<UserGrowthStat> userGrowthStats;

    private List<JobStat> jobStats;

    private List<ApplyStat> applyStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserGrowthStat {
        private String date;
        private Integer newStudents;
        private Integer newEnterprises;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobStat {
        private String date;
        private Integer published;
        private Integer online;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplyStat {
        private String date;
        private Integer total;
        private Integer accepted;
    }
}