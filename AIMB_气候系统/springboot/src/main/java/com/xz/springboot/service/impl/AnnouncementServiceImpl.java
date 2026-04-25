package com.xz.springboot.service.impl;

import com.xz.springboot.entity.Announcement;
import com.xz.springboot.mapper.AnnouncementMapper;
import com.xz.springboot.service.IAnnouncementService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xz
 * @since 2023-05-02
 */
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements IAnnouncementService {

}
