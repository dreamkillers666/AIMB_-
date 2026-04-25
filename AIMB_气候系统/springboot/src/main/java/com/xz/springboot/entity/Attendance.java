package com.xz.springboot.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
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
 * @since 2023-05-09
 */
@Getter
@Setter
  @ApiModel(value = "Attendance对象", description = "")
public class Attendance implements Serializable {

    private static final long serialVersionUID = 1L;

      @ApiModelProperty("id")
        @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

      @ApiModelProperty("员工姓名")
      private String name;

      @ApiModelProperty("工号")
      private String workId;

      @ApiModelProperty("考勤日期")
      private LocalDate attendanceDate;

      @ApiModelProperty("上班时间")
      private double startTime;

      @ApiModelProperty("下班时间")
      private double  endTime;

      @ApiModelProperty("工作时长")
      private double workHours;

      @ApiModelProperty("缺勤次数（每月）")
      private Integer absenceTimes;

      @ApiModelProperty("迟到早退次数（每月）")
      private Integer lateEarlyTimes;

      private Integer isSign;
}
