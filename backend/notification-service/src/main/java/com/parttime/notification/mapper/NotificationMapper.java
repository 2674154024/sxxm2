package com.parttime.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parttime.notification.entity.NotificationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NotificationMapper extends BaseMapper<NotificationEntity> {

    @Select("SELECT * FROM t_notification WHERE user_id = #{userId} ORDER BY created_at DESC")
    Page<NotificationEntity> selectByUserId(Page<NotificationEntity> page, @Param("userId") String userId);

    @Select("SELECT COUNT(*) FROM t_notification WHERE user_id = #{userId} AND is_read = 0")
    Integer countUnread(@Param("userId") String userId);

    @Update("UPDATE t_notification SET is_read = 1, read_at = NOW() WHERE id = #{id}")
    void markAsRead(@Param("id") String id);

    @Update("UPDATE t_notification SET is_read = 1, read_at = NOW() WHERE user_id = #{userId}")
    void markAllAsRead(@Param("userId") String userId);
}