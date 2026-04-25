package com.xz.springboot.service.impl;

import com.xz.springboot.entity.Salary;
import com.xz.springboot.mapper.SalaryMapper;
import com.xz.springboot.service.ISalaryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 员工工资 服务实现类
 * </p>
 *
 * @author xz
 * @since 2023-05-09
 */
@Service
public class SalaryServiceImpl extends ServiceImpl<SalaryMapper, Salary> implements ISalaryService {

}
