package com.xz.springboot.service.impl;

import com.xz.springboot.entity.Train;
import com.xz.springboot.mapper.TrainMapper;
import com.xz.springboot.service.ITrainService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xz
 * @since 2023-04-22
 */
@Service
public class TrainServiceImpl extends ServiceImpl<TrainMapper, Train> implements ITrainService {

}
