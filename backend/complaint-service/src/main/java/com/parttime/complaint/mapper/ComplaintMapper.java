package com.parttime.complaint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parttime.complaint.entity.ComplaintEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ComplaintMapper extends BaseMapper<ComplaintEntity> {

    @Select("SELECT * FROM t_complaint WHERE student_id = #{studentId} ORDER BY created_at DESC")
    Page<ComplaintEntity> selectByStudentId(Page<ComplaintEntity> page, @Param("studentId") String studentId);

    @Select("SELECT * FROM t_complaint ORDER BY created_at DESC")
    Page<ComplaintEntity> selectAll(Page<ComplaintEntity> page);

    @Select("SELECT * FROM t_complaint WHERE status = #{status} ORDER BY created_at ASC")
    Page<ComplaintEntity> selectByStatus(Page<ComplaintEntity> page, @Param("status") Integer status);
}