package com.xz.springboot.service; // 1. 确保包名正确（如果你移动到了service文件夹）

import com.xz.springboot.controller.dto.PredictionResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class EnsoPredictionService {

    // 2. 在这里定义 PYTHON_URL，这样整个类都能看到它
    private final String PYTHON_URL = "http://localhost:8000/predict";

    public PredictionResult predict(String ncFilePath, int leadTime) {
        // 3. 这里的 RestTemplate 和 requestParams 必须在方法内部定义
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("file_path", ncFilePath);
        requestParams.put("lead_time", leadTime);

        try {
            // 4. 这里的参数现在不会爆红了，因为上面已经定义过了
            // 注意：直接映射到 PredictionResult 类，字段名会自动匹配 Python 返回的 JSON
            return restTemplate.postForObject(PYTHON_URL, requestParams, PredictionResult.class);
        } catch (Exception e) {
            System.err.println("调用Python引擎失败: " + e.getMessage());
            return null;
        }
    }
}