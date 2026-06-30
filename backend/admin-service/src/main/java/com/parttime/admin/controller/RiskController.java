package com.parttime.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parttime.admin.dto.request.ComplaintHandleRequest;
import com.parttime.admin.dto.response.ComplaintResponse;
import com.parttime.admin.dto.response.RiskDashboardResponse;
import com.parttime.admin.service.RiskService;
import com.parttime.common.annotation.AuditLog;
import com.parttime.common.response.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/v1/pc/admin/risk")
public class RiskController {

    @Autowired
    private RiskService riskService;

    @GetMapping("/complaint/list")
    public R<IPage<ComplaintResponse>> getComplaintList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String complaintType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<ComplaintResponse> result = riskService.getComplaintList(status, complaintType, page, size);
        return R.ok(result);
    }

    @PostMapping("/complaint/handle")
    @AuditLog(module = "投诉处理", action = "处理")
    public R<Void> handleComplaint(@Validated @RequestBody ComplaintHandleRequest request,
                                   HttpServletRequest httpRequest) {
        String operatorId = httpRequest.getHeader("X-User-Id");
        riskService.handleComplaint(request, operatorId);
        return R.ok();
    }

    @GetMapping("/dashboard")
    public R<RiskDashboardResponse> getRiskDashboard() {
        RiskDashboardResponse result = riskService.getRiskDashboard();
        return R.ok(result);
    }
}