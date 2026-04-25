package com.xz.springboot.component;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.*;

public class CpcStrengthsParser {

    public static class Row {
        public String season;
        public int leNeg2, leNeg15, leNeg1, leNeg05, gePos05, gePos1, gePos15, gePos2;
    }

    private static final Pattern SEASON3 = Pattern.compile("^[A-Z]{3}$");
    private static final Pattern NUM = Pattern.compile("(-?\\d{1,3})");

    public List<Row> parse(String url, int timeoutMs) throws Exception {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) enso-bot/1.0")
                .timeout(timeoutMs)
                .followRedirects(true)
                .get();

        List<Row> byTable = parseFromHtmlTable(doc);
        if (!byTable.isEmpty()) return byTable;

        List<Row> byPre = parseFromPre(doc);
        if (!byPre.isEmpty()) return byPre;

        List<Row> byText = parseFromWholeText(doc);
        if (!byText.isEmpty()) return byText;

        String title = doc.title();
        String sample = doc.body().text();
        if (sample.length() > 200) sample = sample.substring(0, 200) + "...";
        throw new IllegalStateException("CPC strengths: parsed rows empty. title=" + title + ", sample=" + sample);
    }

    private List<Row> parseFromHtmlTable(Document doc) {
        List<Row> out = new ArrayList<>();

        for (Element table : doc.select("table")) {
            for (Element tr : table.select("tr")) {
                Elements cells = tr.select("th,td");
                if (cells.size() < 9) continue;

                String season = cells.get(0).text().trim();
                if (!SEASON3.matcher(season).matches()) continue;

                List<Integer> nums = new ArrayList<>();
                for (int i = 1; i < cells.size(); i++) {
                    Integer v = tryParseInt(cells.get(i).text());
                    if (v != null) nums.add(v);
                }
                if (nums.size() < 8) continue;

                Row r = new Row();
                r.season = season;
                r.leNeg2 = nums.get(0);
                r.leNeg15 = nums.get(1);
                r.leNeg1 = nums.get(2);
                r.leNeg05 = nums.get(3);
                r.gePos05 = nums.get(4);
                r.gePos1 = nums.get(5);
                r.gePos15 = nums.get(6);
                r.gePos2 = nums.get(7);
                out.add(r);
            }
            if (!out.isEmpty()) return out;
        }
        return out;
    }

    private List<Row> parseFromPre(Document doc) {
        List<Row> out = new ArrayList<>();
        for (Element pre : doc.select("pre")) {
            out.addAll(parseLines(pre.text().split("\\R")));
        }
        return out;
    }

    private List<Row> parseFromWholeText(Document doc) {
        return parseLines(doc.body().wholeText().split("\\R"));
    }

    private List<Row> parseLines(String[] lines) {
        List<Row> out = new ArrayList<>();
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.replace("~", " ").replaceAll("\\s+", " ").trim().split(" ");
            if (parts.length < 9) continue;

            String season = parts[0].trim();
            if (!SEASON3.matcher(season).matches()) continue;

            List<Integer> nums = new ArrayList<>();
            for (int i = 1; i < parts.length; i++) {
                Integer v = tryParseInt(parts[i]);
                if (v != null) nums.add(v);
            }
            if (nums.size() < 8) continue;

            Row r = new Row();
            r.season = season;
            r.leNeg2 = nums.get(0);
            r.leNeg15 = nums.get(1);
            r.leNeg1 = nums.get(2);
            r.leNeg05 = nums.get(3);
            r.gePos05 = nums.get(4);
            r.gePos1 = nums.get(5);
            r.gePos15 = nums.get(6);
            r.gePos2 = nums.get(7);
            out.add(r);
        }
        return out;
    }

    private Integer tryParseInt(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        Matcher m = NUM.matcher(t);
        if (!m.find()) return null;
        try { return Integer.parseInt(m.group(1)); } catch (Exception e) { return null; }
    }
}