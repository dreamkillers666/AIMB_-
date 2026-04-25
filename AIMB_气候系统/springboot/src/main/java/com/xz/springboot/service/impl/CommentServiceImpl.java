package com.xz.springboot.service.impl;

import com.xz.springboot.entity.Comment;
import com.xz.springboot.mapper.CommentMapper;
import com.xz.springboot.service.ICommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xz
 * @since 2023-05-18
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {
    @Resource
    private CommentMapper commentMapper;

    @Override
    public List<Comment> findCommentDetail(Integer feedbackId) {
        return commentMapper.findCommentDetail(feedbackId);
    }
}
