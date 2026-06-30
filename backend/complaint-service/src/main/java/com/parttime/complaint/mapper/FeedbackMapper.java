package com.parttime.complaint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parttime.complaint.entity.FeedbackEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FeedbackMapper extends BaseMapper<FeedbackEntity> {

    @Select("SELECT * FROM t_feedback WHERE from_id = #{fromId} AND to_id = #{toId} AND job_id = #{jobId}")
    FeedbackEntity selectByUniqueKey(@Param("fromId") String fromId, @Param("toId") String toId, @Param("jobId") String jobId);
}