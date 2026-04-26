package com.xz.springboot.controller.dto;

import lombok.Data;

@Data
public class PredictionResult {
    // 必须和 Python 返回的 JSON 键名一模一样（带下划线）
    private int lead_time;
    private double enso_index;
    private String plot_url;
    private String input_url;
    private String true_url;
    private String pred_url;
    private String diff_url;
    private String line_url;
}