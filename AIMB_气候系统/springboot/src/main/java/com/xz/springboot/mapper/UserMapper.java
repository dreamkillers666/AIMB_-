package com.xz.springboot.mapper;

import com.xz.springboot.controller.dto.UserPasswordDTO;
import com.xz.springboot.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 青哥哥
 * @since 2023-04-18
 */
public interface UserMapper extends BaseMapper<User> {
//    @Update("update sys_user set password = #{newPassword} where username = #{username} and password = #{password}")
//    int updatePassword(UserPasswordDTO userPasswordDTO);
@Update("UPDATE sys_user SET password = #{userPasswordDTO.newPassword} WHERE username = #{userPasswordDTO.username} AND password = #{userPasswordDTO.password}")
int updatePassword(@Param("userPasswordDTO") UserPasswordDTO userPasswordDTO);

}
