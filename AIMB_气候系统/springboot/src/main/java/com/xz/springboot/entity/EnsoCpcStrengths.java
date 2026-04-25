package com.xz.springboot.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("enso_cpc_strengths")
public class EnsoCpcStrengths {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String season;

    @TableField("le_neg2")
    private Integer leNeg2;

    @TableField("le_neg15")
    private Integer leNeg15;

    @TableField("le_neg1")
    private Integer leNeg1;

    @TableField("le_neg05")
    private Integer leNeg05;

    @TableField("ge_pos05")
    private Integer gePos05;

    @TableField("ge_pos1")
    private Integer gePos1;

    @TableField("ge_pos15")
    private Integer gePos15;

    @TableField("ge_pos2")
    private Integer gePos2;

    @TableField("fetched_at")
    private LocalDateTime fetchedAt;

    @TableField("source_url")
    private String sourceUrl;
}
