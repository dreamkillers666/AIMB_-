package com.xz.springboot.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xz.springboot.entity.EnsoCpcStrengths;
import com.xz.springboot.entity.EnsoIriProbability;
import com.xz.springboot.mapper.EnsoCpcStrengthsMapper;
import com.xz.springboot.mapper.EnsoIriProbabilityMapper;
import com.xz.springboot.component.CpcStrengthsParser;
import com.xz.springboot.component.IriParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class EnsoFetchService {

    @Value("${enso.sources.iriCurrentUrl:https://iri.columbia.edu/our-expertise/climate/forecasts/enso/current/}")
    private String iriUrl;

    @Value("${enso.sources.cpcStrengthsUrl:https://www.cpc.ncep.noaa.gov/products/analysis_monitoring/enso_advisory/strengths/index.php}")
    private String cpcUrl;

    @Value("${enso.fetch.timeoutMs:15000}")
    private int timeoutMs;

    private final EnsoIriProbabilityMapper iriMapper;
    private final EnsoCpcStrengthsMapper cpcMapper;

    public EnsoFetchService(EnsoIriProbabilityMapper iriMapper, EnsoCpcStrengthsMapper cpcMapper) {
        this.iriMapper = iriMapper;
        this.cpcMapper = cpcMapper;
    }

    public Map<String, Object> fetchNow() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("iri", fetchIri());
        result.put("cpc", fetchCpc());
        return result;
    }

    private Map<String, Object> fetchIri() {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            IriParser.Result parsed = new IriParser().parse(iriUrl, timeoutMs);

            // 幂等：如果这次 publishedAt 已存在，就不重复写
            Long count = iriMapper.selectCount(
                    new LambdaQueryWrapper<EnsoIriProbability>()
                            .eq(EnsoIriProbability::getPublishedAt, parsed.publishedAt)
            );

            if (count != null && count > 0) {
                out.put("inserted", 0);
                out.put("publishedAt", parsed.publishedAt);
                out.put("message", "already up-to-date");
                return out;
            }

            LocalDateTime now = LocalDateTime.now();
            int inserted = 0;
            for (IriParser.Row r : parsed.rows) {
                EnsoIriProbability p = new EnsoIriProbability();
                p.setSeason(r.season);
                p.setLaNina(r.laNina);
                p.setNeutral(r.neutral);
                p.setElNino(r.elNino);
                p.setPublishedAt(parsed.publishedAt);
                p.setSourceUrl(iriUrl);
                p.setFetchedAt(now);
                inserted += iriMapper.insert(p);
            }
            out.put("inserted", inserted);
            out.put("publishedAt", parsed.publishedAt);
            out.put("message", "ok");
            return out;
        } catch (Exception e) {
            e.printStackTrace();
            out.put("inserted", 0);
            out.put("message", "error: " + e.getMessage());
            return out;
        }
    }

    private Map<String, Object> fetchCpc() {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            List<CpcStrengthsParser.Row> rows = new CpcStrengthsParser().parse(cpcUrl, timeoutMs);
            LocalDateTime now = LocalDateTime.now();
            int inserted = 0;
            for (CpcStrengthsParser.Row r : rows) {
                EnsoCpcStrengths c = new EnsoCpcStrengths();
                c.setSeason(r.season);
                c.setLeNeg2(r.leNeg2);
                c.setLeNeg15(r.leNeg15);
                c.setLeNeg1(r.leNeg1);
                c.setLeNeg05(r.leNeg05);
                c.setGePos05(r.gePos05);
                c.setGePos1(r.gePos1);
                c.setGePos15(r.gePos15);
                c.setGePos2(r.gePos2);
                c.setFetchedAt(now);
                c.setSourceUrl(cpcUrl);
                inserted += cpcMapper.insert(c);
            }
            out.put("inserted", inserted);
            out.put("fetchedAt", now);
            out.put("message", "ok");
            return out;
        } catch (Exception e) {
            e.printStackTrace();
            out.put("inserted", 0);
            out.put("message", "error: " + e.getMessage());
            return out;
        }
    }
}
