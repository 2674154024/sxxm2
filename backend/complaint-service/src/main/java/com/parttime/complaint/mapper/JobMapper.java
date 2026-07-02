package com.parttime.complaint.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface JobMapper {

    @Select("SELECT enterprise_id FROM t_job WHERE id = #{jobId}")
    String getEnterpriseIdByJobId(@Param("jobId") String jobId);

    @Select("SELECT job_title FROM t_job WHERE id = #{jobId}")
    String getJobTitleByJobId(@Param("jobId") String jobId);
}
