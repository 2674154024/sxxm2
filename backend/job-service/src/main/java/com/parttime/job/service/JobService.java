package com.parttime.job.service;

import com.parttime.job.dto.request.JobAuditRequest;
import com.parttime.job.dto.request.JobPublishRequest;
import com.parttime.job.dto.request.JobSearchRequest;
import com.parttime.job.dto.request.JobUpdateRequest;
import com.parttime.job.dto.response.JobDetailResponse;
import com.parttime.job.dto.response.JobListResponse;
import com.parttime.job.dto.response.PageResponse;

public interface JobService {

    void publish(String enterpriseId, JobPublishRequest request);

    void update(String enterpriseId, JobUpdateRequest request);

    void offline(String enterpriseId, String jobId);

    PageResponse<JobListResponse> getMyJobList(String enterpriseId, Integer page, Integer size);

    PageResponse<JobListResponse> getPublicJobList(JobSearchRequest request);

    PageResponse<JobListResponse> getLbsJobList(JobSearchRequest request);

    JobDetailResponse getJobDetail(String jobId);

    void incrementViewCount(String jobId);

    void incrementApplyCount(String jobId);

    PageResponse<JobListResponse> getAuditList(Integer page, Integer size);

    void audit(JobAuditRequest request);
}