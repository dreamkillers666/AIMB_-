package com.xz.springboot.service.impl;

import com.xz.springboot.entity.Building;
import com.xz.springboot.mapper.BuildingMapper;
import com.xz.springboot.service.IBuildingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xz
 * @since 2023-05-18
 */
@Service
public class BuildingServiceImpl extends ServiceImpl<BuildingMapper, Building> implements IBuildingService {

}
