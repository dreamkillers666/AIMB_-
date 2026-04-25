package com.xz.springboot.service.impl;

import com.xz.springboot.entity.Feedback;
import com.xz.springboot.mapper.FeedbackMapper;
import com.xz.springboot.service.IFeedbackService;
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
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements IFeedbackService {

}
