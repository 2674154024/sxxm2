package com.parttime.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parttime.admin.dto.request.SettlementPayRequest;
import com.parttime.admin.dto.response.FinanceReportResponse;
import com.parttime.admin.dto.response.SettlementResponse;
import com.parttime.admin.service.FinanceService;
import com.parttime.common.annotation.AuditLog;
import com.parttime.common.response.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/v1/pc/admin/finance")
public class FinanceController {

    @Autowired
    private FinanceService financeService;

    @GetMapping("/settlement/list")
    public R<IPage<SettlementResponse>> getSettlementList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<SettlementResponse> result = financeService.getSettlementList(status, month, page, size);
        return R.ok(result);
    }

    @PostMapping("/settlement/pay")
    @AuditLog(module = "薪资代发", action = "批量发放")
    public R<Void> batchPay(@Validated @RequestBody SettlementPayRequest request,
                            HttpServletRequest httpRequest) {
        String operatorId = httpRequest.getHeader("X-User-Id");
        financeService.batchPay(request, operatorId);
        return R.ok();
    }

    @GetMapping("/report")
    public R<FinanceReportResponse> getFinanceReport(
            @RequestParam(required = false) String month) {
        FinanceReportResponse result = financeService.getFinanceReport(month);
        return R.ok(result);
    }
}