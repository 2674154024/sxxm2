package com.parttime.task.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parttime.common.exception.BusinessException;
import com.parttime.task.config.ClockProperties;
import com.parttime.task.dto.response.AttendanceRecordResponse;
import com.parttime.task.dto.response.ClockRecordResponse;
import com.parttime.task.entity.AttendanceRecordEntity;
import com.parttime.task.entity.ClockAppealEntity;
import com.parttime.task.entity.ClockRecordEntity;
import com.parttime.task.mapper.AttendanceRecordMapper;
import com.parttime.task.mapper.ClockAppealMapper;
import com.parttime.task.mapper.ClockRecordMapper;
import com.parttime.task.service.ClockService;
import com.parttime.task.util.GeoUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClockServiceImpl implements ClockService {

    @Autowired
    private ClockRecordMapper clockRecordMapper;

    @Autowired
    private AttendanceRecordMapper attendanceRecordMapper;

    @Autowired
    private ClockAppealMapper clockAppealMapper;

    @Autowired
    private ClockProperties clockProperties;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public ClockRecordResponse clock(String studentId, String jobId, Integer clockType, Double longitude, Double latitude) {
        LocalDate today = LocalDate.now();

        ClockRecordEntity existingRecord = clockRecordMapper.selectTodayRecord(studentId, jobId, today, clockType);
        if (existingRecord != null) {
            throw new BusinessException(409, "今天已完成该类型打卡");
        }

        BigDecimal distance = BigDecimal.ZERO;
        int isAbnormal = 0;

        BigDecimal jobLongitude = getJobLongitude(jobId);
        BigDecimal jobLatitude = getJobLatitude(jobId);

        if (jobLongitude != null && jobLatitude != null) {
            distance = GeoUtil.calculateDistance(latitude, longitude, jobLatitude.doubleValue(), jobLongitude.doubleValue());
            if (distance.compareTo(BigDecimal.valueOf(clockProperties.getMaxDistance())) > 0) {
                isAbnormal = 1;
            }
        }

        ClockRecordEntity record = ClockRecordEntity.builder()
                .id(UUID.randomUUID().toString())
                .studentId(studentId)
                .jobId(jobId)
                .clockType(clockType)
                .longitude(BigDecimal.valueOf(longitude))
                .latitude(BigDecimal.valueOf(latitude))
                .distance(distance)
                .isAbnormal(isAbnormal)
                .clockTime(LocalDateTime.now())
                .settlementStatus(0)
                .createdAt(LocalDateTime.now())
                .build();

        if (clockType == 2) {
            ClockRecordEntity lastClockIn = clockRecordMapper.selectLastClockIn(studentId, jobId);
            if (lastClockIn == null) {
                throw new BusinessException("请先签到");
            }

            BigDecimal workHours = calculateWorkHours(lastClockIn.getClockTime(), record.getClockTime());
            record.setWorkHours(workHours);
        }

        clockRecordMapper.insert(record);

        if (clockType == 2) {
            AttendanceRecordEntity attendance = attendanceRecordMapper.selectByUniqueKey(studentId, jobId, today);
            if (attendance == null) {
                attendance = AttendanceRecordEntity.builder()
                        .id(UUID.randomUUID().toString())
                        .studentId(studentId)
                        .jobId(jobId)
                        .workDate(today)
                        .clockInTime(getClockInTime(studentId, jobId, today))
                        .clockOutTime(record.getClockTime())
                        .workHours(record.getWorkHours())
                        .isAbnormal(record.getIsAbnormal())
                        .settlementStatus(0)
                        .createdAt(LocalDateTime.now())
                        .build();
                attendanceRecordMapper.insert(attendance);
            } else {
                attendance.setClockOutTime(record.getClockTime());
                attendance.setWorkHours(record.getWorkHours());
                attendance.setIsAbnormal(record.getIsAbnormal());
                attendance.setUpdatedAt(LocalDateTime.now());
                attendanceRecordMapper.updateById(attendance);
            }
        }

        return convertToClockRecordResponse(record);
    }

    @Override
    public List<ClockRecordResponse> getClockRecords(String studentId, String jobId, String date, Integer page, Integer size) {
        Page<ClockRecordEntity> pageParam = new Page<>(page, size);
        Page<ClockRecordEntity> result;

        if (jobId != null && date != null) {
            throw new BusinessException("不能同时指定岗位和日期");
        } else if (jobId != null) {
            result = clockRecordMapper.selectByStudentIdAndJobId(pageParam, studentId, jobId);
        } else if (date != null) {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            result = clockRecordMapper.selectByStudentIdAndDate(pageParam, studentId, localDate);
        } else {
            result = clockRecordMapper.selectByStudentId(pageParam, studentId);
        }

        return result.getRecords().stream()
                .map(this::convertToClockRecordResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceRecordResponse> getAttendanceList(String jobId, Integer page, Integer size) {
        Page<AttendanceRecordEntity> pageParam = new Page<>(page, size);
        Page<AttendanceRecordEntity> result = attendanceRecordMapper.selectByJobId(pageParam, jobId);

        return result.getRecords().stream()
                .map(this::convertToAttendanceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttendanceRecordResponse confirmAttendance(String studentId, String jobId, String workDate,
                                                      BigDecimal confirmedHours, String note) {
        LocalDate localDate = LocalDate.parse(workDate, DateTimeFormatter.ISO_LOCAL_DATE);

        AttendanceRecordEntity attendance = attendanceRecordMapper.selectByUniqueKey(studentId, jobId, localDate);
        if (attendance == null) {
            throw new BusinessException("考勤记录不存在");
        }

        attendance.setConfirmedHours(confirmedHours);
        attendance.setNote(note);
        attendance.setSettlementStatus(1);
        attendance.setConfirmedAt(LocalDateTime.now());
        attendance.setUpdatedAt(LocalDateTime.now());

        attendanceRecordMapper.updateById(attendance);

        return convertToAttendanceResponse(attendance);
    }

    @Override
    @Transactional
    public void appealClock(String recordId, String studentId, String appealReason, String evidenceUrls) {
        ClockRecordEntity record = clockRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("打卡记录不存在");
        }

        if (!record.getStudentId().equals(studentId)) {
            throw new BusinessException("无权申诉此记录");
        }

        if (record.getIsAbnormal() != 1) {
            throw new BusinessException("只有异常打卡才能申诉");
        }

        ClockAppealEntity existingAppeal = clockAppealMapper.selectByRecordId(recordId);
        if (existingAppeal != null && existingAppeal.getAppealStatus() == 0) {
            throw new BusinessException("已提交申诉，等待审核");
        }

        ClockAppealEntity appeal = ClockAppealEntity.builder()
                .id(UUID.randomUUID().toString())
                .recordId(recordId)
                .studentId(studentId)
                .jobId(record.getJobId())
                .appealReason(appealReason)
                .evidenceUrls(evidenceUrls)
                .appealStatus(0)
                .createdAt(LocalDateTime.now())
                .build();

        clockAppealMapper.insert(appeal);
    }

    @Override
    @Transactional
    public void calculateWorkHours(LocalDate date) {
        List<String> jobIds = clockRecordMapper.selectJobIdsForDate(date);

        for (String jobId : jobIds) {
            List<ClockRecordEntity> records = clockRecordMapper.selectCompletedRecordsForDate(jobId, date);

            for (ClockRecordEntity record : records) {
                if (record.getWorkHours() == null) {
                    ClockRecordEntity lastClockIn = clockRecordMapper.selectLastClockIn(record.getStudentId(), jobId);
                    if (lastClockIn != null) {
                        BigDecimal workHours = calculateWorkHours(lastClockIn.getClockTime(), record.getClockTime());
                        record.setWorkHours(workHours);
                        clockRecordMapper.updateById(record);

                        AttendanceRecordEntity attendance = attendanceRecordMapper.selectByUniqueKey(
                                record.getStudentId(), jobId, date);
                        if (attendance != null) {
                            attendance.setWorkHours(workHours);
                            attendance.setUpdatedAt(LocalDateTime.now());
                            attendanceRecordMapper.updateById(attendance);
                        }
                    }
                }
            }
        }
    }

    private BigDecimal calculateWorkHours(LocalDateTime start, LocalDateTime end) {
        long minutes = java.time.Duration.between(start, end).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
        return GeoUtil.roundToHalf(hours);
    }

    private BigDecimal getJobLongitude(String jobId) {
        return BigDecimal.valueOf(112.9399);
    }

    private BigDecimal getJobLatitude(String jobId) {
        return BigDecimal.valueOf(28.2280);
    }

    private LocalDateTime getClockInTime(String studentId, String jobId, LocalDate date) {
        ClockRecordEntity record = clockRecordMapper.selectTodayRecord(studentId, jobId, date, 1);
        return record != null ? record.getClockTime() : null;
    }

    private ClockRecordResponse convertToClockRecordResponse(ClockRecordEntity entity) {
        return ClockRecordResponse.builder()
                .id(entity.getId())
                .jobId(entity.getJobId())
                .clockType(entity.getClockType())
                .longitude(entity.getLongitude())
                .latitude(entity.getLatitude())
                .distance(entity.getDistance())
                .isAbnormal(entity.getIsAbnormal())
                .clockTime(entity.getClockTime())
                .workHours(entity.getWorkHours())
                .settlementStatus(entity.getSettlementStatus())
                .build();
    }

    private AttendanceRecordResponse convertToAttendanceResponse(AttendanceRecordEntity entity) {
        return AttendanceRecordResponse.builder()
                .id(entity.getId())
                .studentId(entity.getStudentId())
                .jobId(entity.getJobId())
                .workDate(entity.getWorkDate())
                .clockInTime(entity.getClockInTime())
                .clockOutTime(entity.getClockOutTime())
                .workHours(entity.getWorkHours())
                .confirmedHours(entity.getConfirmedHours())
                .isAbnormal(entity.getIsAbnormal())
                .settlementStatus(entity.getSettlementStatus())
                .note(entity.getNote())
                .build();
    }
}