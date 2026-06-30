package com.parttime.complaint.controller;

import com.parttime.common.annotation.AuditLog;
import com.parttime.common.response.R;
import com.parttime.common.util.JwtUtil;
import com.parttime.complaint.dto.request.FeedbackRequest;
import com.parttime.complaint.dto.response.FeedbackResponse;
import com.parttime.complaint.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/v1/enterprise")
public class EnterpriseComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @PostMapping("/feedback")
    @AuditLog(module = "评价管理", action = "企业评价")
    public R<FeedbackResponse> enterpriseFeedback(@Valid @RequestBody FeedbackRequest request, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String enterpriseId = JwtUtil.getUserId(token);
        FeedbackResponse response = complaintService.enterpriseFeedback(enterpriseId, request.getToId(),
                request.getJobId(), request.getScore(), request.getComment());
        return R.ok(response);
    }
}