package com.parttime.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parttime.common.entity.JobApplyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface JobApplyMapper extends BaseMapper<JobApplyEntity> {

    @Select("SELECT COUNT(*) FROM t_job_apply WHERE student_id = #{studentId} AND job_id = #{jobId}")
    int countByStudentAndJob(@Param("studentId") String studentId, @Param("jobId") String jobId);
}
