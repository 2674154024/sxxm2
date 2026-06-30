package com.parttime.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parttime.task.entity.ClockAppealEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ClockAppealMapper extends BaseMapper<ClockAppealEntity> {

    @Select("SELECT * FROM t_clock_appeal WHERE record_id = #{recordId}")
    ClockAppealEntity selectByRecordId(@Param("recordId") String recordId);
}