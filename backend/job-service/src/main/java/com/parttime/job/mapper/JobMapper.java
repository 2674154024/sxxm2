package com.parttime.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parttime.job.entity.JobEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface JobMapper extends BaseMapper<JobEntity> {

    @Update("UPDATE t_job SET view_count = view_count + #{count} WHERE id = #{jobId}")
    int updateViewCount(@Param("jobId") String jobId, @Param("count") int count);

    @Update("UPDATE t_job SET apply_count = apply_count + 1 WHERE id = #{jobId}")
    int incrementApplyCount(@Param("jobId") String jobId);
}