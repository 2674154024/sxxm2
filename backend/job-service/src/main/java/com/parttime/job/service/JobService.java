package com.parttime.job.service;

import com.parttime.job.dto.request.JobAuditRequest;
import com.parttime.job.dto.request.JobPublishRequest;
import com.parttime.job.dto.request.JobSearchRequest;
import com.parttime.job.dto.request.JobUpdateRequest;
import com.parttime.job.dto.response.ApplyListResponse;
import com.parttime.job.dto.response.JobDetailResponse;
import com.parttime.job.dto.response.JobListResponse;
import com.parttime.job.dto.response.PageResponse;

import java.util.List;

public interface JobService {

    void publish(String enterpriseId, JobPublishRequest request);

    void update(String enterpriseId, JobUpdateRequest request);

    void offline(String enterpriseId, String jobId);

    PageResponse<JobListResponse> getMyJobList(String enterpriseId, Integer status, Integer page, Integer size);

    PageResponse<JobListResponse> getPublicJobList(JobSearchRequest request);

    PageResponse<JobListResponse> getLbsJobList(JobSearchRequest request);

    JobDetailResponse getJobDetail(String jobId);

    List<JobListResponse> getSimilarJobs(String jobId, Integer limit);

    void incrementViewCount(String jobId);

    void incrementApplyCount(String jobId);

    PageResponse<JobListResponse> getAuditList(Integer page, Integer size);

    void audit(JobAuditRequest request);

    String applyJob(String studentId, String jobId);

    PageResponse<ApplyListResponse> getApplyList(String studentId, Integer status, Integer page, Integer size);

    PageResponse<ApplyListResponse> getEnterpriseApplyList(String enterpriseId, Integer status, Integer page, Integer size);

    void cancelApply(String studentId, String applyId);

    Boolean isApplied(String studentId, String jobId);

    Boolean toggleFavorite(String studentId, String jobId);

    Boolean isFavorite(String studentId, String jobId);

    PageResponse<JobListResponse> getFavoriteList(String studentId, Integer page, Integer size);
}