package com.selenium.studio.db;

import com.google.gson.Gson;
import java.time.Instant;
import java.util.*;

public class DbService {

    private static final DbService INSTANCE = new DbService();
    private final SupabaseClient   client   = new SupabaseClient();
    private final Gson             gson     = new Gson();

    public static DbService getInstance() { return INSTANCE; }
    private DbService() {}

    public boolean login(String email, String password) {
        try {
            var result = client.login(email, password);
            return result.access_token != null;
        } catch (Exception e) {
            System.err.println("[DB] Login failed: " + e.getMessage());
            return false;
        }
    }

    public String startRun(String projectId, String browser, String userId, int totalSteps) {
        try {
            Map<String,Object> data = new LinkedHashMap<>();
            data.put("project_id",  projectId);
            data.put("run_by",      userId);
            data.put("browser",     browser);
            data.put("status",      "running");
            data.put("total_steps", totalSteps);
            data.put("started_at",  Instant.now().toString());
            String resp = client.createRun(data);
            @SuppressWarnings("unchecked")
            List<Map<String,Object>> list = gson.fromJson(resp, List.class);
            if (list != null && !list.isEmpty()) return (String) list.get(0).get("id");
        } catch (Exception e) {
            System.err.println("[DB] Start run failed: " + e.getMessage());
        }
        return null;
    }

    public void finishRun(String runId, int pass, int fail, long durationMs) {
        try {
            Map<String,Object> data = new LinkedHashMap<>();
            data.put("status",      fail == 0 ? "passed" : "failed");
            data.put("pass_count",  pass);
            data.put("fail_count",  fail);
            data.put("duration_ms", (int) durationMs);
            data.put("finished_at", Instant.now().toString());
            client.updateRun(runId, data);
        } catch (Exception e) {
            System.err.println("[DB] Finish run failed: " + e.getMessage());
        }
    }

    public void saveApiLogs(String runId, List<String> rawLogs) {
        try {
            List<Map<String,Object>> logs = new ArrayList<>();
            for (String raw : rawLogs) {
                String[] parts = raw.split("\\|", 3);
                if (parts.length < 3) continue;
                Map<String,Object> log = new LinkedHashMap<>();
                log.put("run_id",  runId);
                log.put("type",    parts[1]);
                String msg = parts[2];
                if (parts[1].equals("REQ") && msg.contains(" ")) {
                    log.put("method", msg.substring(0, msg.indexOf(' ')));
                    log.put("url",    msg.substring(msg.indexOf(' ') + 1));
                } else {
                    log.put("url", msg);
                }
                log.put("logged_at", Instant.now().toString());
                logs.add(log);
            }
            if (!logs.isEmpty()) client.saveApiLogs(logs);
        } catch (Exception e) {
            System.err.println("[DB] Save API logs failed: " + e.getMessage());
        }
    }

    public void saveScreenshot(String runId, String stepId, int order,
                               String fileName, String storageUrl, String trigger) {
        try {
            Map<String,Object> data = new LinkedHashMap<>();
            data.put("run_id",      runId);
            data.put("step_id",     stepId);
            data.put("step_order",  order);
            data.put("file_name",   fileName);
            data.put("storage_url", storageUrl);
            data.put("trigger",     trigger);
            data.put("captured_at", Instant.now().toString());
            client.saveScreenshot(data);
        } catch (Exception e) {
            System.err.println("[DB] Save screenshot failed: " + e.getMessage());
        }
    }

    public String getProjects()              { try { return client.getProjects();          } catch (Exception e) { return "[]"; } }
    public String getProjectSummary()        { try { return client.getProjectSummary();    } catch (Exception e) { return "[]"; } }
    public String getRunHistory(String pid)  { try { return client.getRunHistory(pid, 20); } catch (Exception e) { return "[]"; } }
    public String getStepResults(String rid) { try { return client.getStepResults(rid);    } catch (Exception e) { return "[]"; } }
    public String getApiLogs(String rid)     { try { return client.getApiLogs(rid);        } catch (Exception e) { return "[]"; } }
    public String getScreenshots(String rid) { try { return client.getScreenshots(rid);    } catch (Exception e) { return "[]"; } }

    public SupabaseClient getClient() { return client; }
}