package com.xz.springboot.component;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

public class IriParser {

    public static class Row {
        public String season;
        public int laNina, neutral, elNino;
    }

    public static class Result {
        public LocalDateTime publishedAt;
        public List<Row> rows = new ArrayList<>();
    }

    private static final Pattern PUBLISHED =
            Pattern.compile("Published:\\s*([A-Za-z]+\\s+\\d{1,2},\\s*\\d{4})");
    private static final Pattern IRI_ROW =
            Pattern.compile("^([A-Z]{3})\\s+(\\d{1,3})\\s+(\\d{1,3})\\s+(\\d{1,3})$");

    public Result parse(String url, int timeoutMs) throws Exception {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) enso-bot/1.0")
                .timeout(timeoutMs)
                .followRedirects(true)
                .get();

        Result out = new Result();
        out.publishedAt = parsePublishedAt(doc);

        // 解析 table#1/#3：直接用 tr.text() 正则匹配 FMA 4 96 0
        for (Element table : doc.select("table")) {
            Elements trs = table.select("tr");
            if (trs.size() < 3) continue;

            String header = trs.get(0).text().toLowerCase();
            if (!(header.contains("season") && header.contains("neutral"))) continue;

            List<Row> rows = new ArrayList<>();
            for (Element tr : trs) {
                String line = tr.text().trim().replaceAll("\\s+", " ");
                Matcher m = IRI_ROW.matcher(line);
                if (!m.find()) continue;

                Row r = new Row();
                r.season = m.group(1);
                r.laNina = Integer.parseInt(m.group(2));
                r.neutral = Integer.parseInt(m.group(3));
                r.elNino = Integer.parseInt(m.group(4));
                rows.add(r);
            }

            if (!rows.isEmpty()) {
                out.rows = rows;
                return out;
            }
        }

        throw new IllegalStateException("IRI: parsed rows empty");
    }

    private LocalDateTime parsePublishedAt(Document doc) {
        Matcher m = PUBLISHED.matcher(doc.text());
        if (!m.find()) return LocalDate.now().atStartOfDay();
        LocalDate d = LocalDate.parse(m.group(1),
                DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH));
        return d.atStartOfDay();
    }
}