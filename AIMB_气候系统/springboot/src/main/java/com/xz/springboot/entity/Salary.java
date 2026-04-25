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
 * 员工工资
 * </p>
 *
 * @author xz
 * @since 2023-05-09
 */
@Getter
@Setter
  @ApiModel(value = "Salary对象", description = "员工工资")
public class Salary implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

    private String name;

      @ApiModelProperty("员工工号")
      private String workId;

      @ApiModelProperty("基础工资")
      private Double baseSalary;

      @ApiModelProperty("绩效奖金")
      private Double performanceBonus;

      @ApiModelProperty("社保费用")
      private Double socialSecurity;

      @ApiModelProperty("实际工资")
      private Double actualSalary;


}
