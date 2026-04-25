package com.xz.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>
 * 员工工资
 * </p>
 *
 * @author xz
 * @since 2023-12-29
 */
@Getter
@Setter
  @ApiModel(value = "Model对象")
public class Model implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

    private String name;

    private Double mon1;

    private Double mon2;

    private Double mon3;

    private Double mon4;

    private Double mon5;

    private Double mon6;

    private Double mon7;

    private Double mon8;

    private Double mon9;

    private Double mon10;

    private Double mon11;

    private Double mon12;

    private Double mon13;

    private Double mon14;

    private Double mon15;

    private Double mon16;

    private Double mon17;

    private Double mon18;

    private Double mon19;

    private Double mon20;


}
