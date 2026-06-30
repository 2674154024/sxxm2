package com.parttime.task.service;

import com.parttime.task.dto.response.ClockRecordResponse;
import com.parttime.task.dto.response.AttendanceRecordResponse;

import java.util.List;

public interface ClockService {

    ClockRecordResponse clock(String studentId, String jobId, Integer clockType, Double longitude, Double latitude);

    List<ClockRecordResponse> getClockRecords(String studentId, String jobId, String date, Integer page, Integer size);

    List<AttendanceRecordResponse> getAttendanceList(String jobId, Integer page, Integer size);

    AttendanceRecordResponse confirmAttendance(String studentId, String jobId, String workDate,
                                               java.math.BigDecimal confirmedHours, String note);

    void appealClock(String recordId, String studentId, String appealReason, String evidenceUrls);

    void calculateWorkHours(java.time.LocalDate date);
}