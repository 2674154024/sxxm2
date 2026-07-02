package com.parttime.complaint.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parttime.common.exception.BusinessException;
import com.parttime.complaint.dto.response.ComplaintResponse;
import com.parttime.complaint.dto.response.FeedbackResponse;
import com.parttime.complaint.entity.ComplaintEntity;
import com.parttime.complaint.entity.FeedbackEntity;
import com.parttime.complaint.entity.PracticeReportEntity;
import com.parttime.complaint.mapper.ComplaintMapper;
import com.parttime.complaint.mapper.FeedbackMapper;
import com.parttime.complaint.mapper.JobMapper;
import com.parttime.complaint.mapper.PracticeReportMapper;
import com.parttime.complaint.service.ComplaintService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ComplaintServiceImpl implements ComplaintService {

    private static final String EXCHANGE_NAME = "parttime.exchange";
    private static final String ROUTING_KEY_COMPLAINT = "complaint.admin";

    @Autowired
    private ComplaintMapper complaintMapper;

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private PracticeReportMapper practiceReportMapper;

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public ComplaintResponse createComplaint(String studentId, String defendantId, String defendantType,
                                              String jobId, String complaintType, String complaintContent, String evidenceUrls) {
        String complaintId = UUID.randomUUID().toString();

        String finalDefendantId = defendantId;
        String finalDefendantType = defendantType;

        if ((finalDefendantId == null || finalDefendantId.isEmpty()) && jobId != null && !jobId.isEmpty()) {
            finalDefendantId = jobMapper.getEnterpriseIdByJobId(jobId);
            finalDefendantType = "enterprise";
        }

        if (finalDefendantId == null || finalDefendantId.isEmpty()) {
            throw new BusinessException("被投诉人ID不能为空");
        }
        if (finalDefendantType == null || finalDefendantType.isEmpty()) {
            finalDefendantType = "enterprise";
        }

        ComplaintEntity complaint = ComplaintEntity.builder()
                .complaintId(complaintId)
                .studentId(studentId)
                .defendantId(finalDefendantId)
                .defendantType(finalDefendantType)
                .jobId(jobId)
                .complaintType(complaintType)
                .complaintContent(complaintContent)
                .evidenceUrls(evidenceUrls)
                .status(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        complaintMapper.insert(complaint);

        try {
            JSONObject notification = new JSONObject();
            notification.put("type", "complaint");
            notification.put("complaintId", complaintId);
            notification.put("studentId", studentId);
            notification.put("complaintType", complaintType);
            notification.put("createdAt", System.currentTimeMillis());

            rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY_COMPLAINT, notification.toJSONString());
        } catch (Exception e) {
            log.warn("发送投诉通知失败: {}", e.getMessage());
        }

        return convertToComplaintResponse(complaint);
    }

    @Override
    public List<ComplaintResponse> getStudentComplaints(String studentId, Integer page, Integer size) {
        Page<ComplaintEntity> pageParam = new Page<>(page, size);
        Page<ComplaintEntity> result = complaintMapper.selectByStudentId(pageParam, studentId);
        return result.getRecords().stream()
                .map(this::convertToComplaintResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComplaintResponse> getAllComplaints(Integer status, Integer page, Integer size) {
        Page<ComplaintEntity> pageParam = new Page<>(page, size);
        Page<ComplaintEntity> result;
        if (status != null) {
            result = complaintMapper.selectByStatus(pageParam, status);
        } else {
            result = complaintMapper.selectAll(pageParam);
        }
        return result.getRecords().stream()
                .map(this::convertToComplaintResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ComplaintResponse handleComplaint(String complaintId, String action, java.math.BigDecimal amount,
                                              String handleResult, String handledBy) {
        ComplaintEntity complaint = complaintMapper.selectById(complaintId);
        if (complaint == null) {
            throw new BusinessException("投诉记录不存在");
        }

        if (complaint.getStatus() != 0) {
            throw new BusinessException("投诉已处理");
        }

        if (!isValidAction(action)) {
            throw new BusinessException("无效的处理动作");
        }

        complaint.setStatus(1);
        complaint.setHandleAction(action);
        complaint.setHandleAmount(amount);
        complaint.setHandleResult(handleResult);
        complaint.setHandledBy(handledBy);
        complaint.setHandledAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());

        complaintMapper.updateById(complaint);

        JSONObject notification = new JSONObject();
        notification.put("type", "complaint_handle");
        notification.put("complaintId", complaintId);
        notification.put("action", action);
        notification.put("studentId", complaint.getStudentId());
        notification.put("handledAt", System.currentTimeMillis());

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "notification.im", notification.toJSONString());

        return convertToComplaintResponse(complaint);
    }

    @Override
    @Transactional
    public FeedbackResponse studentFeedback(String studentId, String toId, String jobId, Integer score, String comment) {
        FeedbackEntity existing = feedbackMapper.selectByUniqueKey(studentId, toId, jobId);
        if (existing != null) {
            throw new BusinessException("已评价过该岗位");
        }

        FeedbackEntity feedback = FeedbackEntity.builder()
                .id(UUID.randomUUID().toString())
                .fromId(studentId)
                .fromType("student")
                .toId(toId)
                .toType("enterprise")
                .jobId(jobId)
                .score(score)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();

        feedbackMapper.insert(feedback);

        PracticeReportEntity report = practiceReportMapper.selectByUniqueKey(studentId, jobId);
        if (report == null) {
            report = PracticeReportEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .studentId(studentId)
                    .jobId(jobId)
                    .studentComment(comment)
                    .studentScore(score)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            practiceReportMapper.insert(report);
        } else {
            practiceReportMapper.updateStudentComment(studentId, jobId, comment, score);
        }

        return convertToFeedbackResponse(feedback);
    }

    @Override
    @Transactional
    public FeedbackResponse enterpriseFeedback(String enterpriseId, String toId, String jobId, Integer score, String comment) {
        FeedbackEntity existing = feedbackMapper.selectByUniqueKey(enterpriseId, toId, jobId);
        if (existing != null) {
            throw new BusinessException("已评价过该学生");
        }

        FeedbackEntity feedback = FeedbackEntity.builder()
                .id(UUID.randomUUID().toString())
                .fromId(enterpriseId)
                .fromType("enterprise")
                .toId(toId)
                .toType("student")
                .jobId(jobId)
                .score(score)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();

        feedbackMapper.insert(feedback);

        PracticeReportEntity report = practiceReportMapper.selectByUniqueKey(toId, jobId);
        if (report == null) {
            report = PracticeReportEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .studentId(toId)
                    .jobId(jobId)
                    .enterpriseComment(comment)
                    .enterpriseScore(score)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            practiceReportMapper.insert(report);
        } else {
            practiceReportMapper.updateEnterpriseComment(toId, jobId, comment, score);
        }

        return convertToFeedbackResponse(feedback);
    }

    private boolean isValidAction(String action) {
        return "freeze".equals(action) || "unfreeze".equals(action)
                || "deduct".equals(action) || "compensate".equals(action);
    }

    private ComplaintResponse convertToComplaintResponse(ComplaintEntity entity) {
        return ComplaintResponse.builder()
                .complaintId(entity.getComplaintId())
                .studentId(entity.getStudentId())
                .defendantId(entity.getDefendantId())
                .defendantType(entity.getDefendantType())
                .jobId(entity.getJobId())
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

    private FeedbackResponse convertToFeedbackResponse(FeedbackEntity entity) {
        return FeedbackResponse.builder()
                .id(entity.getId())
                .fromId(entity.getFromId())
                .fromType(entity.getFromType())
                .toId(entity.getToId())
                .toType(entity.getToType())
                .jobId(entity.getJobId())
                .score(entity.getScore())
                .comment(entity.getComment())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}