package com.xz.springboot.service;

import com.xz.springboot.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xz
 * @since 2023-05-18
 */
public interface ICommentService extends IService<Comment> {
    List<Comment> findCommentDetail(Integer feedbackId);
}
