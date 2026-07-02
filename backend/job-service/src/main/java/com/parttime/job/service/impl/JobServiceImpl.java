package com.parttime.job.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parttime.common.annotation.AuditLog;
import com.parttime.common.exception.BusinessException;
import com.parttime.common.util.AesUtil;
import com.parttime.common.util.SensitiveWordFilter;
import com.parttime.job.dto.request.JobAuditRequest;
import com.parttime.job.dto.request.JobPublishRequest;
import com.parttime.job.dto.request.JobSearchRequest;
import com.parttime.job.dto.request.JobUpdateRequest;
import com.parttime.job.dto.response.ApplyListResponse;
import com.parttime.job.dto.response.JobDetailResponse;
import com.parttime.job.dto.response.JobListResponse;
import com.parttime.job.dto.response.PageResponse;
import com.parttime.common.entity.JobApplyEntity;
import com.parttime.job.entity.JobEntity;
import com.parttime.job.entity.JobFavoriteEntity;
import com.parttime.job.mapper.JobApplyMapper;
import com.parttime.job.mapper.JobFavoriteMapper;
import com.parttime.job.mapper.JobMapper;
import com.parttime.job.service.JobService;
import com.parttime.common.util.DesensitizeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobMapper jobMapper;
    private final JobApplyMapper jobApplyMapper;
    private final JobFavoriteMapper jobFavoriteMapper;
    private final RestHighLevelClient esClient;
    private final StringRedisTemplate redisTemplate;

    @Value("${security.min-hourly-wage:17}")
    private BigDecimal minHourlyWage;

    private static final String INDEX_NAME = "job";
    private static final String VIEW_COUNT_PREFIX = "job:view:";
    private static final String APPLY_COUNT_PREFIX = "job:apply:";

    @Override
    @AuditLog(module = "岗位管理", action = "发布岗位")
    @Transactional
    public void publish(String enterpriseId, JobPublishRequest request) {
        checkEnterpriseCertified(enterpriseId);

        if (request.getSalaryAmount().compareTo(minHourlyWage) < 0) {
            throw new BusinessException(422, "时薪不低于长沙最低时薪(" + minHourlyWage + "元)");
        }

        int status = 0;

        String jobId = UUID.randomUUID().toString();

        double[] location = mockGeocoding(request.getWorkAddress());

        JobEntity job = JobEntity.builder()
                .id(jobId)
                .enterpriseId(enterpriseId)
                .jobTitle(request.getJobTitle())
                .jobType(request.getJobType())
                .industryTag(request.getIndustryTag())
                .salaryType(request.getSalaryType())
                .salaryAmount(request.getSalaryAmount())
                .settlementType(request.getSettlementType())
                .workAddress(request.getWorkAddress())
                .longitude(BigDecimal.valueOf(location[0]))
                .latitude(BigDecimal.valueOf(location[1]))
                .workTime(request.getWorkTime())
                .skillRequire(request.getSkillRequire())
                .recruitNum(request.getRecruitNum())
                .status(status)
                .isInsured(request.getIsInsured())
                .viewCount(0)
                .applyCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        jobMapper.insert(job);

        log.info("岗位发布成功，等待审核: jobId={}", jobId);
    }

    @Override
    @AuditLog(module = "岗位管理", action = "编辑岗位")
    @Transactional
    public void update(String enterpriseId, JobUpdateRequest request) {
        JobEntity job = jobMapper.selectById(request.getJobId());

        if (job == null) {
            throw new BusinessException("岗位不存在");
        }

        if (!job.getEnterpriseId().equals(enterpriseId)) {
            throw new BusinessException(403, "无权编辑此岗位");
        }

        if (request.getJobTitle() != null) {
            job.setJobTitle(request.getJobTitle());
        }
        if (request.getJobType() != null) {
            job.setJobType(request.getJobType());
        }
        if (request.getIndustryTag() != null) {
            job.setIndustryTag(request.getIndustryTag());
        }
        if (request.getSalaryType() != null) {
            job.setSalaryType(request.getSalaryType());
        }
        if (request.getSalaryAmount() != null) {
            if (request.getSalaryAmount().compareTo(minHourlyWage) < 0) {
                throw new BusinessException(422, "时薪不低于长沙最低时薪(" + minHourlyWage + "元)");
            }
            job.setSalaryAmount(request.getSalaryAmount());
        }
        if (request.getSettlementType() != null) {
            job.setSettlementType(request.getSettlementType());
        }
        if (request.getWorkAddress() != null) {
            job.setWorkAddress(request.getWorkAddress());
            double[] location = mockGeocoding(request.getWorkAddress());
            job.setLongitude(BigDecimal.valueOf(location[0]));
            job.setLatitude(BigDecimal.valueOf(location[1]));
        }
        if (request.getWorkTime() != null) {
            job.setWorkTime(request.getWorkTime());
        }
        if (request.getSkillRequire() != null) {
            job.setSkillRequire(request.getSkillRequire());
        }
        if (request.getRecruitNum() != null) {
            job.setRecruitNum(request.getRecruitNum());
        }
        if (request.getIsInsured() != null) {
            job.setIsInsured(request.getIsInsured());
        }

        int oldStatus = job.getStatus();
        job.setStatus(0);

        job.setUpdatedAt(LocalDateTime.now());

        jobMapper.updateById(job);

        if (oldStatus == 1) {
            deleteFromEs(request.getJobId());
        }

        log.info("岗位更新成功，进入审核流程: jobId={}", request.getJobId());
    }

    @Override
    @AuditLog(module = "岗位管理", action = "下架岗位")
    @Transactional
    public void offline(String enterpriseId, String jobId) {
        JobEntity job = jobMapper.selectById(jobId);

        if (job == null) {
            throw new BusinessException("岗位不存在");
        }

        if (!job.getEnterpriseId().equals(enterpriseId)) {
            throw new BusinessException(403, "无权操作此岗位");
        }

        job.setStatus(2);
        job.setUpdatedAt(LocalDateTime.now());

        jobMapper.updateById(job);

        deleteFromEs(jobId);

        log.info("岗位下架成功: jobId={}", jobId);
    }

    @Override
    public PageResponse<JobListResponse> getMyJobList(String enterpriseId, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<JobEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobEntity::getEnterpriseId, enterpriseId);
        if (status != null) {
            wrapper.eq(JobEntity::getStatus, status);
        }
        wrapper.orderByDesc(JobEntity::getCreatedAt);

        Page<JobEntity> pageRequest = new Page<>(page, size);
        IPage<JobEntity> result = jobMapper.selectPage(pageRequest, wrapper);

        List<JobListResponse> list = new ArrayList<>();
        for (JobEntity job : result.getRecords()) {
            list.add(convertToListResponse(job));
        }

        PageResponse<JobListResponse> response = new PageResponse<>();
        response.setList(list);
        response.setTotal(result.getTotal());
        response.setPage(page);
        response.setSize(size);
        response.setPages((int) Math.ceil((double) result.getTotal() / size));
        return response;
    }

    @Override
    public PageResponse<JobListResponse> getPublicJobList(JobSearchRequest request) {
        return searchJobs(request, null, null);
    }

    @Override
    public PageResponse<JobListResponse> getLbsJobList(JobSearchRequest request) {
        if (request.getLongitude() == null || request.getLatitude() == null) {
            throw new BusinessException("位置信息不能为空");
        }
        return searchJobs(request, request.getLongitude().doubleValue(), request.getLatitude().doubleValue());
    }

    @Override
    public JobDetailResponse getJobDetail(String jobId) {
        JobEntity job = jobMapper.selectById(jobId);

        if (job == null) {
            throw new BusinessException("岗位不存在");
        }

        incrementViewCount(jobId);

        return convertToDetailResponse(job);
    }

    @Override
    public List<JobListResponse> getSimilarJobs(String jobId, Integer limit) {
        JobEntity currentJob = jobMapper.selectById(jobId);
        if (currentJob == null) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<JobEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(JobEntity::getId, jobId);
        wrapper.eq(JobEntity::getStatus, 1);
        if (currentJob.getIndustryTag() != null) {
            wrapper.eq(JobEntity::getIndustryTag, currentJob.getIndustryTag());
        }
        wrapper.orderByDesc(JobEntity::getCreatedAt);
        wrapper.last("LIMIT " + limit);

        List<JobEntity> jobs = jobMapper.selectList(wrapper);
        return jobs.stream()
                .map(this::convertToListResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void incrementViewCount(String jobId) {
        String redisKey = VIEW_COUNT_PREFIX + jobId;
        redisTemplate.opsForValue().increment(redisKey);
        redisTemplate.expire(redisKey, 600, TimeUnit.SECONDS);
    }

    @Override
    @Transactional
    public void incrementApplyCount(String jobId) {
        String redisKey = APPLY_COUNT_PREFIX + jobId;
        redisTemplate.opsForValue().increment(redisKey);

        jobMapper.incrementApplyCount(jobId);
    }

    @Override
    public PageResponse<JobListResponse> getAuditList(Integer page, Integer size) {
        LambdaQueryWrapper<JobEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobEntity::getStatus, 0);
        wrapper.orderByDesc(JobEntity::getCreatedAt);

        Page<JobEntity> pageRequest = new Page<>(page, size);
        IPage<JobEntity> result = jobMapper.selectPage(pageRequest, wrapper);

        List<JobListResponse> list = new ArrayList<>();
        for (JobEntity job : result.getRecords()) {
            list.add(convertToListResponse(job));
        }

        PageResponse<JobListResponse> response = new PageResponse<>();
        response.setList(list);
        response.setTotal(result.getTotal());
        response.setPage(page);
        response.setSize(size);
        response.setPages((int) Math.ceil((double) result.getTotal() / size));
        return response;
    }

    @Override
    @AuditLog(module = "岗位审核", action = "审核岗位")
    @Transactional
    public void audit(JobAuditRequest request) {
        JobEntity job = jobMapper.selectById(request.getJobId());

        if (job == null) {
            throw new BusinessException("岗位不存在");
        }

        if (job.getStatus() != 0) {
            throw new BusinessException("岗位状态不允许审核");
        }

        if (request.getAuditResult() == 1) {
            job.setStatus(1);
            indexToEs(job);
        } else {
            job.setStatus(2);
        }

        job.setUpdatedAt(LocalDateTime.now());

        jobMapper.updateById(job);

        log.info("岗位审核完成: jobId={}, result={}", request.getJobId(), request.getAuditResult());
    }

    private PageResponse<JobListResponse> searchJobs(JobSearchRequest request, Double longitude, Double latitude) {
        try {
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.filter(QueryBuilders.termQuery("status", 1));

            if (longitude != null && latitude != null) {
                GeoDistanceQueryBuilder geoQuery = QueryBuilders.geoDistanceQuery("location")
                        .point(latitude, longitude)
                        .distance(request.getDistance().doubleValue(), org.elasticsearch.common.unit.DistanceUnit.KILOMETERS);
                boolQuery.filter(geoQuery);
            }

            if (request.getSalaryMin() != null) {
                boolQuery.filter(QueryBuilders.rangeQuery("salary_amount").gte(request.getSalaryMin().doubleValue()));
            }
            if (request.getSalaryMax() != null) {
                boolQuery.filter(QueryBuilders.rangeQuery("salary_amount").lte(request.getSalaryMax().doubleValue()));
            }
            if (request.getSettlementType() != null) {
                boolQuery.filter(QueryBuilders.termQuery("settlement_type", request.getSettlementType()));
            }
            if (request.getIndustryTag() != null) {
                boolQuery.filter(QueryBuilders.termQuery("industry_tag", request.getIndustryTag()));
            }
            if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("job_title", request.getKeyword())
                        .operator(org.elasticsearch.index.query.Operator.OR);
                boolQuery.should(matchQuery);
                boolQuery.minimumShouldMatch(1);
            }
            if (request.getIsInsured() != null && request.getIsInsured() == 1) {
                boolQuery.filter(QueryBuilders.termQuery("is_insured", 1));
            }
            if (request.getWorkTime() != null && !request.getWorkTime().isEmpty()) {
                boolQuery.filter(QueryBuilders.termQuery("work_time", request.getWorkTime()));
            }

            sourceBuilder.query(boolQuery);

            if (longitude != null && latitude != null) {
                sourceBuilder.sort(SortBuilders.geoDistanceSort("location", latitude, longitude)
                        .order(SortOrder.ASC));
            }
            sourceBuilder.sort(new FieldSortBuilder("create_time").order(SortOrder.DESC));
            sourceBuilder.sort(new FieldSortBuilder("enterprise_credit_score").order(SortOrder.DESC));

            sourceBuilder.from((request.getPage() - 1) * request.getSize());
            sourceBuilder.size(request.getSize());

            SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
            searchRequest.source(sourceBuilder);

            SearchResponse response = esClient.search(searchRequest, RequestOptions.DEFAULT);

            List<JobListResponse> list = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                JSONObject source = JSON.parseObject(hit.getSourceAsString());
                JobListResponse item = JobListResponse.builder()
                        .jobId(source.getString("job_id"))
                        .jobTitle(source.getString("job_title"))
                        .jobType(source.getString("job_type"))
                        .industryTag(source.getString("industry_tag"))
                        .salaryType(source.getIntValue("salary_type"))
                        .salaryAmount(source.getBigDecimal("salary_amount"))
                        .settlementType(source.getIntValue("settlement_type"))
                        .workAddress(source.getString("work_address"))
                        .longitude(source.getBigDecimal("longitude"))
                        .latitude(source.getBigDecimal("latitude"))
                        .isInsured(source.getIntValue("is_insured"))
                        .viewCount(source.getIntValue("view_count"))
                        .enterpriseName(source.getString("enterprise_name"))
                        .enterpriseCreditScore(source.getIntValue("enterprise_credit_score"))
                        .createdAt(source.getString("create_time"))
                        .build();
                list.add(item);
            }

            PageResponse<JobListResponse> pageResponse = new PageResponse<>();
            pageResponse.setList(list);
            pageResponse.setTotal(response.getHits().getTotalHits().value);
            pageResponse.setPage(request.getPage());
            pageResponse.setSize(request.getSize());
            pageResponse.setPages((int) Math.ceil((double) response.getHits().getTotalHits().value / request.getSize()));
            return pageResponse;

        } catch (Exception e) {
            log.error("ES搜索失败: {}", e.getMessage());
            return fallbackSearch(request);
        }
    }

    private PageResponse<JobListResponse> fallbackSearch(JobSearchRequest request) {
        LambdaQueryWrapper<JobEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobEntity::getStatus, 1);

        if (request.getSalaryMin() != null) {
            wrapper.ge(JobEntity::getSalaryAmount, request.getSalaryMin());
        }
        if (request.getSalaryMax() != null) {
            wrapper.le(JobEntity::getSalaryAmount, request.getSalaryMax());
        }
        if (request.getSettlementType() != null) {
            wrapper.eq(JobEntity::getSettlementType, request.getSettlementType());
        }
        if (request.getIndustryTag() != null) {
            wrapper.eq(JobEntity::getIndustryTag, request.getIndustryTag());
        }
        if (request.getIsInsured() != null && request.getIsInsured() == 1) {
            wrapper.eq(JobEntity::getIsInsured, 1);
        }
        if (request.getWorkTime() != null && !request.getWorkTime().isEmpty()) {
            wrapper.apply("JSON_CONTAINS(work_time, JSON_OBJECT('day', {0}))", request.getWorkTime());
        }

        Page<JobEntity> pageRequest = new Page<>(request.getPage(), request.getSize());
        IPage<JobEntity> result = jobMapper.selectPage(pageRequest, wrapper);

        List<JobListResponse> list = new ArrayList<>();
        for (JobEntity job : result.getRecords()) {
            list.add(convertToListResponse(job));
        }

        PageResponse<JobListResponse> pageResponse = new PageResponse<>();
        pageResponse.setList(list);
        pageResponse.setTotal(result.getTotal());
        pageResponse.setPage(request.getPage());
        pageResponse.setSize(request.getSize());
        pageResponse.setPages((int) Math.ceil((double) result.getTotal() / request.getSize()));
        return pageResponse;
    }

    private void indexToEs(JobEntity job) {
        try {
            JSONObject doc = new JSONObject();
            doc.put("job_id", job.getId());
            doc.put("job_title", job.getJobTitle());
            doc.put("job_type", job.getJobType());
            doc.put("industry_tag", job.getIndustryTag());
            doc.put("salary_type", job.getSalaryType());
            doc.put("salary_amount", job.getSalaryAmount());
            doc.put("settlement_type", job.getSettlementType());
            doc.put("work_address", job.getWorkAddress());
            doc.put("longitude", job.getLongitude());
            doc.put("latitude", job.getLatitude());

            JSONArray location = new JSONArray();
            location.add(job.getLatitude().doubleValue());
            location.add(job.getLongitude().doubleValue());
            doc.put("location", location);

            doc.put("work_time", job.getWorkTime());
            doc.put("skill_require", job.getSkillRequire());
            doc.put("recruit_num", job.getRecruitNum());
            doc.put("status", job.getStatus());
            doc.put("is_insured", job.getIsInsured());
            doc.put("view_count", job.getViewCount());
            doc.put("enterprise_name", mockGetEnterpriseName(job.getEnterpriseId()));
            doc.put("enterprise_credit_score", mockGetEnterpriseCreditScore(job.getEnterpriseId()));
            doc.put("create_time", job.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            org.elasticsearch.action.index.IndexRequest request = new org.elasticsearch.action.index.IndexRequest(INDEX_NAME)
                    .id(job.getId())
                    .source(doc.toJSONString(), org.elasticsearch.xcontent.XContentType.JSON);

            esClient.index(request, RequestOptions.DEFAULT);

            log.info("岗位索引到ES成功: jobId={}", job.getId());
        } catch (Exception e) {
            log.error("岗位索引到ES失败: {}", e.getMessage());
        }
    }

    private void deleteFromEs(String jobId) {
        try {
            org.elasticsearch.action.delete.DeleteRequest request = new org.elasticsearch.action.delete.DeleteRequest(INDEX_NAME, jobId);
            esClient.delete(request, RequestOptions.DEFAULT);
            log.info("岗位从ES删除成功: jobId={}", jobId);
        } catch (Exception e) {
            log.error("岗位从ES删除失败: {}", e.getMessage());
        }
    }

    private JobListResponse convertToListResponse(JobEntity job) {
        return JobListResponse.builder()
                .jobId(job.getId())
                .jobTitle(job.getJobTitle())
                .jobType(job.getJobType())
                .industryTag(job.getIndustryTag())
                .salaryType(job.getSalaryType())
                .salaryTypeStr(convertSalaryType(job.getSalaryType()))
                .salaryAmount(job.getSalaryAmount())
                .settlementType(job.getSettlementType())
                .settlementTypeStr(convertSettlementType(job.getSettlementType()))
                .workAddress(job.getWorkAddress())
                .longitude(job.getLongitude())
                .latitude(job.getLatitude())
                .statusStr(convertStatus(job.getStatus()))
                .isInsured(job.getIsInsured())
                .hasInsurance(job.getIsInsured() != null && job.getIsInsured() == 1)
                .viewCount(job.getViewCount())
                .enterpriseName(mockGetEnterpriseName(job.getEnterpriseId()))
                .enterpriseCreditScore(mockGetEnterpriseCreditScore(job.getEnterpriseId()))
                .isCertified(mockIsEnterpriseCertified(job.getEnterpriseId()))
                .skillTags(List.of())
                .distance(null)
                .createdAt(job.getCreatedAt() != null ? job.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "")
                .build();
    }

    private String convertStatus(Integer status) {
        if (status == null) return "pending";
        return switch (status) {
            case 0 -> "pending";
            case 1 -> "approved";
            case 2 -> "rejected";
            case 3 -> "offline";
            default -> "pending";
        };
    }

    private String convertSalaryType(Integer type) {
        if (type == null) return "hourly";
        return switch (type) {
            case 1 -> "hourly";
            case 2 -> "daily";
            case 3 -> "monthly";
            default -> "hourly";
        };
    }

    private String convertSettlementType(Integer type) {
        if (type == null) return "daily";
        return switch (type) {
            case 1 -> "daily";
            case 2 -> "weekly";
            case 3 -> "monthly";
            default -> "daily";
        };
    }

    private Boolean mockIsEnterpriseCertified(String enterpriseId) {
        return true;
    }

    private List<String> parseSkillTags(String skillRequire) {
        if (skillRequire == null || skillRequire.isEmpty()) {
            return List.of();
        }
        try {
            JSONObject json = JSON.parseObject(skillRequire);
            return json.getJSONArray("tags").toJavaList(String.class);
        } catch (Exception e) {
            return List.of(skillRequire);
        }
    }

    private JobDetailResponse convertToDetailResponse(JobEntity job) {
        return JobDetailResponse.builder()
                .jobId(job.getId())
                .jobTitle(job.getJobTitle())
                .jobType(job.getJobType())
                .industryTag(job.getIndustryTag())
                .salaryType(job.getSalaryType())
                .salaryAmount(job.getSalaryAmount())
                .settlementType(job.getSettlementType())
                .workAddress(job.getWorkAddress())
                .longitude(job.getLongitude())
                .latitude(job.getLatitude())
                .workTime(job.getWorkTime())
                .skillRequire(job.getSkillRequire())
                .recruitNum(job.getRecruitNum())
                .status(job.getStatus())
                .isInsured(job.getIsInsured())
                .viewCount(job.getViewCount())
                .applyCount(job.getApplyCount())
                .enterpriseName(mockGetEnterpriseName(job.getEnterpriseId()))
                .enterpriseCreditScore(mockGetEnterpriseCreditScore(job.getEnterpriseId()))
                .enterpriseVerifyStatus(mockGetEnterpriseVerifyStatus(job.getEnterpriseId()))
                .contactPhone(DesensitizeUtil.desensitizePhone(mockGetEnterpriseContactPhone(job.getEnterpriseId())))
                .createdAt(job.getCreatedAt())
                .build();
    }

    private void checkEnterpriseCertified(String enterpriseId) {
        int verifyStatus = mockGetEnterpriseVerifyStatus(enterpriseId);
        if (verifyStatus != 2) {
            throw new BusinessException(422, "企业未认证，无法发布岗位");
        }
    }

    private double[] mockGeocoding(String address) {
        return new double[]{112.9388, 28.2282};
    }

    private String mockGetEnterpriseName(String enterpriseId) {
        return "测试企业";
    }

    private Integer mockGetEnterpriseCreditScore(String enterpriseId) {
        return 100;
    }

    private Integer mockGetEnterpriseVerifyStatus(String enterpriseId) {
        return 2;
    }

    private String mockGetEnterpriseContactPhone(String enterpriseId) {
        return "13800138000";
    }

    @Override
    @Transactional
    @AuditLog(module = "岗位投递", action = "投递岗位")
    public String applyJob(String studentId, String jobId) {
        JobEntity job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException("岗位不存在");
        }
        if (job.getStatus() != 1) {
            throw new BusinessException("岗位不可投递");
        }

        int existingCount = jobApplyMapper.countByStudentAndJob(studentId, jobId);
        if (existingCount > 0) {
            throw new BusinessException("您已投递过该岗位");
        }

        String applyId = "apply-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        JobApplyEntity apply = JobApplyEntity.builder()
                .id(applyId)
                .studentId(studentId)
                .jobId(jobId)
                .applyStatus(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        jobApplyMapper.insert(apply);

        incrementApplyCount(jobId);

        return applyId;
    }

    @Override
    @AuditLog(module = "岗位投递", action = "查询投递记录")
    public PageResponse<ApplyListResponse> getApplyList(String studentId, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<JobApplyEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobApplyEntity::getStudentId, studentId);
        if (status != null) {
            wrapper.eq(JobApplyEntity::getApplyStatus, status);
        }
        wrapper.orderByDesc(JobApplyEntity::getCreatedAt);

        Page<JobApplyEntity> pageRequest = new Page<>(page, size);
        IPage<JobApplyEntity> result = jobApplyMapper.selectPage(pageRequest, wrapper);

        List<ApplyListResponse> list = new ArrayList<>();
        for (JobApplyEntity apply : result.getRecords()) {
            JobEntity job = jobMapper.selectById(apply.getJobId());
            if (job != null) {
                list.add(ApplyListResponse.builder()
                        .applyId(apply.getId())
                        .jobId(job.getId())
                        .jobTitle(job.getJobTitle())
                        .jobType(job.getJobType())
                        .salaryAmount(job.getSalaryAmount())
                        .settlementType(job.getSettlementType())
                        .workAddress(job.getWorkAddress())
                        .enterpriseName(mockGetEnterpriseName(job.getEnterpriseId()))
                        .applyStatus(apply.getApplyStatus())
                        .interviewTime(apply.getInterviewTime())
                        .interviewType(apply.getInterviewType())
                        .createdAt(apply.getCreatedAt())
                        .build());
            }
        }

        PageResponse<ApplyListResponse> response = new PageResponse<>();
        response.setList(list);
        response.setTotal(result.getTotal());
        response.setPage(page);
        response.setSize(size);
        response.setPages((int) Math.ceil((double) result.getTotal() / size));
        return response;
    }

    @Override
    @AuditLog(module = "岗位投递", action = "企业查询投递记录")
    public PageResponse<ApplyListResponse> getEnterpriseApplyList(String enterpriseId, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<JobEntity> jobWrapper = new LambdaQueryWrapper<>();
        jobWrapper.eq(JobEntity::getEnterpriseId, enterpriseId);
        List<JobEntity> enterpriseJobs = jobMapper.selectList(jobWrapper);
        
        if (enterpriseJobs.isEmpty()) {
            PageResponse<ApplyListResponse> response = new PageResponse<>();
            response.setList(new ArrayList<>());
            response.setTotal(0L);
            response.setPage(page);
            response.setSize(size);
            response.setPages(0);
            return response;
        }

        List<String> jobIds = enterpriseJobs.stream()
                .map(JobEntity::getId)
                .collect(java.util.stream.Collectors.toList());

        LambdaQueryWrapper<JobApplyEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(JobApplyEntity::getJobId, jobIds);
        if (status != null) {
            wrapper.eq(JobApplyEntity::getApplyStatus, status);
        }
        wrapper.orderByDesc(JobApplyEntity::getCreatedAt);

        Page<JobApplyEntity> pageRequest = new Page<>(page, size);
        IPage<JobApplyEntity> result = jobApplyMapper.selectPage(pageRequest, wrapper);

        List<ApplyListResponse> list = new ArrayList<>();
        for (JobApplyEntity apply : result.getRecords()) {
            JobEntity job = jobMapper.selectById(apply.getJobId());
            if (job != null) {
                String statusStr = "pending";
                if (apply.getApplyStatus() != null) {
                    switch (apply.getApplyStatus()) {
                        case 0: statusStr = "pending"; break;
                        case 1: statusStr = "interview"; break;
                        case 2: statusStr = "hired"; break;
                        case 3: statusStr = "rejected"; break;
                        default: statusStr = "pending";
                    }
                }
                
                String applyTimeStr = apply.getCreatedAt() != null 
                    ? apply.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) 
                    : "";
                
                list.add(ApplyListResponse.builder()
                        .applyId(apply.getId())
                        .jobId(job.getId())
                        .jobTitle(job.getJobTitle())
                        .jobType(job.getJobType())
                        .salaryAmount(job.getSalaryAmount())
                        .settlementType(job.getSettlementType())
                        .workAddress(job.getWorkAddress())
                        .enterpriseName(mockGetEnterpriseName(job.getEnterpriseId()))
                        .applyStatus(apply.getApplyStatus())
                        .interviewTime(apply.getInterviewTime())
                        .interviewType(apply.getInterviewType())
                        .createdAt(apply.getCreatedAt())
                        .statusStr(statusStr)
                        .applyTime(applyTimeStr)
                        .build());
            }
        }

        PageResponse<ApplyListResponse> response = new PageResponse<>();
        response.setList(list);
        response.setTotal(result.getTotal());
        response.setPage(page);
        response.setSize(size);
        response.setPages((int) Math.ceil((double) result.getTotal() / size));
        return response;
    }

    @Override
    @Transactional
    @AuditLog(module = "岗位投递", action = "取消投递")
    public void cancelApply(String studentId, String applyId) {
        JobApplyEntity apply = jobApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("投递记录不存在");
        }
        if (!apply.getStudentId().equals(studentId)) {
            throw new BusinessException(403, "无权取消此投递");
        }
        if (apply.getApplyStatus() != 0) {
            throw new BusinessException("只能取消待审核的投递");
        }
        
        LambdaQueryWrapper<JobApplyEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobApplyEntity::getId, applyId);
        wrapper.eq(JobApplyEntity::getStudentId, studentId);
        jobApplyMapper.delete(wrapper);
        
        log.info("取消投递: applyId={}, studentId={}", applyId, studentId);
    }

    @Override
    public Boolean isApplied(String studentId, String jobId) {
        int count = jobApplyMapper.countByStudentAndJob(studentId, jobId);
        return count > 0;
    }

    @Override
    @Transactional
    @AuditLog(module = "岗位收藏", action = "切换收藏状态")
    public Boolean toggleFavorite(String studentId, String jobId) {
        int count = jobFavoriteMapper.countByStudentAndJob(studentId, jobId);
        if (count > 0) {
            LambdaQueryWrapper<JobFavoriteEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(JobFavoriteEntity::getStudentId, studentId);
            wrapper.eq(JobFavoriteEntity::getJobId, jobId);
            jobFavoriteMapper.delete(wrapper);
            return false;
        } else {
            String favoriteId = "fav-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            JobFavoriteEntity favorite = JobFavoriteEntity.builder()
                    .id(favoriteId)
                    .studentId(studentId)
                    .jobId(jobId)
                    .createdAt(LocalDateTime.now())
                    .build();
            jobFavoriteMapper.insert(favorite);
            return true;
        }
    }

    @Override
    public Boolean isFavorite(String studentId, String jobId) {
        int count = jobFavoriteMapper.countByStudentAndJob(studentId, jobId);
        return count > 0;
    }

    @Override
    @AuditLog(module = "岗位收藏", action = "查询收藏列表")
    public PageResponse<JobListResponse> getFavoriteList(String studentId, Integer page, Integer size) {
        LambdaQueryWrapper<JobFavoriteEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobFavoriteEntity::getStudentId, studentId);
        wrapper.orderByDesc(JobFavoriteEntity::getCreatedAt);

        Page<JobFavoriteEntity> pageRequest = new Page<>(page, size);
        IPage<JobFavoriteEntity> result = jobFavoriteMapper.selectPage(pageRequest, wrapper);

        List<JobListResponse> list = new ArrayList<>();
        for (JobFavoriteEntity favorite : result.getRecords()) {
            JobEntity job = jobMapper.selectById(favorite.getJobId());
            if (job != null) {
                list.add(convertToListResponse(job));
            }
        }

        PageResponse<JobListResponse> response = new PageResponse<>();
        response.setList(list);
        response.setTotal(result.getTotal());
        response.setPage(page);
        response.setSize(size);
        response.setPages((int) Math.ceil((double) result.getTotal() / size));
        return response;
    }
}