package com.xz.springboot.mapper;

import com.xz.springboot.entity.Vacation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author xz
 * @since 2023-05-11
 */
public interface VacationMapper extends BaseMapper<Vacation> {

    @Update("UPDATE vacation SET audit_status = 1 WHERE id = #{id}")
    void auditSuccess(Integer id);

    @Update("UPDATE vacation SET audit_status = -1 WHERE id = #{id}")
    void auditFail(Integer id);
}
