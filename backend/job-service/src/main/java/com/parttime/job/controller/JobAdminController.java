package com.parttime.job.controller;

import com.parttime.common.response.R;
import com.parttime.job.dto.request.JobAuditRequest;
import com.parttime.job.dto.response.JobListResponse;
import com.parttime.job.dto.response.PageResponse;
import com.parttime.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/pc/admin/job")
@RequiredArgsConstructor
public class JobAdminController {

    private final JobService jobService;

    @GetMapping("/audit/list")
    public R<PageResponse<JobListResponse>> getAuditList(@RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(jobService.getAuditList(page, size));
    }

    @PostMapping("/audit")
    public R<String> audit(@Validated @RequestBody JobAuditRequest request) {
        jobService.audit(request);
        return R.ok("审核完成");
    }
}