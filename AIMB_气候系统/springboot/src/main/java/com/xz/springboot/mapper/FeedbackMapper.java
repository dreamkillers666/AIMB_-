package com.xz.springboot.mapper;

import com.xz.springboot.entity.Feedback;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author xz
 * @since 2023-05-18
 */
public interface FeedbackMapper extends BaseMapper<Feedback> {
    @Update("UPDATE feedback SET feedback_status = 1 WHERE id = #{id}")
    void auditSuccess(Integer id);

    @Update("UPDATE feedback  SET feedback_status = -1 WHERE id = #{id}")
    void auditFail(Integer id);
}
