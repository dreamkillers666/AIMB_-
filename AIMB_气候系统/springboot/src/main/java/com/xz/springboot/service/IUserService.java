package com.xz.springboot.service;

import com.xz.springboot.controller.dto.UserDTO;
import com.xz.springboot.controller.dto.UserPasswordDTO;
import com.xz.springboot.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 青哥哥
 * @since 2023-04-18
 */
public interface IUserService extends IService<User> {
    UserDTO login(UserDTO userDTO);
    User register(UserDTO userDTO);

    void updatePassword(UserPasswordDTO userPasswordDTO);
}
