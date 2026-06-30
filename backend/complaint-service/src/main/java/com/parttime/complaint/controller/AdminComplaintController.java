package com.parttime.complaint.controller;

import com.parttime.common.annotation.AuditLog;
import com.parttime.common.response.R;
import com.parttime.complaint.dto.request.ComplaintHandleRequest;
import com.parttime.complaint.dto.response.ComplaintResponse;
import com.parttime.complaint.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/pc/admin/risk")
public class AdminComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @GetMapping("/complaint/list")
    @AuditLog(module = "风控管理", action = "投诉工单列表")
    public R<List<ComplaintResponse>> getComplaintList(@RequestParam(required = false) Integer status,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "20") Integer size) {
        List<ComplaintResponse> complaints = complaintService.getAllComplaints(status, page, size);
        return R.ok(complaints);
    }

    @PostMapping("/complaint/handle")
    @AuditLog(module = "风控管理", action = "处理投诉")
    public R<ComplaintResponse> handleComplaint(@Valid @RequestBody ComplaintHandleRequest request) {
        ComplaintResponse response = complaintService.handleComplaint(request.getComplaintId(),
                request.getAction(), request.getAmount(), request.getHandleResult(), request.getHandledBy());
        return R.ok(response);
    }
}