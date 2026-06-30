package com.parttime.complaint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parttime.complaint.entity.PracticeReportEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PracticeReportMapper extends BaseMapper<PracticeReportEntity> {

    @Select("SELECT * FROM t_practice_report WHERE student_id = #{studentId} AND job_id = #{jobId}")
    PracticeReportEntity selectByUniqueKey(@Param("studentId") String studentId, @Param("jobId") String jobId);

    @Update("UPDATE t_practice_report SET enterprise_comment = #{comment}, enterprise_score = #{score}, updated_at = NOW() WHERE student_id = #{studentId} AND job_id = #{jobId}")
    void updateEnterpriseComment(@Param("studentId") String studentId, @Param("jobId") String jobId,
                                  @Param("comment") String comment, @Param("score") Integer score);

    @Update("UPDATE t_practice_report SET student_comment = #{comment}, student_score = #{score}, updated_at = NOW() WHERE student_id = #{studentId} AND job_id = #{jobId}")
    void updateStudentComment(@Param("studentId") String studentId, @Param("jobId") String jobId,
                               @Param("comment") String comment, @Param("score") Integer score);
}