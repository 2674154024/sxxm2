package com.parttime.task.task;

import com.parttime.task.service.ClockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class WorkHourCalculateTask {

    private static final Logger logger = LoggerFactory.getLogger(WorkHourCalculateTask.class);

    @Autowired
    private ClockService clockService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void calculateWorkHoursDaily() {
        logger.info("开始执行每日工时计算任务");
        LocalDate yesterday = LocalDate.now().minusDays(1);
        try {
            clockService.calculateWorkHours(yesterday);
            logger.info("每日工时计算任务执行完成，日期：{}", yesterday);
        } catch (Exception e) {
            logger.error("每日工时计算任务执行失败", e);
        }
    }
}