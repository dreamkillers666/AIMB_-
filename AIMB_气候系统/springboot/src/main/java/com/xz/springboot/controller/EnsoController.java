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
     * 手动触发 IRI/CPC ENSO 数据抓取
     * 返回 iri/cpc 各自的 inserted 数量、publishedAt/fetchedAt、message
     */
    @PostMapping("/fetch")
    public Result fetch() {
        Map<String, Object> result = fetchService.fetchNow();
        return Result.success(result);
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

    @GetMapping("/lsta-swin/metadata")
    public Result lstaSwinMetadata() {
        Map<String, Object> data = demoEnvelope();
        data.put("model", "LSTA-Swin");
        data.put("status", "待真实模型与数据接入");
        Map<String, Integer> windows = new LinkedHashMap<>();
        windows.put("longTermMonths", 12);
        windows.put("shortTermMonths", 3);
        data.put("windows", windows);
        data.put("variables", Arrays.asList("SSTA", "SSTA150", "τx", "τy"));
        data.put("weightsAvailable", false);
        data.put("matchingNetcdfAvailable", false);
        data.put("realMetrics", null);
        return Result.success(data);
    }

    @PostMapping("/lsta-swin/forecast")
    public Result lstaSwinForecast(@RequestBody(required = false) Map<String, Object> request) {
        String start = request != null && request.get("forecastStart") != null
                ? request.get("forecastStart").toString() : "2026-01";
        int lead = 20;
        try {
            if (request != null && request.get("leadMonths") != null) {
                lead = Math.max(3, Math.min(20, Integer.parseInt(request.get("leadMonths").toString())));
            }
        } catch (NumberFormatException ignored) { }
        Map<String, Object> data = demoEnvelope();
        data.put("model", "LSTA-Swin");
        data.put("forecastStart", start);
        data.put("leadMonths", lead);
        data.put("indexName", "Niño3.4 / ONI");
        List<Map<String, Object>> series = new ArrayList<>();
        for (int i = 1; i <= lead; i++) {
            double value = 1.55 * Math.exp(-Math.pow((i - 8.0) / 6.2, 2)) - 0.18;
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("leadMonth", i);
            point.put("month", i + "月");
            point.put("value", Math.round(value * 100.0) / 100.0);
            point.put("lower", Math.round((value - 0.28) * 100.0) / 100.0);
            point.put("upper", Math.round((value + 0.28) * 100.0) / 100.0);
            series.add(point);
        }
        data.put("series", series);
        data.put("spatialMaps", demoGrid());
        return Result.success(data);
    }

    @PostMapping("/lsta-swin/replay")
    public Result lstaSwinReplay(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> data = demoEnvelope();
        data.put("model", "LSTA-Swin");
        data.put("forecastStart", request != null && request.get("forecastStart") != null
                ? request.get("forecastStart") : "2015-03");
        data.put("leadMonths", 20);
        data.put("series", demoSeries(20));
        data.put("observationStatus", "真实观测序列待接入");
        data.put("limitation", "演示轨迹不可用于评价 2015-2016 事件预测技巧；峰值滞后与强度偏差待实测复现。");
        return Result.success(data);
    }

    @GetMapping("/lsta-swin/metrics")
    public Result lstaSwinMetrics() {
        Map<String, Object> data = demoEnvelope();
        data.put("realMetrics", null);
        data.put("leadMonths", Arrays.asList(1, 3, 6, 9, 12, 18, 20));
        data.put("systemResults", Collections.emptyList());
        data.put("status", "系统实测指标待复现");
        data.put("ablation", Arrays.asList(
                paperMetric("原始 Swin", 0.59), paperMetric("LST-IAM + MLP", 0.65),
                paperMetric("LSTA-Swin", 0.69), paperMetric("去除 MLP", 0.62),
                paperMetric("替换 Swin Block", 0.62)));
        return Result.success(data);
    }

    @GetMapping("/lsta-swin/explainability")
    public Result lstaSwinExplainability() {
        Map<String, Object> data = demoEnvelope();
        data.put("variables", Arrays.asList("SSTA", "SSTA150", "τx", "τy"));
        data.put("stages", Arrays.asList("充电", "发展", "衰减"));
        data.put("heatmap", demoGrid());
        data.put("interpretationBoundary", "演示热力图不是 SHAP、梯度或模型注意力输出。");
        return Result.success(data);
    }

    private Map<String, Object> demoEnvelope() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("dataMode", "synthetic_demo");
        data.put("isSynthetic", true);
        data.put("source", "服务端固定公式生成的联调演示数据；非观测、非模型推理");
        data.put("generatedAt", java.time.OffsetDateTime.now().toString());
        data.put("warning", "演示数据/待实测复现。当前未接入 LSTA-Swin 权重及匹配的四变量 NetCDF。");
        return data;
    }

    private List<Map<String, Object>> demoSeries(int count) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("leadMonth", i);
            point.put("month", i + "月");
            double value = 1.55 * Math.exp(-Math.pow((i - 8.0) / 6.2, 2)) - 0.18;
            point.put("value", Math.round(value * 100.0) / 100.0);
            result.add(point);
        }
        return result;
    }

    private List<List<Double>> demoGrid() {
        List<List<Double>> grid = new ArrayList<>();
        for (int y = 0; y < 9; y++) {
            List<Double> row = new ArrayList<>();
            for (int x = 0; x < 15; x++) {
                double equatorial = Math.exp(-Math.pow((y - 4.0) / 1.5, 2));
                double pacific = Math.exp(-Math.pow((x - 8.0) / 4.0, 2));
                row.add(Math.round((1.4 * equatorial * pacific - 0.12) * 100.0) / 100.0);
            }
            grid.add(row);
        }
        return grid;
    }

    private Map<String, Object> paperMetric(String variant, double pcc) {
        Map<String, Object> metric = new LinkedHashMap<>();
        metric.put("variant", variant);
        metric.put("pcc", pcc);
        metric.put("dataMode", "paper_result");
        metric.put("isSynthetic", false);
        metric.put("source", "用户提供的 2026 修改提示词；未独立核验论文原文");
        return metric;
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

        // 从最新一批 cpcStrengths 推导三态概率（CPC 强度数据）
        out.put("probabilities", deriveProbabilities(cpc));
        return out;
    }

    /**
     * CPC 强度表按预报季节从当前季(MAM)到最远季(NDJ)排列，
     * 取最近（最远预报）季节作为当前三态概率来源：
     *   laNina = leNeg05（Niño3.4 ≤ -0.5°C 概率）
     *   elNino = gePos05（≥ +0.5°C 概率）
     *   neutral = 100 - laNina - elNino（钳制 0-100）
     * 数据为 0-100 整数；若为 0-1 小数则乘 100 归一化。
     */
    private Map<String, Object> deriveProbabilities(List<EnsoCpcStrengths> cpc) {
        if (cpc == null || cpc.isEmpty()) return null;

        EnsoCpcStrengths first = mostRecentSeason(cpc);
        if (first == null) return null;

        double laNina = first.getLeNeg05() == null ? 0 : first.getLeNeg05();
        double elNino = first.getGePos05() == null ? 0 : first.getGePos05();

        // 归一化：若为 0-1 小数则乘 100
        if (Math.max(laNina, elNino) <= 1.0) {
            laNina *= 100;
            elNino *= 100;
        }

        double neutral = 100 - laNina - elNino;
        laNina = Math.max(0, Math.min(100, laNina));
        elNino = Math.max(0, Math.min(100, elNino));
        neutral = Math.max(0, Math.min(100, neutral));

        Map<String, Object> p = new LinkedHashMap<>();
        p.put("season", first.getSeason());
        p.put("laNina", (int) Math.round(laNina));
        p.put("neutral", (int) Math.round(neutral));
        p.put("elNino", (int) Math.round(elNino));
        p.put("source", "CPC");
        return p;
    }

    /** CPC 强度表季节按预报时序排列，取最远（最近）预报季节。 */
    private static final List<String> CPC_SEASON_ORDER = Arrays.asList(
            "MAM", "AMJ", "MJJ", "JJA", "JAS", "ASO", "SON", "OND", "NDJ");

    private EnsoCpcStrengths mostRecentSeason(List<EnsoCpcStrengths> batch) {
        EnsoCpcStrengths best = null;
        int bestIdx = -1;
        for (EnsoCpcStrengths c : batch) {
            int idx = CPC_SEASON_ORDER.indexOf(c.getSeason());
            if (idx > bestIdx) {
                bestIdx = idx;
                best = c;
            }
        }
        return best;
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
