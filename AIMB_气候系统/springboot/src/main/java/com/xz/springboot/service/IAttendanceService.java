package com.xz.springboot.service;

import com.xz.springboot.entity.Attendance;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xz
 * @since 2023-05-09
 */
public interface IAttendanceService extends IService<Attendance> {

    void sign(String workId);
}
