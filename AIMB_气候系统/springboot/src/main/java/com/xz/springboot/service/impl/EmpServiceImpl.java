package com.xz.springboot.service.impl;

import com.xz.springboot.entity.Emp;
import com.xz.springboot.mapper.EmpMapper;
import com.xz.springboot.service.IEmpService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 员工 服务实现类
 * </p>
 *
 * @author xz
 * @since 2023-04-22
 */
@Service
public class EmpServiceImpl extends ServiceImpl<EmpMapper, Emp> implements IEmpService {

}
