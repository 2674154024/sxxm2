package com.parttime.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parttime.user.entity.StudentEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper extends BaseMapper<StudentEntity> {
}