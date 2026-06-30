package com.parttime.admin.controller;

import com.parttime.admin.dto.request.NotificationPushRequest;
import com.parttime.admin.dto.response.OperationReportResponse;
import com.parttime.admin.service.OperationService;
import com.parttime.common.annotation.AuditLog;
import com.parttime.common.response.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/pc/admin/operation")
public class OperationController {

    @Autowired
    private OperationService operationService;

    @GetMapping("/report")
    public R<OperationReportResponse> getOperationReport(
            @RequestParam(required = false) String dateRange) {
        OperationReportResponse result = operationService.getOperationReport(dateRange);
        return R.ok(result);
    }

    @PostMapping("/notification")
    @AuditLog(module = "运营管理", action = "推送通知")
    public R<Void> pushNotification(@Validated @RequestBody NotificationPushRequest request) {
        operationService.pushNotification(request);
        return R.ok();
    }
}