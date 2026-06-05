package com.selenium.studio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * JSON utilities using Gson.
 * Parses incoming request JSON into TestConfig / TestStep objects.
 */
public class JsonUtil {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Parses a JSON string (from the /run endpoint) into a TestConfig.
     * Uses a raw Map parse then manually maps fields for robustness.
     */
    @SuppressWarnings("unchecked")
    public static TestConfig parseConfig(String json) {
        Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> raw = GSON.fromJson(json, mapType);

        TestConfig cfg = new TestConfig();

        if (raw.containsKey("url"))           cfg.setUrl(str(raw, "url"));
        if (raw.containsKey("maximize"))      cfg.setMaximize(bool(raw, "maximize"));
        if (raw.containsKey("headless"))      cfg.setHeadless(bool(raw, "headless"));
        if (raw.containsKey("resW"))          cfg.setResW(num(raw, "resW"));
        if (raw.containsKey("resH"))          cfg.setResH(num(raw, "resH"));
        if (raw.containsKey("implicitWait"))  cfg.setImplicitWait(num(raw, "implicitWait"));
        if (raw.containsKey("pageLoad"))      cfg.setPageLoad(num(raw, "pageLoad"));
        if (raw.containsKey("autoScreenshot"))cfg.setAutoScreenshot(bool(raw, "autoScreenshot"));
        if (raw.containsKey("screenshotDir")) cfg.setScreenshotDir(str(raw, "screenshotDir"));
        if (raw.containsKey("captureApi"))    cfg.setCaptureApi(bool(raw, "captureApi"));

        if (raw.containsKey("browsers")) {
            List<String> browsers = (List<String>) raw.get("browsers");
            if (browsers != null && !browsers.isEmpty()) cfg.setBrowsers(browsers);
        }

        if (raw.containsKey("steps")) {
            List<Map<String, Object>> rawSteps = (List<Map<String, Object>>) raw.get("steps");
            if (rawSteps != null) {
                java.util.List<TestStep> stepList = new java.util.ArrayList<>();
                for (Map<String, Object> rs : rawSteps) {
                    TestStep s = new TestStep();
                    if (rs.containsKey("id"))     s.setId((int) Math.round((Double) rs.getOrDefault("id", 0.0)));
                    if (rs.containsKey("action")) s.setAction(rs.get("action").toString());
                    if (rs.containsKey("xpath"))  s.setXpath(rs.get("xpath").toString());
                    if (rs.containsKey("value"))  s.setValue(rs.get("value").toString());
                    if (rs.containsKey("value2")) s.setValue2(rs.get("value2").toString());
                    if (rs.containsKey("wait"))   s.setWait((int) Math.round((Double) rs.getOrDefault("wait", 0.0)));
                    stepList.add(s);
                }
                cfg.setSteps(stepList);
            }
        }

        return cfg;
    }

    /** Converts a list of log strings to a JSON array. */
    public static String logsToJson(List<String> logs) {
        return GSON.toJson(logs);
    }

    /** Serialize any object to JSON string. */
    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    // ── helpers ──────────────────────────────────────────────────────────
    private static String str(Map<String,Object> m, String k) {
        Object v = m.get(k); return v != null ? v.toString() : "";
    }
    private static boolean bool(Map<String,Object> m, String k) {
        Object v = m.get(k);
        if (v instanceof Boolean) return (Boolean) v;
        return "true".equalsIgnoreCase(String.valueOf(v));
    }
    private static int num(Map<String,Object> m, String k) {
        Object v = m.get(k);
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return 0; }
    }
}
