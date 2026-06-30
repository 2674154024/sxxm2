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
public class FinanceReportResponse {

    private BigDecimal totalEscrowAmount;

    private BigDecimal totalPaidAmount;

    private BigDecimal totalTaxAmount;

    private BigDecimal pendingSettlementAmount;

    private Integer totalTransactions;

    private List<MonthlyFinanceStat> monthlyStats;

    private List<EnterprisePayStat> enterpriseStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyFinanceStat {
        private String month;
        private BigDecimal paidAmount;
        private BigDecimal taxAmount;
        private Integer transactionCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnterprisePayStat {
        private String enterpriseName;
        private BigDecimal totalPaid;
        private Integer transactionCount;
    }
}