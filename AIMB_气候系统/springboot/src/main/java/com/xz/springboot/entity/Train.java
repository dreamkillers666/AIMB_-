package com.xz.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDate;
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
 * @since 2023-04-22
 */
@Getter
@Setter
  @ApiModel(value = "Train对象", description = "")
public class Train implements Serializable {

    private static final long serialVersionUID = 1L;

      @ApiModelProperty("编号")
        @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

      @ApiModelProperty("工号")
      private String workId;

      @ApiModelProperty("培训日期")
      private LocalDate trainDate;

      @ApiModelProperty("培训内容")
      private String trainContent;

      @ApiModelProperty("备注")
      private String remark;


}
