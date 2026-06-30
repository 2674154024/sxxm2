package com.parttime.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parttime.admin.dto.request.SettlementPayRequest;
import com.parttime.admin.dto.response.FinanceReportResponse;
import com.parttime.admin.dto.response.SettlementResponse;

public interface FinanceService {

    IPage<SettlementResponse> getSettlementList(Integer status, String month, 
                                                 Integer page, Integer size);

    void batchPay(SettlementPayRequest request, String operatorId);

    FinanceReportResponse getFinanceReport(String month);
}