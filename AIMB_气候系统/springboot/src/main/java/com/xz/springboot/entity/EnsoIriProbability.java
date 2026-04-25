package com.xz.springboot.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("enso_iri_probability")
public class EnsoIriProbability {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String season;

    @TableField("la_nina")
    private Integer laNina;

    private Integer neutral;

    @TableField("el_nino")
    private Integer elNino;

    @TableField("published_at")
    private LocalDateTime publishedAt;

    @TableField("source_url")
    private String sourceUrl;

    @TableField("fetched_at")
    private LocalDateTime fetchedAt;
}
