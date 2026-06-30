package com.parttime.match.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.parttime.common.exception.BusinessException;
import com.parttime.match.dto.JobInfo;
import com.parttime.match.dto.StudentInfo;
import com.parttime.match.dto.request.MatchCalculateRequest;
import com.parttime.match.dto.request.MatchCandidateRequest;
import com.parttime.match.dto.request.MatchRecommendRequest;
import com.parttime.match.dto.response.MatchCandidateResponse;
import com.parttime.match.dto.response.MatchRecommendResponse;
import com.parttime.match.dto.response.MatchScoreResponse;
import com.parttime.match.entity.JobMatchLogEntity;
import com.parttime.match.mapper.JobMatchLogMapper;
import com.parttime.match.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final JobMatchLogMapper matchLogMapper;
    private final RestHighLevelClient esClient;

    @Value("${match.algorithm.skill-weight:0.4}")
    private double skillWeight;

    @Value("${match.algorithm.time-weight:0.3}")
    private double timeWeight;

    @Value("${match.algorithm.distance-weight:0.3}")
    private double distanceWeight;

    @Value("${match.max-recommend:20}")
    private int maxRecommend;

    @Value("${match.search-radius:5}")
    private int searchRadius;

    private static final String JOB_INDEX_NAME = "job";

    @Override
    @Transactional
    public MatchScoreResponse calculate(MatchCalculateRequest request) {
        StudentInfo student = getStudentInfo(request.getStudentId());
        JobInfo job = getJobInfo(request.getJobId());

        double[] location = mockStudentLocation(request.getStudentId());
        student.setLongitude(BigDecimal.valueOf(location[0]));
        student.setLatitude(BigDecimal.valueOf(location[1]));

        int skillScore = calculateSkillScore(student.getSkillTags(), job.getSkillRequire());
        int timeScore = calculateTimeScore(student.getAvailableTime(), job.getWorkTime());
        double distanceKm = calculateDistance(
                student.getLatitude().doubleValue(), student.getLongitude().doubleValue(),
                job.getLatitude().doubleValue(), job.getLongitude().doubleValue()
        );
        int distanceScore = calculateDistanceScore(distanceKm);

        int matchScore = (int) (skillScore * skillWeight + timeScore * timeWeight + distanceScore * distanceWeight);

        saveMatchLog(request.getStudentId(), request.getJobId(), matchScore, skillScore, timeScore, distanceScore, distanceKm);

        return MatchScoreResponse.builder()
                .studentId(request.getStudentId())
                .jobId(request.getJobId())
                .matchScore(matchScore)
                .skillScore(skillScore)
                .timeScore(timeScore)
                .distanceScore(distanceScore)
                .distanceKm(distanceKm)
                .build();
    }

    @Override
    @Transactional
    public List<MatchRecommendResponse> recommend(MatchRecommendRequest request) {
        StudentInfo student = getStudentInfo(request.getStudentId());

        double[] location = mockStudentLocation(request.getStudentId());
        BigDecimal longitude = request.getLongitude() != null ? request.getLongitude() : BigDecimal.valueOf(location[0]);
        BigDecimal latitude = request.getLatitude() != null ? request.getLatitude() : BigDecimal.valueOf(location[1]);
        student.setLongitude(longitude);
        student.setLatitude(latitude);

        List<JobInfo> jobs = searchJobsByLocation(longitude.doubleValue(), latitude.doubleValue(),
                request.getDistance() != null ? request.getDistance() : searchRadius);

        List<MatchRecommendResponse> results = new ArrayList<>();
        for (JobInfo job : jobs) {
            int skillScore = calculateSkillScore(student.getSkillTags(), job.getSkillRequire());
            int timeScore = calculateTimeScore(student.getAvailableTime(), job.getWorkTime());
            double distanceKm = calculateDistance(
                    student.getLatitude().doubleValue(), student.getLongitude().doubleValue(),
                    job.getLatitude().doubleValue(), job.getLongitude().doubleValue()
            );
            int distanceScore = calculateDistanceScore(distanceKm);

            int matchScore = (int) (skillScore * skillWeight + timeScore * timeWeight + distanceScore * distanceWeight);

            saveMatchLog(request.getStudentId(), job.getJobId(), matchScore, skillScore, timeScore, distanceScore, distanceKm);

            results.add(MatchRecommendResponse.builder()
                    .jobId(job.getJobId())
                    .jobTitle(job.getJobTitle())
                    .jobType(job.getJobType())
                    .industryTag(job.getIndustryTag())
                    .salaryType(job.getSalaryType())
                    .salaryAmount(job.getSalaryAmount())
                    .settlementType(job.getSettlementType())
                    .workAddress(job.getWorkAddress())
                    .longitude(job.getLongitude())
                    .latitude(job.getLatitude())
                    .status(job.getStatus())
                    .isInsured(job.getIsInsured())
                    .viewCount(job.getViewCount())
                    .enterpriseName(job.getEnterpriseName())
                    .enterpriseCreditScore(job.getEnterpriseCreditScore())
                    .matchScore(matchScore)
                    .skillScore(skillScore)
                    .timeScore(timeScore)
                    .distanceScore(distanceScore)
                    .distanceKm(distanceKm)
                    .build());
        }

        results.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));

        int limit = request.getLimit() != null ? request.getLimit() : maxRecommend;
        if (results.size() > limit) {
            results = results.subList(0, limit);
        }

        log.info("为学生推荐岗位完成: studentId={}, count={}", request.getStudentId(), results.size());
        return results;
    }

    @Override
    @Transactional
    public List<MatchCandidateResponse> matchCandidates(MatchCandidateRequest request) {
        JobInfo job = getJobInfo(request.getJobId());

        List<StudentInfo> students = searchStudentsByLocation(
                job.getLatitude().doubleValue(), job.getLongitude().doubleValue(), searchRadius);

        List<MatchCandidateResponse> results = new ArrayList<>();
        for (StudentInfo student : students) {
            int skillScore = calculateSkillScore(student.getSkillTags(), job.getSkillRequire());
            int timeScore = calculateTimeScore(student.getAvailableTime(), job.getWorkTime());
            double distanceKm = calculateDistance(
                    student.getLatitude().doubleValue(), student.getLongitude().doubleValue(),
                    job.getLatitude().doubleValue(), job.getLongitude().doubleValue()
            );
            int distanceScore = calculateDistanceScore(distanceKm);

            int matchScore = (int) (skillScore * skillWeight + timeScore * timeWeight + distanceScore * distanceWeight);

            saveMatchLog(student.getStudentId(), request.getJobId(), matchScore, skillScore, timeScore, distanceScore, distanceKm);

            results.add(MatchCandidateResponse.builder()
                    .studentId(student.getStudentId())
                    .realName(student.getRealName())
                    .avatarUrl(student.getAvatarUrl())
                    .skillTags(student.getSkillTags())
                    .availableTime(student.getAvailableTime())
                    .creditScore(student.getCreditScore())
                    .matchScore(matchScore)
                    .skillScore(skillScore)
                    .timeScore(timeScore)
                    .distanceScore(distanceScore)
                    .distanceKm(distanceKm)
                    .build());
        }

        results.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));

        int limit = request.getLimit() != null ? request.getLimit() : maxRecommend;
        if (results.size() > limit) {
            results = results.subList(0, limit);
        }

        log.info("为岗位推荐候选人完成: jobId={}, count={}", request.getJobId(), results.size());
        return results;
    }

    private int calculateSkillScore(String studentSkillTags, String jobSkillRequire) {
        if (studentSkillTags == null || studentSkillTags.isEmpty() ||
                jobSkillRequire == null || jobSkillRequire.isEmpty()) {
            return 10;
        }

        Set<String> studentSkills = parseSkillTags(studentSkillTags);
        Set<String> jobSkills = parseSkillTags(jobSkillRequire);

        if (jobSkills.isEmpty()) {
            return 10;
        }

        int matches = 0;
        for (String skill : jobSkills) {
            if (studentSkills.contains(skill)) {
                matches++;
            }
        }

        if (matches == jobSkills.size()) {
            return 40;
        } else if (matches > 0) {
            return (int) (40.0 * matches / jobSkills.size());
        } else {
            return 10;
        }
    }

    private Set<String> parseSkillTags(String skillTags) {
        try {
            JSONArray array = JSON.parseArray(skillTags);
            Set<String> skills = new HashSet<>();
            for (int i = 0; i < array.size(); i++) {
                skills.add(array.getString(i).toLowerCase().trim());
            }
            return skills;
        } catch (Exception e) {
            Set<String> skills = new HashSet<>();
            String[] parts = skillTags.split("[,，;；\\s]+");
            for (String part : parts) {
                String trimmed = part.trim().toLowerCase();
                if (!trimmed.isEmpty()) {
                    skills.add(trimmed);
                }
            }
            return skills;
        }
    }

    private int calculateTimeScore(String studentAvailableTime, String jobWorkTime) {
        if (studentAvailableTime == null || studentAvailableTime.isEmpty() ||
                jobWorkTime == null || jobWorkTime.isEmpty()) {
            return 0;
        }

        Set<String> studentTimeSlots = parseTimeSlots(studentAvailableTime);
        Set<String> jobTimeSlots = parseTimeSlots(jobWorkTime);

        if (jobTimeSlots.isEmpty()) {
            return 0;
        }

        int matches = 0;
        for (String slot : jobTimeSlots) {
            if (studentTimeSlots.contains(slot)) {
                matches++;
            }
        }

        double coverage = (double) matches / jobTimeSlots.size();

        if (coverage >= 0.8) {
            return 30;
        } else if (coverage >= 0.5) {
            return 20;
        } else if (coverage > 0) {
            return 10;
        } else {
            return 0;
        }
    }

    private Set<String> parseTimeSlots(String timeJson) {
        Set<String> slots = new HashSet<>();
        try {
            JSONArray array = JSON.parseArray(timeJson);
            for (int i = 0; i < array.size(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String dayOfWeek = obj.getString("dayOfWeek");
                String startTime = obj.getString("startTime");
                String endTime = obj.getString("endTime");
                slots.add(dayOfWeek + "_" + startTime + "_" + endTime);
            }
        } catch (Exception e) {
            log.warn("解析时间JSON失败: {}", timeJson);
        }
        return slots;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    private int calculateDistanceScore(double distanceKm) {
        if (distanceKm <= 1) {
            return 30;
        } else if (distanceKm <= 3) {
            return 25;
        } else if (distanceKm <= 5) {
            return 20;
        } else if (distanceKm <= 10) {
            return 10;
        } else {
            return 5;
        }
    }

    private List<JobInfo> searchJobsByLocation(double longitude, double latitude, int distanceKm) {
        List<JobInfo> jobs = new ArrayList<>();
        try {
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.filter(QueryBuilders.termQuery("status", 1));

            GeoDistanceQueryBuilder geoQuery = QueryBuilders.geoDistanceQuery("location")
                    .point(latitude, longitude)
                    .distance(distanceKm, org.elasticsearch.common.unit.DistanceUnit.KILOMETERS);
            boolQuery.filter(geoQuery);

            sourceBuilder.query(boolQuery);
            sourceBuilder.sort(new FieldSortBuilder("create_time").order(SortOrder.DESC));
            sourceBuilder.size(100);

            SearchRequest searchRequest = new SearchRequest(JOB_INDEX_NAME);
            searchRequest.source(sourceBuilder);

            SearchResponse response = esClient.search(searchRequest, RequestOptions.DEFAULT);

            for (SearchHit hit : response.getHits().getHits()) {
                JSONObject source = JSON.parseObject(hit.getSourceAsString());
                jobs.add(JobInfo.builder()
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
                        .workTime(source.getString("work_time"))
                        .skillRequire(source.getString("skill_require"))
                        .status(source.getIntValue("status"))
                        .isInsured(source.getIntValue("is_insured"))
                        .viewCount(source.getIntValue("view_count"))
                        .enterpriseName(source.getString("enterprise_name"))
                        .enterpriseCreditScore(source.getIntValue("enterprise_credit_score"))
                        .build());
            }
        } catch (Exception e) {
            log.error("ES搜索岗位失败: {}", e.getMessage());
            jobs = fallbackSearchJobsByLocation(latitude, longitude);
        }
        return jobs;
    }

    private List<JobInfo> fallbackSearchJobsByLocation(double latitude, double longitude) {
        return new ArrayList<>();
    }

    private List<StudentInfo> searchStudentsByLocation(double lat, double lon, int distanceKm) {
        List<StudentInfo> students = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            double randomLat = lat + (Math.random() - 0.5) * 0.1;
            double randomLon = lon + (Math.random() - 0.5) * 0.1;
            double distance = calculateDistance(lat, lon, randomLat, randomLon);

            if (distance <= distanceKm) {
                students.add(StudentInfo.builder()
                        .studentId(UUID.randomUUID().toString())
                        .realName("测试学生" + (i + 1))
                        .avatarUrl("")
                        .skillTags("[\"" + mockSkillTags() + "\"]")
                        .availableTime(mockAvailableTime())
                        .creditScore(80 + (int) (Math.random() * 40))
                        .longitude(BigDecimal.valueOf(randomLon))
                        .latitude(BigDecimal.valueOf(randomLat))
                        .build());
            }
        }

        return students;
    }

    private StudentInfo getStudentInfo(String studentId) {
        return StudentInfo.builder()
                .studentId(studentId)
                .realName("测试学生")
                .avatarUrl("")
                .skillTags("[\"Java\", \"Python\", \"前端开发\"]")
                .availableTime("[{\"dayOfWeek\":\"周一\",\"startTime\":\"09:00\",\"endTime\":\"18:00\"},{\"dayOfWeek\":\"周二\",\"startTime\":\"09:00\",\"endTime\":\"18:00\"},{\"dayOfWeek\":\"周五\",\"startTime\":\"09:00\",\"endTime\":\"18:00\"}]")
                .creditScore(100)
                .build();
    }

    private JobInfo getJobInfo(String jobId) {
        return JobInfo.builder()
                .jobId(jobId)
                .jobTitle("测试岗位")
                .jobType("全职")
                .industryTag("互联网")
                .salaryType(1)
                .salaryAmount(BigDecimal.valueOf(25))
                .settlementType(1)
                .workAddress("长沙市高新区")
                .longitude(BigDecimal.valueOf(112.9388))
                .latitude(BigDecimal.valueOf(28.2282))
                .workTime("[{\"dayOfWeek\":\"周一\",\"startTime\":\"09:00\",\"endTime\":\"18:00\"},{\"dayOfWeek\":\"周二\",\"startTime\":\"09:00\",\"endTime\":\"18:00\"}]")
                .skillRequire("[\"Java\", \"Spring Boot\"]")
                .status(1)
                .isInsured(1)
                .viewCount(100)
                .enterpriseName("测试企业")
                .enterpriseCreditScore(100)
                .build();
    }

    private double[] mockStudentLocation(String studentId) {
        return new double[]{112.9388 + (Math.random() - 0.5) * 0.1, 28.2282 + (Math.random() - 0.5) * 0.1};
    }

    private String mockSkillTags() {
        String[] skills = {"Java", "Python", "前端", "后端", "Android", "iOS", "测试", "运维"};
        int count = 2 + (int) (Math.random() * 3);
        List<String> selected = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            selected.add(skills[random.nextInt(skills.length)]);
        }
        return String.join("\", \"", selected);
    }

    private String mockAvailableTime() {
        String[] days = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        int count = 3 + (int) (Math.random() * 3);
        List<String> slots = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            String day = days[random.nextInt(days.length)];
            slots.add("{\"dayOfWeek\":\"" + day + "\",\"startTime\":\"09:00\",\"endTime\":\"18:00\"}");
        }
        return "[" + String.join(",", slots) + "]";
    }

    private void saveMatchLog(String studentId, String jobId, int matchScore, int skillScore,
                              int timeScore, int distanceScore, double distanceKm) {
        JobMatchLogEntity logEntity = JobMatchLogEntity.builder()
                .studentId(studentId)
                .jobId(jobId)
                .matchScore(matchScore)
                .skillScore(skillScore)
                .timeScore(timeScore)
                .distanceScore(distanceScore)
                .distanceKm(distanceKm)
                .createdAt(LocalDateTime.now())
                .build();
        matchLogMapper.insert(logEntity);
    }
}