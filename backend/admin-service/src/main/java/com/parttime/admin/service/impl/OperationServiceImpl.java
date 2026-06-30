package com.parttime.admin.service.impl;

import com.parttime.admin.dto.request.NotificationPushRequest;
import com.parttime.admin.dto.response.OperationReportResponse;
import com.parttime.admin.mapper.ComplaintMapper;
import com.parttime.admin.mapper.EnterpriseMapper;
import com.parttime.admin.mapper.JobMapper;
import com.parttime.admin.mapper.SalaryFlowMapper;
import com.parttime.admin.mapper.StudentMapper;
import com.parttime.admin.service.OperationService;
import com.parttime.common.entity.ComplaintEntity;
import com.parttime.common.entity.JobEntity;
import com.parttime.common.entity.SalaryFlowEntity;
import com.parttime.common.entity.EnterpriseEntity;
import com.parttime.common.entity.StudentEntity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class OperationServiceImpl implements OperationService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private EnterpriseMapper enterpriseMapper;

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private ComplaintMapper complaintMapper;

    @Autowired
    private SalaryFlowMapper salaryFlowMapper;

    @Override
    public OperationReportResponse getOperationReport(String dateRange) {
        long totalStudents = studentMapper.selectCount(null);
        long totalEnterprises = enterpriseMapper.selectCount(null);
        long totalJobs = jobMapper.selectCount(null);
        long totalComplaints = complaintMapper.selectCount(null);

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        long dailyNewStudents = studentMapper.selectCount(
            new LambdaQueryWrapper<StudentEntity>()
                .ge(StudentEntity::getCreatedAt, todayStart)
                .le(StudentEntity::getCreatedAt, todayEnd)
        );

        long dailyNewEnterprises = enterpriseMapper.selectCount(
            new LambdaQueryWrapper<EnterpriseEntity>()
                .ge(EnterpriseEntity::getCreatedAt, todayStart)
                .le(EnterpriseEntity::getCreatedAt, todayEnd)
        );

        long dailyNewJobs = jobMapper.selectCount(
            new LambdaQueryWrapper<JobEntity>()
                .ge(JobEntity::getCreatedAt, todayStart)
                .le(JobEntity::getCreatedAt, todayEnd)
        );

        BigDecimal totalSalaryPaid = salaryFlowMapper.selectList(
            new LambdaQueryWrapper<SalaryFlowEntity>()
                .eq(SalaryFlowEntity::getStatus, 3)
        ).stream()
            .map(SalaryFlowEntity::getNetAmount)
            .filter(a -> a != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OperationReportResponse.UserGrowthStat> userGrowthStats = new ArrayList<>();
        List<OperationReportResponse.JobStat> jobStats = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.toString();
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);

            long newStudents = studentMapper.selectCount(
                new LambdaQueryWrapper<StudentEntity>()
                    .ge(StudentEntity::getCreatedAt, start)
                    .le(StudentEntity::getCreatedAt, end)
            );

            long newEnterprises = enterpriseMapper.selectCount(
                new LambdaQueryWrapper<EnterpriseEntity>()
                    .ge(EnterpriseEntity::getCreatedAt, start)
                    .le(EnterpriseEntity::getCreatedAt, end)
            );

            long publishedJobs = jobMapper.selectCount(
                new LambdaQueryWrapper<JobEntity>()
                    .ge(JobEntity::getCreatedAt, start)
                    .le(JobEntity::getCreatedAt, end)
            );

            long onlineJobs = jobMapper.selectCount(
                new LambdaQueryWrapper<JobEntity>()
                    .eq(JobEntity::getStatus, 1)
            );

            userGrowthStats.add(OperationReportResponse.UserGrowthStat.builder()
                .date(dateStr)
                .newStudents((int) newStudents)
                .newEnterprises((int) newEnterprises)
                .build());

            jobStats.add(OperationReportResponse.JobStat.builder()
                .date(dateStr)
                .published((int) publishedJobs)
                .online((int) onlineJobs)
                .build());
        }

        return OperationReportResponse.builder()
            .totalStudents((int) totalStudents)
            .totalEnterprises((int) totalEnterprises)
            .totalJobs((int) totalJobs)
            .totalApplies(0)
            .totalComplaints((int) totalComplaints)
            .dailyNewStudents((int) dailyNewStudents)
            .dailyNewEnterprises((int) dailyNewEnterprises)
            .dailyNewJobs((int) dailyNewJobs)
            .dailyNewApplies(0)
            .totalSalaryPaid(totalSalaryPaid)
            .userGrowthStats(userGrowthStats)
            .jobStats(jobStats)
            .applyStats(new ArrayList<>())
            .build();
    }

    @Override
    public void pushNotification(NotificationPushRequest request) {
    }
}