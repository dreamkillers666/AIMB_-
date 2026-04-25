package com.xz.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 员工
 * </p>
 *
 * @author xz
 * @since 2023-04-22
 */
@Getter
@Setter
  @ApiModel(value = "Emp对象", description = "员工")
public class Emp implements Serializable {

    private static final long serialVersionUID = 1L;

      @ApiModelProperty("员工编号")
        @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

      @ApiModelProperty("员工姓名")
      private String name;

      @ApiModelProperty("年龄")
      private Integer age;

      @ApiModelProperty("性别")
      private String gender;

      @ApiModelProperty("身份证号")
      private String idCard;

      @ApiModelProperty("民族")
      private String nation;

      @ApiModelProperty("邮箱")
      private String email;

      @ApiModelProperty("电话号码")
      private String phone;

      @ApiModelProperty("联系地址")
      private String address;

      @ApiModelProperty("所属部门")
      private String dept;

      @ApiModelProperty("职位")
      private String postion;

      @ApiModelProperty("最高学历")
      private String degree;

      @ApiModelProperty("入职日期")
      private Date edate;

      @ApiModelProperty("在职状态")
      private String workState;

      @ApiModelProperty("工号")
      private String workId;

      @ApiModelProperty("转正日期")
      private LocalDate conversionTime;

      @ApiModelProperty("工龄")
      private Integer workAge;

}
