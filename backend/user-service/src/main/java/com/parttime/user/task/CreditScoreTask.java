package com.parttime.user.task;

import com.parttime.user.entity.EnterpriseEntity;
import com.parttime.user.entity.StudentEntity;
import com.parttime.user.mapper.EnterpriseMapper;
import com.parttime.user.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditScoreTask {

    private final StudentMapper studentMapper;
    private final EnterpriseMapper enterpriseMapper;

    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 200;
    private static final int REWARD_FULFILLMENT = 5;
    private static final int PENALTY_COMPLAINT = 20;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void updateStudentCreditScore() {
        log.info("开始执行学生信用评分定时任务");

        List<StudentEntity> students = studentMapper.selectList(null);

        for (StudentEntity student : students) {
            int newScore = calculateStudentScore(student);
            if (newScore != student.getCreditScore()) {
                student.setCreditScore(newScore);
                studentMapper.updateById(student);
                log.info("学生信用分更新: userId={}, score={}", student.getId(), newScore);
            }
        }

        log.info("学生信用评分定时任务执行完成");
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void updateEnterpriseCreditScore() {
        log.info("开始执行企业信用评分定时任务");

        List<EnterpriseEntity> enterprises = enterpriseMapper.selectList(null);

        for (EnterpriseEntity enterprise : enterprises) {
            int newScore = calculateEnterpriseScore(enterprise);
            if (newScore != enterprise.getCreditScore()) {
                enterprise.setCreditScore(newScore);
                enterpriseMapper.updateById(enterprise);
                log.info("企业信用分更新: enterpriseId={}, score={}", enterprise.getId(), newScore);
            }
        }

        log.info("企业信用评分定时任务执行完成");
    }

    private int calculateStudentScore(StudentEntity student) {
        int currentScore = student.getCreditScore() != null ? student.getCreditScore() : 100;
        int fulfillmentCount = mockGetFulfillmentCount(student.getId());
        int complaintCount = mockGetComplaintCount(student.getId());

        int newScore = currentScore + fulfillmentCount * REWARD_FULFILLMENT - complaintCount * PENALTY_COMPLAINT;

        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, newScore));
    }

    private int calculateEnterpriseScore(EnterpriseEntity enterprise) {
        int currentScore = enterprise.getCreditScore() != null ? enterprise.getCreditScore() : 100;
        int onTimeSalaryCount = mockGetOnTimeSalaryCount(enterprise.getId());
        int complaintCount = mockGetComplaintCount(enterprise.getId());

        int newScore = currentScore + onTimeSalaryCount * REWARD_FULFILLMENT - complaintCount * PENALTY_COMPLAINT;

        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, newScore));
    }

    private int mockGetFulfillmentCount(String userId) {
        return 0;
    }

    private int mockGetComplaintCount(String userId) {
        return 0;
    }

    private int mockGetOnTimeSalaryCount(String enterpriseId) {
        return 0;
    }
}