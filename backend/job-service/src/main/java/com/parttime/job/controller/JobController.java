package com.parttime.job.controller;

import com.parttime.common.response.R;
import com.parttime.job.dto.request.JobPublishRequest;
import com.parttime.job.dto.request.JobUpdateRequest;
import com.parttime.job.dto.response.JobListResponse;
import com.parttime.job.dto.response.PageResponse;
import com.parttime.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/enterprise/job")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping("/publish")
    public R<String> publish(@RequestHeader("X-User-Id") String enterpriseId,
                             @Validated @RequestBody JobPublishRequest request) {
        jobService.publish(enterpriseId, request);
        return R.ok("岗位发布成功");
    }

    @PutMapping("/update")
    public R<String> update(@RequestHeader("X-User-Id") String enterpriseId,
                            @Validated @RequestBody JobUpdateRequest request) {
        jobService.update(enterpriseId, request);
        return R.ok("岗位更新成功");
    }

    @PutMapping("/offline")
    public R<String> offline(@RequestHeader("X-User-Id") String enterpriseId,
                             @RequestParam("job_id") String jobId) {
        jobService.offline(enterpriseId, jobId);
        return R.ok("岗位已下架");
    }

    @GetMapping("/my-list")
    public R<PageResponse<JobListResponse>> getMyJobList(@RequestHeader("X-User-Id") String enterpriseId,
                                                          @RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(jobService.getMyJobList(enterpriseId, page, size));
    }
}