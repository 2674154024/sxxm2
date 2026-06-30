package com.parttime.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parttime.common.entity.JobEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JobMapper extends BaseMapper<JobEntity> {
}