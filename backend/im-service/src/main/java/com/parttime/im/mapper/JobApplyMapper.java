package com.parttime.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parttime.im.entity.JobApplyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface JobApplyMapper extends BaseMapper<JobApplyEntity> {

    @Select("SELECT * FROM t_job_apply WHERE id = #{applyId}")
    JobApplyEntity selectByApplyId(String applyId);

    @Update("UPDATE t_job_apply SET apply_status = #{applyStatus}, interview_time = #{interviewTime}, trtc_room_id = #{trtcRoomId}, updated_at = NOW() WHERE id = #{applyId}")
    void updateApplyStatus(String applyId, Integer applyStatus, java.time.LocalDateTime interviewTime, String trtcRoomId);
}