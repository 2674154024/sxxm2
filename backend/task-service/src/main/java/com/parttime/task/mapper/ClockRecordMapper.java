package com.parttime.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parttime.task.entity.ClockRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ClockRecordMapper extends BaseMapper<ClockRecordEntity> {

    @Select("SELECT * FROM t_clock_record WHERE student_id = #{studentId} AND job_id = #{jobId} " +
            "AND DATE(clock_time) = #{date} AND clock_type = #{clockType} LIMIT 1")
    ClockRecordEntity selectTodayRecord(@Param("studentId") String studentId, @Param("jobId") String jobId,
                                         @Param("date") LocalDate date, @Param("clockType") Integer clockType);

    @Select("SELECT * FROM t_clock_record WHERE student_id = #{studentId} ORDER BY clock_time DESC")
    Page<ClockRecordEntity> selectByStudentId(Page<ClockRecordEntity> page, @Param("studentId") String studentId);

    @Select("SELECT * FROM t_clock_record WHERE student_id = #{studentId} AND job_id = #{jobId} ORDER BY clock_time DESC")
    Page<ClockRecordEntity> selectByStudentIdAndJobId(Page<ClockRecordEntity> page,
                                                       @Param("studentId") String studentId,
                                                       @Param("jobId") String jobId);

    @Select("SELECT * FROM t_clock_record WHERE student_id = #{studentId} AND DATE(clock_time) = #{date} ORDER BY clock_time DESC")
    Page<ClockRecordEntity> selectByStudentIdAndDate(Page<ClockRecordEntity> page,
                                                      @Param("studentId") String studentId,
                                                      @Param("date") LocalDate date);

    @Select("SELECT * FROM t_clock_record WHERE student_id = #{studentId} AND job_id = #{jobId} AND clock_type = 1 ORDER BY clock_time DESC LIMIT 1")
    ClockRecordEntity selectLastClockIn(@Param("studentId") String studentId, @Param("jobId") String jobId);

    @Select("SELECT * FROM t_clock_record WHERE job_id = #{jobId} AND clock_type = 2 AND DATE(clock_time) = #{date} AND work_hours IS NOT NULL")
    List<ClockRecordEntity> selectCompletedRecordsForDate(@Param("jobId") String jobId, @Param("date") LocalDate date);

    @Select("SELECT DISTINCT job_id FROM t_clock_record WHERE DATE(clock_time) = #{date} AND clock_type = 2 AND work_hours IS NOT NULL")
    List<String> selectJobIdsForDate(@Param("date") LocalDate date);
}