package com.xz.springboot.mapper;

import com.xz.springboot.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author xz
 * @since 2023-05-18
 */
public interface CommentMapper extends BaseMapper<Comment> {
    @Select("select c.*,u.username,u.avatar_url from feedback_comment c left join sys_user u on c.user_id = u.id " +
            "where c.feedback_id = #{feedbackId} order by id desc")
    List<Comment> findCommentDetail(@Param("feedbackId") Integer feedbackId);
}
