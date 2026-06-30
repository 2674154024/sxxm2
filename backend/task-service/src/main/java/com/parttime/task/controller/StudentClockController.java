package com.parttime.task.controller;

import com.parttime.common.annotation.AuditLog;
import com.parttime.common.response.R;
import com.parttime.common.util.JwtUtil;
import com.parttime.task.dto.request.ClockAppealRequest;
import com.parttime.task.dto.request.ClockRecordsRequest;
import com.parttime.task.dto.request.ClockRequest;
import com.parttime.task.dto.response.ClockRecordResponse;
import com.parttime.task.service.ClockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/student")
public class StudentClockController {

    @Autowired
    private ClockService clockService;

    @PostMapping("/clock")
    @AuditLog(module = "打卡管理", action = "GPS打卡")
    public R<ClockRecordResponse> clock(@Valid @RequestBody ClockRequest request, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String studentId = JwtUtil.getUserId(token);
        ClockRecordResponse response = clockService.clock(studentId, request.getJobId(),
                request.getClockType(), request.getLongitude(), request.getLatitude());
        return R.ok(response);
    }

    @GetMapping("/clock/records")
    @AuditLog(module = "打卡管理", action = "获取打卡记录")
    public R<List<ClockRecordResponse>> getClockRecords(ClockRecordsRequest request, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String studentId = JwtUtil.getUserId(token);
        List<ClockRecordResponse> records = clockService.getClockRecords(studentId,
                request.getJobId(), request.getDate(), request.getPage(), request.getSize());
        return R.ok(records);
    }

    @PostMapping("/clock/appeal")
    @AuditLog(module = "打卡管理", action = "异常申诉")
    public R<Void> appealClock(@Valid @RequestBody ClockAppealRequest request, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String studentId = JwtUtil.getUserId(token);
        clockService.appealClock(request.getRecordId(), studentId, request.getAppealReason(), request.getEvidenceUrls());
        return R.ok();
    }
}