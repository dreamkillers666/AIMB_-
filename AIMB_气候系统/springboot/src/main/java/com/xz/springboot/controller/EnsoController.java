package com.xz.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xz.springboot.entity.EnsoCpcStrengths;
import com.xz.springboot.entity.EnsoIriProbability;
import com.xz.springboot.mapper.EnsoCpcStrengthsMapper;
import com.xz.springboot.mapper.EnsoIriProbabilityMapper;
import com.xz.springboot.service.EnsoFetchService;
import com.xz.springboot.service.EnsoPredictionService; // 1. 确保导入了预测 Service
import org.springframework.web.bind.annotation.*;
import com.xz.springboot.common.Result;
import com.xz.springboot.controller.dto.PredictionResult;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/enso")
public class EnsoController {

    private final EnsoFetchService fetchService;
    private final EnsoIriProbabilityMapper iriMapper;
    private final EnsoCpcStrengthsMapper cpcMapper;
    private final EnsoPredictionService ensoPredictionService; // 2. 声明预测 Service

    // 3. 修改构造函数，注入新的 Service
    public EnsoController(EnsoFetchService fetchService,
                          EnsoIriProbabilityMapper iriMapper,
                          EnsoCpcStrengthsMapper cpcMapper,
                          EnsoPredictionService ensoPredictionService) {
        this.fetchService = fetchService;
        this.iriMapper = iriMapper;
        this.cpcMapper = cpcMapper;
        this.ensoPredictionService = ensoPredictionService;
    }

    /**
     * AI 模型预测接口
     * 调用 Python 引擎进行 ConvLSTM 推理
     */
    @PostMapping("/predict")
    public Result predict(@RequestParam String filePath, @RequestParam int leadTime) {
        // 调用 Service 向 Python 8000 端口发送请求
        PredictionResult res = ensoPredictionService.predict(filePath, leadTime);
        // 返回统一的 Result 格式
        return Result.success(res);
    }


    @GetMapping("/latest")
    public Map<String, Object> latest() {
        Map<String, Object> out = new LinkedHashMap<>();

        // IRI 最新 publishedAt
        EnsoIriProbability latestIri = iriMapper.selectOne(new LambdaQueryWrapper<EnsoIriProbability>()
                .orderByDesc(EnsoIriProbability::getPublishedAt)
                .last("limit 1"));
        LocalDateTime iriPub = latestIri == null ? null : latestIri.getPublishedAt();
        List<EnsoIriProbability> iri = iriPub == null ? Collections.emptyList()
                : iriMapper.selectList(new LambdaQueryWrapper<EnsoIriProbability>()
                .eq(EnsoIriProbability::getPublishedAt, iriPub)
                .orderByAsc(EnsoIriProbability::getSeason));

        // CPC 最新 fetchedAt
        EnsoCpcStrengths latestCpc = cpcMapper.selectOne(new LambdaQueryWrapper<EnsoCpcStrengths>()
                .orderByDesc(EnsoCpcStrengths::getFetchedAt)
                .last("limit 1"));
        LocalDateTime cpcFetch = latestCpc == null ? null : latestCpc.getFetchedAt();
        List<EnsoCpcStrengths> cpc = cpcFetch == null ? Collections.emptyList()
                : cpcMapper.selectList(new LambdaQueryWrapper<EnsoCpcStrengths>()
                .eq(EnsoCpcStrengths::getFetchedAt, cpcFetch)
                .orderByAsc(EnsoCpcStrengths::getSeason));

        out.put("iriPublishedAt", iriPub);
        out.put("cFetchedAt", cpcFetch);
        out.put("iri", iri);
        out.put("cpcStrengths", cpc);
        return out;
    }

    @GetMapping("/iri")
    public List<EnsoIriProbability> iri(@RequestParam(required = false) String publishedAt) {
        if (publishedAt == null || publishedAt.trim().isEmpty()) {
            EnsoIriProbability latest = iriMapper.selectOne(new LambdaQueryWrapper<EnsoIriProbability>()
                    .orderByDesc(EnsoIriProbability::getPublishedAt).last("limit 1"));
            if (latest == null) return Collections.emptyList();
            return iriMapper.selectList(new LambdaQueryWrapper<EnsoIriProbability>()
                    .eq(EnsoIriProbability::getPublishedAt, latest.getPublishedAt())
                    .orderByAsc(EnsoIriProbability::getSeason));
        }
        return Collections.emptyList();
    }

    @GetMapping("/cpc")
    public List<EnsoCpcStrengths> cpc() {
        EnsoCpcStrengths latest = cpcMapper.selectOne(new LambdaQueryWrapper<EnsoCpcStrengths>()
                .orderByDesc(EnsoCpcStrengths::getFetchedAt).last("limit 1"));
        if (latest == null) return Collections.emptyList();
        return cpcMapper.selectList(new LambdaQueryWrapper<EnsoCpcStrengths>()
                .eq(EnsoCpcStrengths::getFetchedAt, latest.getFetchedAt())
                .orderByAsc(EnsoCpcStrengths::getSeason));
    }
}