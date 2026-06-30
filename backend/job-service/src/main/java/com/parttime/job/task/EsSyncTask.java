package com.parttime.job.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parttime.job.entity.JobEntity;
import com.parttime.job.mapper.JobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EsSyncTask {

    private final JobMapper jobMapper;
    private final RestHighLevelClient esClient;

    private static final String INDEX_NAME = "job";

    @Scheduled(cron = "0 */5 * * * ?")
    public void incrementalSync() {
        log.info("开始执行ES增量同步任务");

        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

        LambdaQueryWrapper<JobEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobEntity::getStatus, 1);
        wrapper.gt(JobEntity::getUpdatedAt, fiveMinutesAgo);

        List<JobEntity> jobs = jobMapper.selectList(wrapper);

        for (JobEntity job : jobs) {
            indexToEs(job);
        }

        log.info("ES增量同步任务完成，同步{}条记录", jobs.size());
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void fullRebuild() {
        log.info("开始执行ES全量重建任务");

        try {
            GetIndexRequest getIndexRequest = new GetIndexRequest(INDEX_NAME);
            boolean exists = esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);

            if (exists) {
                DeleteIndexRequest deleteRequest = new DeleteIndexRequest(INDEX_NAME);
                AcknowledgedResponse deleteResponse = esClient.indices().delete(deleteRequest, RequestOptions.DEFAULT);
                log.info("删除旧索引: {}", deleteResponse.isAcknowledged());
            }

            CreateIndexRequest createRequest = new CreateIndexRequest(INDEX_NAME);
            createRequest.mapping(getMapping(), XContentType.JSON);
            CreateIndexResponse createResponse = esClient.indices().create(createRequest, RequestOptions.DEFAULT);
            log.info("创建新索引: {}", createResponse.isAcknowledged());

            LambdaQueryWrapper<JobEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(JobEntity::getStatus, 1);
            List<JobEntity> jobs = jobMapper.selectList(wrapper);

            int count = 0;
            for (JobEntity job : jobs) {
                indexToEs(job);
                count++;
                if (count % 100 == 0) {
                    log.info("全量重建进度: {}/{}", count, jobs.size());
                }
            }

            log.info("ES全量重建任务完成，重建{}条记录", count);

        } catch (Exception e) {
            log.error("ES全量重建失败: {}", e.getMessage());
        }
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

            IndexRequest request = new IndexRequest(INDEX_NAME)
                    .id(job.getId())
                    .source(doc.toJSONString(), XContentType.JSON);

            esClient.index(request, RequestOptions.DEFAULT);

        } catch (Exception e) {
            log.error("索引岗位到ES失败: jobId={}, error={}", job.getId(), e.getMessage());
        }
    }

    private String getMapping() {
        return JSON.toJSONString(new JSONObject() {{
            put("mappings", new JSONObject() {{
                put("properties", new JSONObject() {{
                    put("job_id", new JSONObject() {{ put("type", "keyword"); }});
                    put("job_title", new JSONObject() {{
                        put("type", "text");
                        put("analyzer", "ik_max_word");
                    }});
                    put("job_type", new JSONObject() {{ put("type", "keyword"); }});
                    put("industry_tag", new JSONObject() {{ put("type", "keyword"); }});
                    put("salary_type", new JSONObject() {{ put("type", "integer"); }});
                    put("salary_amount", new JSONObject() {{ put("type", "double"); }});
                    put("settlement_type", new JSONObject() {{ put("type", "integer"); }});
                    put("work_address", new JSONObject() {{ put("type", "text"); }});
                    put("longitude", new JSONObject() {{ put("type", "double"); }});
                    put("latitude", new JSONObject() {{ put("type", "double"); }});
                    put("location", new JSONObject() {{ put("type", "geo_point"); }});
                    put("work_time", new JSONObject() {{ put("type", "nested"); }});
                    put("skill_require", new JSONObject() {{
                        put("type", "text");
                        put("analyzer", "ik_max_word");
                    }});
                    put("status", new JSONObject() {{ put("type", "integer"); }});
                    put("enterprise_name", new JSONObject() {{ put("type", "keyword"); }});
                    put("enterprise_credit_score", new JSONObject() {{ put("type", "integer"); }});
                    put("is_insured", new JSONObject() {{ put("type", "integer"); }});
                    put("view_count", new JSONObject() {{ put("type", "integer"); }});
                    put("create_time", new JSONObject() {{ put("type", "date"); }});
                }});
            }});
        }});
    }

    private String mockGetEnterpriseName(String enterpriseId) {
        return "测试企业";
    }

    private Integer mockGetEnterpriseCreditScore(String enterpriseId) {
        return 100;
    }
}