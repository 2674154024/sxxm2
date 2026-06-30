package com.parttime.job.controller;

import com.parttime.common.response.R;
import com.parttime.job.dto.request.JobSearchRequest;
import com.parttime.job.dto.response.JobDetailResponse;
import com.parttime.job.dto.response.JobListResponse;
import com.parttime.job.dto.response.PageResponse;
import com.parttime.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class JobSearchController {

    private final JobService jobService;

    @GetMapping("/public/job/list")
    public R<PageResponse<JobListResponse>> getPublicJobList(@RequestParam(required = false) BigDecimal salaryMin,
                                                              @RequestParam(required = false) BigDecimal salaryMax,
                                                              @RequestParam(required = false) Integer settlementType,
                                                              @RequestParam(required = false) String industryTag,
                                                              @RequestParam(required = false) String keyword,
                                                              @RequestParam(defaultValue = "1") Integer page,
                                                              @RequestParam(defaultValue = "10") Integer size) {
        JobSearchRequest request = new JobSearchRequest();
        request.setSalaryMin(salaryMin);
        request.setSalaryMax(salaryMax);
        request.setSettlementType(settlementType);
        request.setIndustryTag(industryTag);
        request.setKeyword(keyword);
        request.setPage(page);
        request.setSize(size);
        return R.ok(jobService.getPublicJobList(request));
    }

    @GetMapping("/student/job/list")
    public R<PageResponse<JobListResponse>> getLbsJobList(@RequestParam BigDecimal longitude,
                                                           @RequestParam BigDecimal latitude,
                                                           @RequestParam(defaultValue = "5") BigDecimal distance,
                                                           @RequestParam(defaultValue = "17") BigDecimal salaryMin,
                                                           @RequestParam(required = false) BigDecimal salaryMax,
                                                           @RequestParam(required = false) Integer settlementType,
                                                           @RequestParam(required = false) String industryTag,
                                                           @RequestParam(required = false) String keyword,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "10") Integer size) {
        JobSearchRequest request = new JobSearchRequest();
        request.setLongitude(longitude);
        request.setLatitude(latitude);
        request.setDistance(distance);
        request.setSalaryMin(salaryMin);
        request.setSalaryMax(salaryMax);
        request.setSettlementType(settlementType);
        request.setIndustryTag(industryTag);
        request.setKeyword(keyword);
        request.setPage(page);
        request.setSize(size);
        return R.ok(jobService.getLbsJobList(request));
    }

    @GetMapping("/student/job/detail")
    public R<JobDetailResponse> getJobDetail(@RequestParam("job_id") String jobId) {
        return R.ok(jobService.getJobDetail(jobId));
    }
}