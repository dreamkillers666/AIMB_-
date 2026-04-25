package com.xz.springboot.service.impl;

import com.xz.springboot.entity.Attendance;
import com.xz.springboot.mapper.AttendanceMapper;
import com.xz.springboot.service.IAttendanceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xz
 * @since 2023-05-09
 */
@Service
public class AttendanceServiceImpl extends ServiceImpl<AttendanceMapper, Attendance> implements IAttendanceService {

    @Resource
    AttendanceMapper attendanceMapper;
    @Override
    public void sign(String workId) {
        attendanceMapper.sign(workId);
    }
}
