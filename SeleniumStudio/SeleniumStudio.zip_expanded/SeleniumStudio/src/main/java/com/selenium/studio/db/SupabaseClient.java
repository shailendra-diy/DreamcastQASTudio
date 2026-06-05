package com.selenium.studio.db;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class SupabaseClient {

    private final HttpClient http;
    private final Gson       gson;
    private String           authToken;

    public static class AuthResult {
        public String access_token;
        public String refresh_token;
        public String error;
        public String error_description;
    }

    public SupabaseClient() {
        this.http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.gson = new Gson();
    }

    public AuthResult login(String email, String password)
            throws IOException, InterruptedException {
        String body = gson.toJson(Map.of("email", email, "password", password));
        String resp = post(SupabaseConfig.AUTH_URL + "/token?grant_type=password", body, false);
        AuthResult result = gson.fromJson(resp, AuthResult.class);
        if (result != null && result.access_token != null)
            this.authToken = result.access_token;
        return result != null ? result : new AuthResult();
    }

    public String getProjects() throws IOException, InterruptedException {
        return get(SupabaseConfig.REST_URL + "/projects?order=updated_at.desc&select=*");
    }

    public String createProject(Object data) throws IOException, InterruptedException {
        return post(SupabaseConfig.REST_URL + "/projects", gson.toJson(data), true);
    }

    public String updateProject(String id, Object data) throws IOException, InterruptedException {
        return patch(SupabaseConfig.REST_URL + "/projects?id=eq." + id, gson.toJson(data));
    }

    public String deleteProject(String id) throws IOException, InterruptedException {
        return delete(SupabaseConfig.REST_URL + "/projects?id=eq." + id);
    }

    public String getSteps(String projectId) throws IOException, InterruptedException {
        return get(SupabaseConfig.REST_URL
            + "/test_steps?project_id=eq." + projectId + "&order=step_order.asc");
    }

    public String saveSteps(String projectId, List<?> steps)
            throws IOException, InterruptedException {
        delete(SupabaseConfig.REST_URL + "/test_steps?project_id=eq." + projectId);
        return post(SupabaseConfig.REST_URL + "/test_steps", gson.toJson(steps), true);
    }

    public String createRun(Object data) throws IOException, InterruptedException {
        return post(SupabaseConfig.REST_URL + "/test_runs", gson.toJson(data), true);
    }

    public String updateRun(String id, Object data) throws IOException, InterruptedException {
        return patch(SupabaseConfig.REST_URL + "/test_runs?id=eq." + id, gson.toJson(data));
    }

    public String getRunHistory(String projectId, int limit)
            throws IOException, InterruptedException {
        return get(SupabaseConfig.REST_URL
            + "/test_runs?project_id=eq." + projectId
            + "&order=started_at.desc&limit=" + limit);
    }

    public String saveStepResult(Object data) throws IOException, InterruptedException {
        return post(SupabaseConfig.REST_URL + "/step_results", gson.toJson(data), true);
    }

    public String getStepResults(String runId) throws IOException, InterruptedException {
        return get(SupabaseConfig.REST_URL
            + "/step_results?run_id=eq." + runId + "&order=step_order.asc");
    }

    public String saveApiLogs(List<?> logs) throws IOException, InterruptedException {
        return post(SupabaseConfig.REST_URL + "/api_logs", gson.toJson(logs), true);
    }

    public String getApiLogs(String runId) throws IOException, InterruptedException {
        return get(SupabaseConfig.REST_URL
            + "/api_logs?run_id=eq." + runId + "&order=logged_at.asc");
    }

    public String saveScreenshot(Object data) throws IOException, InterruptedException {
        return post(SupabaseConfig.REST_URL + "/screenshots", gson.toJson(data), true);
    }

    public String getScreenshots(String runId) throws IOException, InterruptedException {
        return get(SupabaseConfig.REST_URL
            + "/screenshots?run_id=eq." + runId + "&order=captured_at.asc");
    }

    public String getProjectSummary() throws IOException, InterruptedException {
        return get(SupabaseConfig.REST_URL + "/project_summary?order=updated_at.desc");
    }

    private String get(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("apikey",        SupabaseConfig.SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer " + token())
            .header("Content-Type",  "application/json")
            .GET().build();
        return http.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    private String post(String url, String body, boolean returnRecord)
            throws IOException, InterruptedException {
        HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("apikey",        SupabaseConfig.SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer " + token())
            .header("Content-Type",  "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body));
        if (returnRecord) b.header("Prefer", "return=representation");
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString()).body();
    }

    private String patch(String url, String body)
            throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("apikey",        SupabaseConfig.SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer " + token())
            .header("Content-Type",  "application/json")
            .header("Prefer",        "return=representation")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(body)).build();
        return http.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    private String delete(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("apikey",        SupabaseConfig.SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer " + token())
            .DELETE().build();
        return http.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    private String token() {
        return authToken != null ? authToken : SupabaseConfig.SUPABASE_ANON_KEY;
    }

    public String getAuthToken()         { return authToken; }
    public void   setAuthToken(String t) { this.authToken = t; }
}