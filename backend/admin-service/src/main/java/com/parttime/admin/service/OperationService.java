package com.parttime.admin.service;

import com.parttime.admin.dto.request.NotificationPushRequest;
import com.parttime.admin.dto.response.OperationReportResponse;

public interface OperationService {

    OperationReportResponse getOperationReport(String dateRange);

    void pushNotification(NotificationPushRequest request);
}