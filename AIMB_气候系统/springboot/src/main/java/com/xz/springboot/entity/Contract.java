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
  @ApiModel(value = "Contract对象", description = "")
public class Contract implements Serializable {

    private static final long serialVersionUID = 1L;

      @ApiModelProperty("合同编号")
        @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

      @ApiModelProperty("工号")
      private String workId;

      @ApiModelProperty("合同期限")
      private Double contractTerm;

      @ApiModelProperty("合同起始日期")
      private LocalDate beginContract;

      @ApiModelProperty("合同终止日期")
      private LocalDate endContract;

      @ApiModelProperty("合同内容")
      private String contractContent;


}
