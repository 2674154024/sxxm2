package com.parttime.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parttime.task.entity.AttendanceRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecordEntity> {

    @Select("SELECT * FROM t_attendance_record WHERE student_id = #{studentId} AND job_id = #{jobId} AND work_date = #{workDate}")
    AttendanceRecordEntity selectByUniqueKey(@Param("studentId") String studentId,
                                              @Param("jobId") String jobId,
                                              @Param("workDate") LocalDate workDate);

    @Select("SELECT * FROM t_attendance_record WHERE job_id = #{jobId} ORDER BY work_date DESC")
    Page<AttendanceRecordEntity> selectByJobId(Page<AttendanceRecordEntity> page, @Param("jobId") String jobId);

    @Select("SELECT * FROM t_attendance_record WHERE job_id = #{jobId} AND work_date = #{workDate}")
    List<AttendanceRecordEntity> selectByJobIdAndDate(@Param("jobId") String jobId, @Param("workDate") LocalDate workDate);

    @Select("SELECT * FROM t_attendance_record WHERE settlement_status = 0 ORDER BY work_date ASC")
    Page<AttendanceRecordEntity> selectPendingRecords(Page<AttendanceRecordEntity> page);

    @Select("SELECT * FROM t_attendance_record WHERE student_id = #{studentId} ORDER BY work_date DESC")
    Page<AttendanceRecordEntity> selectByStudentId(Page<AttendanceRecordEntity> page, @Param("studentId") String studentId);
}