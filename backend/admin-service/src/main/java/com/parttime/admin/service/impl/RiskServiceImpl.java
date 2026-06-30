package com.parttime.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parttime.admin.dto.request.ComplaintHandleRequest;
import com.parttime.admin.dto.response.ComplaintResponse;
import com.parttime.admin.dto.response.RiskDashboardResponse;
import com.parttime.admin.mapper.ComplaintMapper;
import com.parttime.admin.mapper.EnterpriseMapper;
import com.parttime.admin.mapper.JobMapper;
import com.parttime.admin.mapper.StudentMapper;
import com.parttime.admin.service.RiskService;
import com.parttime.common.entity.ComplaintEntity;
import com.parttime.common.entity.JobEntity;
import com.parttime.common.entity.EnterpriseEntity;
import com.parttime.common.entity.StudentEntity;
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
public class RiskServiceImpl implements RiskService {

    @Autowired
    private ComplaintMapper complaintMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private EnterpriseMapper enterpriseMapper;

    @Autowired
    private JobMapper jobMapper;

    @Override
    public IPage<ComplaintResponse> getComplaintList(Integer status, String complaintType, 
                                                      Integer page, Integer size) {
        Page<ComplaintEntity> pageParam = new Page<>(page, size);
        IPage<ComplaintEntity> complaintPage = complaintMapper.selectPage(pageParam,
            new LambdaQueryWrapper<ComplaintEntity>()
                .eq(status != null, ComplaintEntity::getStatus, status)
                .eq(complaintType != null && !complaintType.isEmpty(), 
                    ComplaintEntity::getComplaintType, complaintType)
                .orderByDesc(ComplaintEntity::getCreatedAt)
        );

        return complaintPage.convert(this::convertToResponse);
    }

    @Override
    @Transactional
    public void handleComplaint(ComplaintHandleRequest request, String operatorId) {
        ComplaintEntity complaint = complaintMapper.selectById(request.getComplaintId());
        if (complaint == null) {
            throw new com.parttime.common.exception.BusinessException("投诉不存在");
        }

        complaint.setStatus(1);
        complaint.setHandleAction(request.getAction());
        complaint.setHandleAmount(request.getAmount());
        complaint.setHandleResult(request.getHandleResult());
        complaint.setHandledBy(operatorId);
        complaint.setHandledAt(LocalDateTime.now());
        complaintMapper.updateById(complaint);
    }

    @Override
    public RiskDashboardResponse getRiskDashboard() {
        long totalComplaints = complaintMapper.selectCount(null);
        long pendingComplaints = complaintMapper.selectCount(
            new LambdaQueryWrapper<ComplaintEntity>()
                .eq(ComplaintEntity::getStatus, 0)
        );
        long handledComplaints = complaintMapper.selectCount(
            new LambdaQueryWrapper<ComplaintEntity>()
                .eq(ComplaintEntity::getStatus, 1)
        );

        BigDecimal totalCompensationAmount = complaintMapper.selectList(
            new LambdaQueryWrapper<ComplaintEntity>()
                .eq(ComplaintEntity::getHandleAction, "compensate")
        ).stream()
            .map(ComplaintEntity::getHandleAmount)
            .filter(a -> a != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<RiskDashboardResponse.ComplaintTypeStat> complaintTypeStats = new ArrayList<>();
        String[] types = {"虚假招聘", "薪资拖欠", "押金诈骗", "未履约", "信息泄露"};
        for (String type : types) {
            long count = complaintMapper.selectCount(
                new LambdaQueryWrapper<ComplaintEntity>()
                    .eq(ComplaintEntity::getComplaintType, type)
            );
            complaintTypeStats.add(RiskDashboardResponse.ComplaintTypeStat.builder()
                .type(type)
                .count((int) count)
                .build());
        }

        List<RiskDashboardResponse.MonthlyStat> monthlyStats = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            String monthStr = month.toString();
            long complaintCount = complaintMapper.selectCount(
                new LambdaQueryWrapper<ComplaintEntity>()
                    .like(ComplaintEntity::getCreatedAt, monthStr)
            );
            long handledCount = complaintMapper.selectCount(
                new LambdaQueryWrapper<ComplaintEntity>()
                    .like(ComplaintEntity::getHandledAt, monthStr)
                    .eq(ComplaintEntity::getStatus, 1)
            );
            monthlyStats.add(RiskDashboardResponse.MonthlyStat.builder()
                .month(monthStr)
                .complaintCount((int) complaintCount)
                .handledCount((int) handledCount)
                .build());
        }

        return RiskDashboardResponse.builder()
            .totalComplaints((int) totalComplaints)
            .pendingComplaints((int) pendingComplaints)
            .handledComplaints((int) handledComplaints)
            .totalCompensationAmount(totalCompensationAmount)
            .complaintTypeStats(complaintTypeStats)
            .monthlyStats(monthlyStats)
            .build();
    }

    private ComplaintResponse convertToResponse(ComplaintEntity entity) {
        StudentEntity student = studentMapper.selectById(entity.getStudentId());
        String studentName = student != null ? student.getRealName() : "";

        String defendantName = "";
        if ("enterprise".equals(entity.getDefendantType())) {
            EnterpriseEntity enterprise = enterpriseMapper.selectById(entity.getDefendantId());
            defendantName = enterprise != null ? enterprise.getEnterpriseName() : "";
        } else if ("student".equals(entity.getDefendantType())) {
            StudentEntity s = studentMapper.selectById(entity.getDefendantId());
            defendantName = s != null ? s.getRealName() : "";
        }

        String jobTitle = "";
        if (entity.getJobId() != null) {
            JobEntity job = jobMapper.selectById(entity.getJobId());
            jobTitle = job != null ? job.getJobTitle() : "";
        }

        return ComplaintResponse.builder()
            .complaintId(entity.getComplaintId())
            .studentId(entity.getStudentId())
            .studentName(studentName)
            .defendantId(entity.getDefendantId())
            .defendantType(entity.getDefendantType())
            .defendantName(defendantName)
            .jobId(entity.getJobId())
            .jobTitle(jobTitle)
            .complaintType(entity.getComplaintType())
            .complaintContent(entity.getComplaintContent())
            .evidenceUrls(entity.getEvidenceUrls())
            .status(entity.getStatus())
            .handleAction(entity.getHandleAction())
            .handleAmount(entity.getHandleAmount())
            .handleResult(entity.getHandleResult())
            .handledBy(entity.getHandledBy())
            .handledAt(entity.getHandledAt())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}