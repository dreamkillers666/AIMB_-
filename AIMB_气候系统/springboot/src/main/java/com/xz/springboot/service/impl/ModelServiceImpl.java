package com.xz.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xz.springboot.entity.Model;
import com.xz.springboot.mapper.ModelMapper;
import com.xz.springboot.service.IModelService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 员工工资 服务实现类
 * </p>
 *
 * @author xz
 * @since 2023-12-29
 */
@Service
public class ModelServiceImpl extends ServiceImpl<ModelMapper, Model> implements IModelService {

}
