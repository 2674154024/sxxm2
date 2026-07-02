package com.parttime.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parttime.im.entity.ImMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ImMessageMapper extends BaseMapper<ImMessageEntity> {

    @Select("SELECT * FROM t_im_message WHERE (from_id = #{userId} AND to_id = #{targetId}) OR (from_id = #{targetId} AND to_id = #{userId}) ORDER BY timestamp DESC")
    List<ImMessageEntity> selectMessagesBetween(@Param("userId") String userId, @Param("targetId") String targetId);

    @Select("SELECT * FROM t_im_message WHERE (from_id = #{userId} AND to_id = #{targetId}) OR (from_id = #{targetId} AND to_id = #{userId}) ORDER BY timestamp DESC")
    Page<ImMessageEntity> selectMessagesBetweenPage(Page<ImMessageEntity> page, @Param("userId") String userId, @Param("targetId") String targetId);

    @Select("SELECT * FROM t_im_message WHERE to_id = #{userId} AND is_read = 0 ORDER BY timestamp ASC")
    List<ImMessageEntity> selectUnreadMessages(@Param("userId") String userId);

    @Select("SELECT " +
            "  other_user AS target_id, " +
            "  MAX(timestamp) AS last_timestamp, " +
            "  (SELECT content FROM t_im_message WHERE " +
            "    (from_id = other_user AND to_id = #{userId}) OR (from_id = #{userId} AND to_id = other_user) " +
            "    ORDER BY timestamp DESC LIMIT 1) AS last_content, " +
            "  (SELECT COUNT(*) FROM t_im_message WHERE " +
            "    to_id = #{userId} AND from_id = other_user AND is_read = 0) AS unread_count " +
            "FROM ( " +
            "  SELECT to_id AS other_user, timestamp FROM t_im_message WHERE from_id = #{userId} " +
            "  UNION ALL " +
            "  SELECT from_id AS other_user, timestamp FROM t_im_message WHERE to_id = #{userId} " +
            ") AS combined " +
            "GROUP BY other_user " +
            "ORDER BY last_timestamp DESC")
    List<ConversationListVO> selectConversationList(@Param("userId") String userId);

    @Select("UPDATE t_im_message SET is_read = 1 WHERE to_id = #{userId} AND from_id = #{targetId} AND is_read = 0")
    void markMessagesAsRead(@Param("userId") String userId, @Param("targetId") String targetId);

    @Select("SELECT COUNT(*) FROM t_im_message WHERE to_id = #{userId} AND is_read = 0")
    Integer countUnreadMessages(@Param("userId") String userId);

    interface ConversationListVO {
        String getTargetId();
        Long getLastTimestamp();
        String getLastContent();
        Integer getUnreadCount();
    }
}