package com.parttime.task.controller;

import com.parttime.common.annotation.AuditLog;
import com.parttime.common.response.R;
import com.parttime.task.dto.request.AttendanceConfirmRequest;
import com.parttime.task.dto.response.AttendanceRecordResponse;
import com.parttime.task.service.ClockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/enterprise")
public class EnterpriseAttendanceController {

    @Autowired
    private ClockService clockService;

    @GetMapping("/attendance/list")
    @AuditLog(module = "考勤管理", action = "查看考勤列表")
    public R<List<AttendanceRecordResponse>> getAttendanceList(@RequestParam String jobId,
                                                               @RequestParam(defaultValue = "1") Integer page,
                                                               @RequestParam(defaultValue = "20") Integer size) {
        List<AttendanceRecordResponse> records = clockService.getAttendanceList(jobId, page, size);
        return R.ok(records);
    }

    @PostMapping("/attendance/confirm")
    @AuditLog(module = "考勤管理", action = "确认工时")
    public R<AttendanceRecordResponse> confirmAttendance(@Valid @RequestBody AttendanceConfirmRequest request) {
        AttendanceRecordResponse response = clockService.confirmAttendance(request.getStudentId(),
                request.getJobId(), request.getWorkDate(), request.getConfirmedHours(), request.getNote());
        return R.ok(response);
    }
}