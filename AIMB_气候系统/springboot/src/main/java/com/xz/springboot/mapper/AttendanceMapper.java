package com.xz.springboot.mapper;

import com.xz.springboot.entity.Attendance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author xz
 * @since 2023-05-09
 */
public interface AttendanceMapper extends BaseMapper<Attendance> {
    @Update("UPDATE attendance SET is_sign = 1 WHERE work_id = #{workId}")
    void sign(String workId);

}
