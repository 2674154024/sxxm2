package com.parttime.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parttime.admin.dto.request.ComplaintHandleRequest;
import com.parttime.admin.dto.response.ComplaintResponse;
import com.parttime.admin.dto.response.RiskDashboardResponse;

public interface RiskService {

    IPage<ComplaintResponse> getComplaintList(Integer status, String complaintType, 
                                              Integer page, Integer size);

    void handleComplaint(ComplaintHandleRequest request, String operatorId);

    RiskDashboardResponse getRiskDashboard();
}