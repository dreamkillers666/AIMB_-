package com.xz.springboot.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xz.springboot.entity.EnsoCpcStrengths;
import com.xz.springboot.entity.EnsoIriProbability;
import com.xz.springboot.mapper.EnsoCpcStrengthsMapper;
import com.xz.springboot.mapper.EnsoIriProbabilityMapper;
import com.xz.springboot.service.EnsoFetchService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/enso")
public class EnsoController {

    private final EnsoFetchService fetchService;
    private final EnsoIriProbabilityMapper iriMapper;
    private final EnsoCpcStrengthsMapper cpcMapper;

    public EnsoController(EnsoFetchService fetchService,
                          EnsoIriProbabilityMapper iriMapper,
                          EnsoCpcStrengthsMapper cpcMapper) {
        this.fetchService = fetchService;
        this.iriMapper = iriMapper;
        this.cpcMapper = cpcMapper;
    }

    // 手动抓取（调试用）
    @PostMapping("/fetch")
    public Map<String, Object> fetchNow() {
        System.out.println(">>> ENSO /fetch called");
        return fetchService.fetchNow();
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

        // CPC 最新 fetchedAt（我们每次抓取一批同一个 fetchedAt）
        EnsoCpcStrengths latestCpc = cpcMapper.selectOne(new LambdaQueryWrapper<EnsoCpcStrengths>()
                .orderByDesc(EnsoCpcStrengths::getFetchedAt)
                .last("limit 1"));
        LocalDateTime cpcFetch = latestCpc == null ? null : latestCpc.getFetchedAt();
        List<EnsoCpcStrengths> cpc = cpcFetch == null ? Collections.emptyList()
                : cpcMapper.selectList(new LambdaQueryWrapper<EnsoCpcStrengths>()
                .eq(EnsoCpcStrengths::getFetchedAt, cpcFetch)
                .orderByAsc(EnsoCpcStrengths::getSeason));

        out.put("iriPublishedAt", iriPub);
        out.put("cpcFetchedAt", cpcFetch);
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
        // 你也可以按自己格式解析 publishedAt 字符串，这里省略
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
