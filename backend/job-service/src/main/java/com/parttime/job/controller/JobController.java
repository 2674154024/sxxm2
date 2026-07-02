package com.parttime.job.controller;

import com.parttime.common.response.R;
import com.parttime.job.dto.request.JobPublishRequest;
import com.parttime.job.dto.request.JobUpdateRequest;
import com.parttime.job.dto.response.ApplyListResponse;
import com.parttime.job.dto.response.JobDetailResponse;
import com.parttime.job.dto.response.JobListResponse;
import com.parttime.job.dto.response.PageResponse;
import com.parttime.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/pc/enterprise/job")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping("/publish")
    public R<String> publish(@RequestHeader("X-User-Id") String enterpriseId,
                             @Validated @RequestBody JobPublishRequest request) {
        jobService.publish(enterpriseId, request);
        return R.ok("岗位发布成功");
    }

    @PostMapping("/batch-publish")
    public R<Map<String, Object>> batchPublish(@RequestHeader("X-User-Id") String enterpriseId,
                                                @RequestBody List<JobPublishRequest> requests) {
        int success = 0;
        int failed = 0;
        for (JobPublishRequest request : requests) {
            try {
                jobService.publish(enterpriseId, request);
                success++;
            } catch (Exception e) {
                failed++;
            }
        }
        return R.ok(Map.of("success", success, "failed", failed));
    }

    @PutMapping("/update")
    public R<String> update(@RequestHeader("X-User-Id") String enterpriseId,
                            @Validated @RequestBody JobUpdateRequest request) {
        jobService.update(enterpriseId, request);
        return R.ok("岗位更新成功");
    }

    @PostMapping("/offline")
    public R<String> offline(@RequestHeader("X-User-Id") String enterpriseId,
                             @RequestBody Map<String, String> body) {
        String jobId = body.get("job_id");
        jobService.offline(enterpriseId, jobId);
        return R.ok("岗位已下架");
    }

    @GetMapping("/list")
    public R<PageResponse<JobListResponse>> getJobList(@RequestHeader("X-User-Id") String enterpriseId,
                                                        @RequestParam(defaultValue = "1") Integer page,
                                                        @RequestParam(defaultValue = "10") Integer size,
                                                        @RequestParam(required = false) String status) {
        Integer statusInt = parseStatus(status);
        return R.ok(jobService.getMyJobList(enterpriseId, statusInt, page, size));
    }

    @GetMapping("/detail")
    public R<JobDetailResponse> getJobDetail(@RequestParam("job_id") String jobId) {
        return R.ok(jobService.getJobDetail(jobId));
    }

    @GetMapping("/apply/list")
    public R<PageResponse<ApplyListResponse>> getApplyList(@RequestHeader("X-User-Id") String enterpriseId,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "10") Integer size,
                                                           @RequestParam(required = false) Integer status) {
        return R.ok(jobService.getEnterpriseApplyList(enterpriseId, status, page, size));
    }

    @GetMapping("/v1/pc/enterprise/apply/list")
    public R<PageResponse<ApplyListResponse>> getApplyListDirect(@RequestHeader("X-User-Id") String enterpriseId,
                                                                 @RequestParam(defaultValue = "1") Integer page,
                                                                 @RequestParam(defaultValue = "10") Integer size,
                                                                 @RequestParam(required = false) Integer status) {
        return R.ok(jobService.getEnterpriseApplyList(enterpriseId, status, page, size));
    }

    @GetMapping("/my-list")
    public R<PageResponse<JobListResponse>> getMyJobList(@RequestHeader("X-User-Id") String enterpriseId,
                                                          @RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(jobService.getMyJobList(enterpriseId, null, page, size));
    }

    private Integer parseStatus(String status) {
        if (status == null || status.isEmpty()) {
            return null;
        }
        switch (status.toLowerCase()) {
            case "pending": return 0;
            case "approved": return 1;
            case "rejected":
            case "offline": return 2;
            default:
                try {
                    return Integer.parseInt(status);
                } catch (NumberFormatException e) {
                    return null;
                }
        }
    }
}
