package com.parttime.complaint.controller;

import com.parttime.common.annotation.AuditLog;
import com.parttime.common.response.R;
import com.parttime.common.util.JwtUtil;
import com.parttime.complaint.dto.request.ComplaintRequest;
import com.parttime.complaint.dto.request.FeedbackRequest;
import com.parttime.complaint.dto.response.ComplaintResponse;
import com.parttime.complaint.dto.response.FeedbackResponse;
import com.parttime.complaint.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/student")
public class StudentComplaintController {

    @Autowired
    private ComplaintService complaintService;

    private String getStudentId(HttpServletRequest httpRequest) {
        String gatewayUserId = httpRequest.getHeader("X-User-Id");
        if (gatewayUserId != null && !gatewayUserId.isEmpty()) {
            return gatewayUserId;
        }
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return JwtUtil.getUserId(token);
    }

    @PostMapping("/complaint")
    @AuditLog(module = "投诉管理", action = "发起投诉")
    public R<ComplaintResponse> createComplaint(@Valid @RequestBody ComplaintRequest request, HttpServletRequest httpRequest) {
        String studentId = getStudentId(httpRequest);
        ComplaintResponse response = complaintService.createComplaint(studentId, request.getDefendantId(),
                request.getDefendantType(), request.getJobId(), request.getComplaintType(),
                request.getComplaintContent(), request.getEvidenceUrls());
        return R.ok(response);
    }

    @GetMapping("/complaint/list")
    @AuditLog(module = "投诉管理", action = "查看我的投诉")
    public R<List<ComplaintResponse>> getComplaintList(HttpServletRequest httpRequest,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "20") Integer size) {
        String studentId = getStudentId(httpRequest);
        List<ComplaintResponse> complaints = complaintService.getStudentComplaints(studentId, page, size);
        return R.ok(complaints);
    }

    @PostMapping("/feedback")
    @AuditLog(module = "评价管理", action = "学生评价")
    public R<FeedbackResponse> studentFeedback(@Valid @RequestBody FeedbackRequest request, HttpServletRequest httpRequest) {
        String studentId = getStudentId(httpRequest);
        FeedbackResponse response = complaintService.studentFeedback(studentId, request.getToId(),
                request.getJobId(), request.getScore(), request.getComment());
        return R.ok(response);
    }
}