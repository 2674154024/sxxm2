package com.parttime.job.controller;

import com.parttime.common.response.R;
import com.parttime.job.dto.request.JobSearchRequest;
import com.parttime.job.dto.response.ApplyListResponse;
import com.parttime.job.dto.response.JobDetailResponse;
import com.parttime.job.dto.response.JobListResponse;
import com.parttime.job.dto.response.PageResponse;
import com.parttime.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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
                                                              @RequestParam(required = false) Integer isInsured,
                                                              @RequestParam(required = false) String workTime,
                                                              @RequestParam(defaultValue = "1") Integer page,
                                                              @RequestParam(defaultValue = "10") Integer size) {
        JobSearchRequest request = new JobSearchRequest();
        request.setSalaryMin(salaryMin);
        request.setSalaryMax(salaryMax);
        request.setSettlementType(settlementType);
        request.setIndustryTag(industryTag);
        request.setKeyword(keyword);
        request.setIsInsured(isInsured);
        request.setWorkTime(workTime);
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

    @GetMapping("/public/job/detail")
    public R<JobDetailResponse> getPublicJobDetail(@RequestParam("job_id") String jobId) {
        return R.ok(jobService.getJobDetail(jobId));
    }

    @GetMapping("/public/job/similar")
    public R<List<JobListResponse>> getSimilarJobs(@RequestParam("job_id") String jobId,
                                                    @RequestParam(defaultValue = "5") Integer limit) {
        return R.ok(jobService.getSimilarJobs(jobId, limit));
    }

    @GetMapping("/student/job/detail")
    public R<JobDetailResponse> getJobDetail(@RequestParam("job_id") String jobId) {
        jobService.incrementViewCount(jobId);
        return R.ok(jobService.getJobDetail(jobId));
    }

    @PostMapping("/student/job/apply")
    public R<String> applyJob(@RequestHeader("X-User-Id") String studentId,
                               @RequestParam("job_id") String jobId) {
        String applyId = jobService.applyJob(studentId, jobId);
        return R.ok(applyId);
    }

    @GetMapping("/student/job/apply/list")
    public R<PageResponse<ApplyListResponse>> getApplyList(@RequestHeader("X-User-Id") String studentId,
                                                            @RequestParam(required = false) Integer status,
                                                            @RequestParam(defaultValue = "1") Integer page,
                                                            @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(jobService.getApplyList(studentId, status, page, size));
    }

    @DeleteMapping("/student/job/apply/cancel")
    public R<Void> cancelApply(@RequestHeader("X-User-Id") String studentId,
                                @RequestParam("apply_id") String applyId) {
        jobService.cancelApply(studentId, applyId);
        return R.ok();
    }

    @PostMapping("/student/job/favorite/toggle")
    public R<Boolean> toggleFavorite(@RequestHeader("X-User-Id") String studentId,
                                      @RequestParam("job_id") String jobId) {
        return R.ok(jobService.toggleFavorite(studentId, jobId));
    }

    @GetMapping("/student/job/favorite/check")
    public R<Boolean> checkFavorite(@RequestHeader("X-User-Id") String studentId,
                                     @RequestParam("job_id") String jobId) {
        return R.ok(jobService.isFavorite(studentId, jobId));
    }

    @GetMapping("/student/job/apply/check")
    public R<Boolean> checkApplyStatus(@RequestHeader("X-User-Id") String studentId,
                                        @RequestParam("job_id") String jobId) {
        return R.ok(jobService.isApplied(studentId, jobId));
    }

    @GetMapping("/student/job/favorite/list")
    public R<PageResponse<JobListResponse>> getFavoriteList(@RequestHeader("X-User-Id") String studentId,
                                                              @RequestParam(defaultValue = "1") Integer page,
                                                              @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(jobService.getFavoriteList(studentId, page, size));
    }
}