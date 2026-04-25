package com.xz.springboot.service.impl;

import com.xz.springboot.entity.Vacation;
import com.xz.springboot.mapper.VacationMapper;
import com.xz.springboot.service.IVacationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xz
 * @since 2023-05-11
 */
@Service
public class VacationServiceImpl extends ServiceImpl<VacationMapper, Vacation> implements IVacationService {

}
