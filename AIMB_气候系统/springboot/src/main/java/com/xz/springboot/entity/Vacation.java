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
 * @since 2023-05-11
 */
@Getter
@Setter
  @ApiModel(value = "Vacation对象", description = "")
public class Vacation implements Serializable {

    private static final long serialVersionUID = 1L;

      @ApiModelProperty("id")
        @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

      @ApiModelProperty("员工姓名")
      private String name;

      @ApiModelProperty("工号")
      private String workId;

      @ApiModelProperty("请假日期")
      private LocalDate leaveDate;

      @ApiModelProperty("请假天数")
      private Integer leaveDays;

      @ApiModelProperty("补班日期")
      private LocalDate backDate;

      @ApiModelProperty("请假类型")
      private String vacationType;

      @ApiModelProperty("请假原因")
      private String vacationReason;

      @ApiModelProperty("审核状态")
      private Integer auditStatus;


}
