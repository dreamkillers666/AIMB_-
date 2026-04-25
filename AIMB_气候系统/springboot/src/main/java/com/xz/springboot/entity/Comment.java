package com.xz.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author xz
 * @since 2023-05-18
 */
@Getter
@Setter
  @TableName("feedback_comment")
@ApiModel(value = "Comment对象", description = "")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

      @ApiModelProperty("内容")
      private String content;

      @ApiModelProperty("评论人id")
      private Integer userId;

      @ApiModelProperty("评论时间")
      private String time;

      @ApiModelProperty("父id")
      private Integer pid;

      @ApiModelProperty("最上级评论id")
      private Integer originId;
      @TableField(exist = false)
      private String pUsername;  // 父节点的用户昵称
      @TableField(exist = false)
      private Integer pUserId;  // 父节点的用户id

      @ApiModelProperty("关联文章的id")
      private Integer feedbackId;

      @TableField(exist = false)
      private String username;
      @TableField(exist = false)
      private String avatarUrl;

      @TableField(exist = false)
      private List<Comment> children;


}
