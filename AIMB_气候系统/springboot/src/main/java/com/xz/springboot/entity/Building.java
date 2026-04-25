package com.xz.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
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
  @ApiModel(value = "Building对象", description = "")
public class Building implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

      @ApiModelProperty("产业名称")
      private String title;

      @ApiModelProperty("经度")
      private String longitude;

      @ApiModelProperty("纬度")
      private String latitude;

      @ApiModelProperty("图标链接")
      private String icon;


}
