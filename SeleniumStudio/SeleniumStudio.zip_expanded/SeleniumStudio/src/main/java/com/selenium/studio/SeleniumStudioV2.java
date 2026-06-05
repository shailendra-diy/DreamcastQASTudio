package com.selenium.studio;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║         SELENIUM AUTOMATION STUDIO - v2.0                   ║
 * ║  Run this class → browser opens → build & run tests visually║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  HOW TO RUN:                                                 ║
 * ║  mvn package                                                 ║
 * ║  java -jar target/selenium-studio-2.0.jar                   ║
 * ║  → Opens http://localhost:8769                               ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class SeleniumStudioV2 {

	private static final int PORT = findAvailablePort(8769);

	private static int findAvailablePort(int startPort) {
	    for (int port = startPort; port < startPort + 10; port++) {
	        try {
	            new java.net.ServerSocket(port).close();
	            return port;
	        } catch (Exception ignored) {}
	    }
	    return startPort;
	}

    // Single shared engine instance
    private static final TestEngine engine = new TestEngine();

    // ═══════════════════════════════════════════════════════════
    //  MAIN
    // ═══════════════════════════════════════════════════════════
    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   Selenium Automation Studio  v2.0      ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("Starting server on port " + PORT + "...");

        startServer();

        String url = "http://localhost:" + PORT + "/";
        System.out.println("Studio running at: " + url);
        System.out.println("Opening browser...");
        openBrowser(url);
        System.out.println("Press Ctrl+C to stop.");
    }

    // ═══════════════════════════════════════════════════════════
    //  SERVER SETUP
    // ═══════════════════════════════════════════════════════════
    private static void startServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/",               ex -> new UIHandler().handle(ex));
        server.createContext("/run",            ex -> new RunHandler().handle(ex));
        server.createContext("/stop",           ex -> new StopHandler().handle(ex));
        server.createContext("/status",         ex -> new StatusHandler().handle(ex));
        server.createContext("/logs",           ex -> new LogsHandler().handle(ex));
        server.createContext("/clearlogs",      ex -> new ClearLogsHandler().handle(ex));
        server.createContext("/apilogs",        ex -> new ApiLogsHandler().handle(ex));
        server.createContext("/clearapilogs",   ex -> new ClearApiLogsHandler().handle(ex));
        server.createContext("/gencode",        ex -> new CodeGenHandler().handle(ex));
        server.createContext("/setapicapture",  ex -> new SetApiCaptureHandler().handle(ex));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    // ═══════════════════════════════════════════════════════════
    //  HTTP HANDLERS
    // ═══════════════════════════════════════════════════════════

    static class UIHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            String html  = HtmlBuilder.build();
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            ex.sendResponseHeaders(200, bytes.length);
            ex.getResponseBody().write(bytes);
            ex.getResponseBody().close();
        }
    }

    static class RunHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            if (engine.isRunning.get()) {
                respond(ex, 409, "{\"error\":\"Already running\"}");
                return;
            }
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            respond(ex, 200, "{\"started\":true}");
            new Thread(() -> {
                try {
                    TestConfig cfg = JsonUtil.parseConfig(body);
                    engine.execute(cfg);
                } catch (Exception e) {
                    engine.runLog.add(ts() + "|FAIL|ERROR: " + e.getMessage());
                    engine.isRunning.set(false);
                }
            }).start();
        }
    }

    static class StopHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            engine.stop();
            respond(ex, 200, "{\"stopped\":true}");
        }
    }

    static class StatusHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            String stepMap = engine.stepStatus.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"")
                .collect(Collectors.joining(","));
            String json = "{\"running\":"   + engine.isRunning.get()   +
                ",\"pass\":"                + engine.passCount.get()   +
                ",\"fail\":"                + engine.failCount.get()   +
                ",\"currentStep\":"         + engine.currentStep.get() +
                ",\"captureApi\":"          + engine.captureApi.get()  +
                ",\"steps\":{"              + stepMap + "}}";
            respond(ex, 200, json);
        }
    }

    static class LogsHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            respond(ex, 200, JsonUtil.logsToJson(engine.runLog));
        }
    }

    static class ClearLogsHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            engine.runLog.clear();
            respond(ex, 200, "{\"cleared\":true}");
        }
    }

    static class ApiLogsHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            respond(ex, 200, JsonUtil.logsToJson(engine.apiLog));
        }
    }

    static class ClearApiLogsHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            engine.apiLog.clear();
            respond(ex, 200, "{\"cleared\":true}");
        }
    }

    static class SetApiCaptureHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            boolean enabled = body.contains("\"enabled\":true");
            engine.captureApi.set(enabled);
            respond(ex, 200, "{\"captureApi\":" + enabled + "}");
        }
    }

    static class CodeGenHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            TestConfig cfg  = JsonUtil.parseConfig(body);
            String     code = CodeGenerator.generate(cfg);
            byte[]     bytes = code.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().set("Content-Type",                 "text/plain; charset=UTF-8");
            ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
            ex.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            ex.sendResponseHeaders(200, bytes.length);
            ex.getResponseBody().write(bytes);
            ex.getResponseBody().close();
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  UTILITIES
    // ═══════════════════════════════════════════════════════════

    private static String ts() {
        return java.time.format.DateTimeFormatter
            .ofPattern("HH:mm:ss")
            .format(java.time.LocalDateTime.now());
    }

    private static boolean handleCors(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            ex.getResponseBody().close();
            return true;
        }
        return false;
    }

    private static void respond(HttpExchange ex, int code, String body) throws IOException {
        ex.getResponseHeaders().set("Content-Type",                  "application/json");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin",   "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods",  "POST, GET, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers",  "Content-Type");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    private static void openBrowser(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if      (os.contains("win")) Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            else if (os.contains("mac")) Runtime.getRuntime().exec(new String[]{"open", url});
            else                         Runtime.getRuntime().exec(new String[]{"xdg-open", url});
        } catch (Exception e) {
            System.out.println("Could not auto-open browser. Please visit: " + url);
        }
    }
}
