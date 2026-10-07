package com.xz.springboot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ENSO 数据定时抓取任务
 * IRI 官网每月更新一次，每周抓取一次足够。
 */
@Component
public class EnsoFetchScheduler {

    private static final Logger log = LoggerFactory.getLogger(EnsoFetchScheduler.class);

    private final EnsoFetchService fetchService;

    public EnsoFetchScheduler(EnsoFetchService fetchService) {
        this.fetchService = fetchService;
    }

    /**
     * 每周一凌晨 3 点自动抓取 IRI/CPC ENSO 数据
     */
    @Scheduled(cron = "0 0 3 * * MON")
    public void fetchEnsoData() {
        log.info("[EnsoFetchScheduler] 开始定时抓取 ENSO 数据...");
        try {
            Map<String, Object> result = fetchService.fetchNow();
            log.info("[EnsoFetchScheduler] 抓取完成: {}", result);
        } catch (Exception e) {
            log.error("[EnsoFetchScheduler] 抓取失败", e);
        }
    }
}