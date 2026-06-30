package com.parttime.job.task;

import com.parttime.job.mapper.JobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountSyncTask {

    private final StringRedisTemplate redisTemplate;
    private final JobMapper jobMapper;

    private static final String VIEW_COUNT_PREFIX = "job:view:";

    @Scheduled(cron = "0 */10 * * * ?")
    public void syncViewCount() {
        log.info("开始执行浏览量同步任务");

        try {
            Set<String> keys = redisTemplate.keys(VIEW_COUNT_PREFIX + "*");

            if (keys == null || keys.isEmpty()) {
                log.info("没有需要同步的浏览量数据");
                return;
            }

            int count = 0;
            for (String key : keys) {
                String jobId = key.substring(VIEW_COUNT_PREFIX.length());
                String viewCountStr = redisTemplate.opsForValue().get(key);

                if (viewCountStr != null) {
                    int viewCount = Integer.parseInt(viewCountStr);
                    jobMapper.updateViewCount(jobId, viewCount);
                    redisTemplate.delete(key);
                    count++;
                }
            }

            log.info("浏览量同步任务完成，同步{}条记录", count);

        } catch (Exception e) {
            log.error("浏览量同步失败: {}", e.getMessage());
        }
    }
}