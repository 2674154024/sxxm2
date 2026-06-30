package com.parttime.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parttime.admin.dto.request.SettlementPayRequest;
import com.parttime.admin.dto.response.FinanceReportResponse;
import com.parttime.admin.dto.response.SettlementResponse;
import com.parttime.admin.mapper.EnterpriseMapper;
import com.parttime.admin.mapper.JobMapper;
import com.parttime.admin.mapper.SalaryFlowMapper;
import com.parttime.admin.mapper.StudentMapper;
import com.parttime.admin.service.FinanceService;
import com.parttime.common.entity.JobEntity;
import com.parttime.common.entity.SalaryFlowEntity;
import com.parttime.common.entity.EnterpriseEntity;
import com.parttime.common.entity.StudentEntity;
import com.parttime.common.util.AesUtil;
import com.parttime.common.util.DesensitizeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class FinanceServiceImpl implements FinanceService {

    @Autowired
    private SalaryFlowMapper salaryFlowMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private EnterpriseMapper enterpriseMapper;

    @Autowired
    private JobMapper jobMapper;

    

    @Override
    public IPage<SettlementResponse> getSettlementList(Integer status, String month, 
                                                        Integer page, Integer size) {
        Page<SalaryFlowEntity> pageParam = new Page<>(page, size);
        IPage<SalaryFlowEntity> flowPage = salaryFlowMapper.selectPage(pageParam,
            new LambdaQueryWrapper<SalaryFlowEntity>()
                .eq(status != null, SalaryFlowEntity::getStatus, status)
                .like(month != null && !month.isEmpty(), SalaryFlowEntity::getWorkDate, month)
                .orderByDesc(SalaryFlowEntity::getCreatedAt)
        );

        return flowPage.convert(this::convertToResponse);
    }

    @Override
    @Transactional
    public void batchPay(SettlementPayRequest request, String operatorId) {
        for (String flowId : request.getFlowIds()) {
            SalaryFlowEntity flow = salaryFlowMapper.selectById(flowId);
            if (flow == null) {
                throw new com.parttime.common.exception.BusinessException("流水ID不存在: " + flowId);
            }
            if (flow.getStatus() != 1) {
                throw new com.parttime.common.exception.BusinessException("流水状态不允许发放: " + flowId);
            }

            flow.setStatus(3);
            flow.setUpdatedAt(LocalDateTime.now());
            salaryFlowMapper.updateById(flow);
        }
    }

    @Override
    public FinanceReportResponse getFinanceReport(String month) {
        BigDecimal totalPaidAmount = salaryFlowMapper.selectList(
            new LambdaQueryWrapper<SalaryFlowEntity>()
                .eq(SalaryFlowEntity::getStatus, 3)
                .like(month != null && !month.isEmpty(), SalaryFlowEntity::getCreatedAt, month)
        ).stream()
            .map(SalaryFlowEntity::getNetAmount)
            .filter(a -> a != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTaxAmount = salaryFlowMapper.selectList(
            new LambdaQueryWrapper<SalaryFlowEntity>()
                .eq(SalaryFlowEntity::getStatus, 3)
                .like(month != null && !month.isEmpty(), SalaryFlowEntity::getCreatedAt, month)
        ).stream()
            .map(SalaryFlowEntity::getTaxAmount)
            .filter(a -> a != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingSettlementAmount = salaryFlowMapper.selectList(
            new LambdaQueryWrapper<SalaryFlowEntity>()
                .eq(SalaryFlowEntity::getStatus, 1)
                .like(month != null && !month.isEmpty(), SalaryFlowEntity::getCreatedAt, month)
        ).stream()
            .map(SalaryFlowEntity::getNetAmount)
            .filter(a -> a != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalTransactions = salaryFlowMapper.selectCount(
            new LambdaQueryWrapper<SalaryFlowEntity>()
                .eq(SalaryFlowEntity::getStatus, 3)
                .like(month != null && !month.isEmpty(), SalaryFlowEntity::getCreatedAt, month)
        );

        List<FinanceReportResponse.MonthlyFinanceStat> monthlyStats = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth m = YearMonth.now().minusMonths(i);
            String mStr = m.toString();
            BigDecimal paid = salaryFlowMapper.selectList(
                new LambdaQueryWrapper<SalaryFlowEntity>()
                    .eq(SalaryFlowEntity::getStatus, 3)
                    .like(SalaryFlowEntity::getCreatedAt, mStr)
            ).stream()
                .map(SalaryFlowEntity::getNetAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal tax = salaryFlowMapper.selectList(
                new LambdaQueryWrapper<SalaryFlowEntity>()
                    .eq(SalaryFlowEntity::getStatus, 3)
                    .like(SalaryFlowEntity::getCreatedAt, mStr)
            ).stream()
                .map(SalaryFlowEntity::getTaxAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            long count = salaryFlowMapper.selectCount(
                new LambdaQueryWrapper<SalaryFlowEntity>()
                    .eq(SalaryFlowEntity::getStatus, 3)
                    .like(SalaryFlowEntity::getCreatedAt, mStr)
            );

            monthlyStats.add(FinanceReportResponse.MonthlyFinanceStat.builder()
                .month(mStr)
                .paidAmount(paid)
                .taxAmount(tax)
                .transactionCount((int) count)
                .build());
        }

        return FinanceReportResponse.builder()
            .totalEscrowAmount(BigDecimal.ZERO)
            .totalPaidAmount(totalPaidAmount)
            .totalTaxAmount(totalTaxAmount)
            .pendingSettlementAmount(pendingSettlementAmount)
            .totalTransactions((int) totalTransactions)
            .monthlyStats(monthlyStats)
            .enterpriseStats(new ArrayList<>())
            .build();
    }

    private SettlementResponse convertToResponse(SalaryFlowEntity entity) {
        StudentEntity student = studentMapper.selectById(entity.getStudentId());
        String studentName = student != null ? student.getRealName() : "";
        String phone = "";
        if (student != null && student.getPhoneEncrypt() != null) {
            try {
                phone = AesUtil.decrypt(student.getPhoneEncrypt());
                phone = DesensitizeUtil.desensitizePhone(phone);
            } catch (Exception e) {
                phone = "***";
            }
        }

        String jobTitle = "";
        if (entity.getJobId() != null) {
            JobEntity job = jobMapper.selectById(entity.getJobId());
            jobTitle = job != null ? job.getJobTitle() : "";
        }

        return SettlementResponse.builder()
            .flowId(entity.getFlowId())
            .studentId(entity.getStudentId())
            .studentName(studentName)
            .phone(phone)
            .jobId(entity.getJobId())
            .jobTitle(jobTitle)
            .workHours(entity.getWorkHours())
            .hourlyRate(entity.getHourlyRate())
            .grossAmount(entity.getGrossAmount())
            .taxAmount(entity.getTaxAmount())
            .netAmount(entity.getNetAmount())
            .status(entity.getStatus())
            .workDate(entity.getWorkDate())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}