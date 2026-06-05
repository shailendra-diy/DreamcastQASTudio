package com.selenium.studio;

import com.sun.net.httpserver.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.edge.*;
import org.openqa.selenium.support.ui.*;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.devtools.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║         SELENIUM AUTOMATION STUDIO - v2.0 (COMPLETE)        ║
 * ║  Single file - Run this class, browser opens automatically  ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  FEATURES:                                                   ║
 * ║  ✅ Visual Test Step Builder (18 actions)                    ║
 * ║           ║
 * ║  ✅ API Log Export to JSON/TXT                               ║
 * ║  ✅ Save / Load test projects (.json)                        ║
 * ║  ✅ Java Code Generator                                      ║
 * ║  ✅ Auto screenshot on failure                               ║
 * ║  ✅ Per-step save button                                     ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  MAVEN DEPENDENCIES (pom.xml):                              ║
 * ║  <dependency>                                               ║
 * ║    <groupId>org.seleniumhq.selenium</groupId>               ║
 * ║    <artifactId>selenium-java</artifactId>                   ║
 * ║    <version>4.18.1</version>                                ║
 * ║  </dependency>                                              ║
 * ║  <dependency>                                               ║
 * ║    <groupId>commons-io</groupId>                            ║
 * ║    <artifactId>commons-io</artifactId>                      ║
 * ║    <version>2.15.1</version>                                ║
 * ║  </dependency>                                              ║
 * ║  <dependency>                                               ║
 * ║    <groupId>com.google.code.gson</groupId>                  ║
 * ║    <artifactId>gson</artifactId>                            ║
 * ║    <version>2.10.1</version>                                ║
 * ║  </dependency>                                              ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class SeleniumStudioV2 {

    private static final int PORT = 8769;
    private static HttpServer server;
    private static WebDriver testDriver;

    // Execution log — server-side, cleared on each run or on explicit /clearlogs
    private static final List<String> runLog      = new CopyOnWriteArrayList<>();
    // API capture log — separate list
    private static final List<String> apiLog      = new CopyOnWriteArrayList<>();

    private static volatile boolean isRunning     = false;
    private static volatile boolean captureApi    = false;
    private static volatile int passCount         = 0;
    private static volatile int failCount         = 0;
    private static volatile int currentStep       = -1;
    private static final Map<Integer, String> stepStatus = new ConcurrentHashMap<>();

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

    // ═══════════════════════════════════════════════════════════
    //  SERVER SETUP
    // ═══════════════════════════════════════════════════════════
    private static void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/",            new UIHandler());
        server.createContext("/run",         new RunHandler());
        server.createContext("/stop",        new StopHandler());
        server.createContext("/status",      new StatusHandler());
        server.createContext("/logs",        new LogsHandler());
        server.createContext("/clearlogs",   new ClearLogsHandler());
        server.createContext("/apilogs",     new ApiLogsHandler());
        server.createContext("/clearapilogs",new ClearApiLogsHandler());
        server.createContext("/gencode",     new CodeGenHandler());
        server.createContext("/setapicapture", new SetApiCaptureHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    // ═══════════════════════════════════════════════════════════
    //  HTTP HANDLERS
    // ═══════════════════════════════════════════════════════════
    static class UIHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            String html = buildHTML();
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
            if (isRunning) { respond(ex, 409, "{\"error\":\"Already running\"}"); return; }
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            final String bodyFinal = body;
            respond(ex, 200, "{\"started\":true}");
            new Thread(() -> {
                try { executeTests(bodyFinal); }
                catch (Exception e) { runLog.add(ts() + "|FAIL|ERROR: " + e.getMessage()); isRunning = false; }
            }).start();
        }
    }

    static class StopHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            isRunning = false;
            if (testDriver != null) { try { testDriver.quit(); } catch (Exception ignored) {} testDriver = null; }
            respond(ex, 200, "{\"stopped\":true}");
        }
    }

    static class StatusHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            String stepMap = stepStatus.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"")
                .collect(Collectors.joining(","));
            String json = "{\"running\":" + isRunning +
                ",\"pass\":"        + passCount +
                ",\"fail\":"        + failCount +
                ",\"currentStep\":" + currentStep +
                ",\"captureApi\":"  + captureApi +
                ",\"steps\":{"      + stepMap + "}}";
            respond(ex, 200, json);
        }
    }

    static class LogsHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            respond(ex, 200, logsToJson(runLog));
        }
    }

    static class ClearLogsHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            runLog.clear();
            respond(ex, 200, "{\"cleared\":true}");
        }
    }

    static class ApiLogsHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            respond(ex, 200, logsToJson(apiLog));
        }
    }

    static class ClearApiLogsHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            apiLog.clear();
            respond(ex, 200, "{\"cleared\":true}");
        }
    }

    static class SetApiCaptureHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            captureApi = body.contains("\"enabled\":true");
            respond(ex, 200, "{\"captureApi\":" + captureApi + "}");
        }
    }

    static class CodeGenHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String code = generateJavaCode(body);
            byte[] bytes = code.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
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
        return DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());
    }

    private static String logsToJson(List<String> list) {
        return "[" + list.stream()
            .map(l -> "\"" + l.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "") + "\"")
            .collect(Collectors.joining(",")) + "]";
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

    // ═══════════════════════════════════════════════════════════
    //  TEST EXECUTION ENGINE
    // ═══════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private static void executeTests(String jsonBody) throws Exception {
        isRunning = true; passCount = 0; failCount = 0; currentStep = -1;
        runLog.clear(); stepStatus.clear(); apiLog.clear();

        Map<String,Object> cfg = parseJson(jsonBody);
        String  url       = str(cfg, "url",           "https://example.com");
        boolean maximize  = bool(cfg, "maximize",     true);
        boolean headless  = bool(cfg, "headless",     false);
        int     implWait  = num(cfg, "implicitWait",  10);
        int     pageLoad  = num(cfg, "pageLoad",      30);
        int     resW      = num(cfg, "resW",          1920);
        int     resH      = num(cfg, "resH",          1080);
        String  ssDir     = str(cfg, "screenshotDir", "./screenshots");
        boolean autoSS    = bool(cfg, "autoScreenshot", true);
        List<String>           browsers = (List<String>) cfg.getOrDefault("browsers", List.of("chrome"));
        List<Map<String,Object>> steps  = (List<Map<String,Object>>) cfg.getOrDefault("steps", new ArrayList<>());

        log("INFO", "🚀 Selenium Automation Studio v2.0 — Starting");
        log("INFO", "📋 Steps: " + steps.size() + "  |  Browsers: " + browsers);
        log("INFO", "🌐 URL: " + url);
        log("INFO", "📡 API Capture: " + (captureApi ? "ON ✅" : "OFF"));

        for (String browser : browsers) {
            if (!isRunning) break;
            log("INFO", "\n════════ Browser: " + browser.toUpperCase() + " ════════");
            try {
                testDriver = createDriver(browser, headless);
                testDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implWait));
                testDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoad));
                if (maximize) testDriver.manage().window().maximize();
                else          testDriver.manage().window().setSize(new Dimension(resW, resH));

                // ── CDP API Capture (Chrome only) ──
                if (captureApi && browser.equalsIgnoreCase("chrome") && testDriver instanceof HasDevTools) {
                    setupApiCapture((HasDevTools) testDriver);
                }

                testDriver.get(url);
                log("INFO", "✅ Opened: " + url);

                for (int i = 0; i < steps.size(); i++) {
                    if (!isRunning) break;
                    currentStep = i;
                    Map<String,Object> step = steps.get(i);
                    String action   = str(step, "action",  "click");
                    String xpath    = str(step, "xpath",   "");
                    String value    = str(step, "value",   "");
                    String value2   = str(step, "value2",  "");
                    int    waitAfter = num(step, "wait",   0);
                    log("INFO", "▶ Step " + (i+1) + ": " + action + (xpath.isEmpty() ? "" : " | " + xpath));
                    boolean passed = runStep(testDriver, action, xpath, value, value2, i, ssDir, autoSS);
                    if (passed) { stepStatus.put(i, "pass"); passCount++; log("PASS", "✅ Step " + (i+1) + " PASSED"); }
                    else        { stepStatus.put(i, "fail"); failCount++; log("FAIL", "❌ Step " + (i+1) + " FAILED"); }
                    if (waitAfter > 0) Thread.sleep(waitAfter);
                }
            } catch (Exception e) {
                log("FAIL", "💥 Browser error: " + e.getMessage());
            } finally {
                if (testDriver != null) { try { testDriver.quit(); } catch (Exception ignored) {} testDriver = null; }
            }
        }
        currentStep = -1; isRunning = false;
        log("INFO", "═══════════════════════════════════════");
        log(failCount == 0 ? "PASS" : "FAIL",
            "🏁 DONE — PASS: " + passCount + "  FAIL: " + failCount);
        log("INFO", "═══════════════════════════════════════");
    }

    // CDP API capture setup
    @SuppressWarnings({"unchecked","rawtypes"})
    private static void setupApiCapture(HasDevTools devToolsDriver) {
        try {
            DevTools devTools = devToolsDriver.getDevTools();
            devTools.createSession();

            // Use reflection to avoid hard version dependency
            try {
                Class<?> networkClass = Class.forName("org.openqa.selenium.devtools.v85.network.Network");
                Object enableCmd = networkClass.getMethod("enable",
                    Optional.class, Optional.class, Optional.class)
                    .invoke(null, Optional.empty(), Optional.empty(), Optional.empty());
                devTools.send((org.openqa.selenium.devtools.Command<?>) enableCmd);

                // FIX: requestWillBeSent/responseReceived are static methods, not fields.
                // Cast to raw Event (not Event<?>) so raw Consumer is accepted — avoids
                // "Consumer<X> not applicable for Event<capture#7-of ?>" compile error.
                org.openqa.selenium.devtools.Event rawReqEvent =
                    (org.openqa.selenium.devtools.Event) networkClass.getMethod("requestWillBeSent").invoke(null);
                org.openqa.selenium.devtools.Event rawRespEvent =
                    (org.openqa.selenium.devtools.Event) networkClass.getMethod("responseReceived").invoke(null);

                devTools.addListener(rawReqEvent, (java.util.function.Consumer) (Object event) -> {
                    try {
                        Object request = event.getClass().getMethod("getRequest").invoke(event);
                        String reqUrl  = request.getClass().getMethod("getUrl").invoke(request).toString();
                        String method  = request.getClass().getMethod("getMethod").invoke(request).toString();
                        apiLog.add(ts() + "|REQ|" + method + " " + reqUrl);
                    } catch (Exception ignored) {}
                });

                devTools.addListener(rawRespEvent, (java.util.function.Consumer) (Object event) -> {
                    try {
                        Object resp      = event.getClass().getMethod("getResponse").invoke(event);
                        String respUrl   = resp.getClass().getMethod("getUrl").invoke(resp).toString();
                        Object statusObj = resp.getClass().getMethod("getStatus").invoke(resp);
                        String status    = statusObj != null ? statusObj.toString() : "?";
                        apiLog.add(ts() + "|RES|" + status + " " + respUrl);
                    } catch (Exception ignored) {}
                });
                log("INFO", "📡 CDP API Capture active");
            } catch (Exception reflEx) {
                // Fallback: use DevTools directly with latest version classes
                try {
                    var network = org.openqa.selenium.devtools.v85.network.Network.enable(
                        Optional.empty(), Optional.empty(), Optional.empty());
                    devTools.send(network);
                    devTools.addListener(
                        org.openqa.selenium.devtools.v85.network.Network.requestWillBeSent(),
                        req -> apiLog.add(ts() + "|REQ|" + req.getRequest().getMethod() + " " + req.getRequest().getUrl())
                    );
                    devTools.addListener(
                        org.openqa.selenium.devtools.v85.network.Network.responseReceived(),
                        resp -> apiLog.add(ts() + "|RES|" + resp.getResponse().getStatus() + " " + resp.getResponse().getUrl())
                    );
                    log("INFO", "📡 CDP API Capture active (v85)");
                } catch (Exception ignored2) {
                    log("WARN", "⚠️ CDP not available for this Chrome version — API capture disabled");
                }
            }
        } catch (Exception e) {
            log("WARN", "⚠️ Could not init DevTools: " + e.getMessage());
        }
    }

    private static WebDriver createDriver(String browser, boolean headless) {
        switch (browser.toLowerCase()) {
            case "firefox": {
                FirefoxOptions o = new FirefoxOptions();
                if (headless) o.addArguments("-headless");
                return new FirefoxDriver(o);
            }
            case "edge": {
                EdgeOptions o = new EdgeOptions();
                if (headless) o.addArguments("--headless", "--no-sandbox");
                return new EdgeDriver(o);
            }
            default: {
                ChromeOptions o = new ChromeOptions();
                if (headless) o.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
                return new ChromeDriver(o);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  STEP RUNNER
    // ═══════════════════════════════════════════════════════════
    private static boolean runStep(WebDriver d, String action, String xpath,
                                    String value, String value2, int idx,
                                    String ssDir, boolean autoSS) {
        try {
            WebDriverWait wait = new WebDriverWait(d, Duration.ofSeconds(15));
            switch (action) {
                case "click":
                    wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath))).click();
                    break;
                case "type":
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).sendKeys(value);
                    break;
                case "clearField":
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).clear();
                    break;
                case "dropdown": {
                    WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
                    Select sel = new Select(el);
                    switch (value) {
                        case "selectByIndex": sel.selectByIndex(Integer.parseInt(value2.trim())); break;
                        case "selectByValue": sel.selectByValue(value2); break;
                        default:              sel.selectByVisibleText(value2);
                    }
                    break;
                }
                case "getText": {
                    String text = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).getText();
                    log("INFO", "   📖 Text: " + text);
                    break;
                }
                case "getAttribute": {
                    String attr = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).getAttribute(value);
                    log("INFO", "   🏷 [" + value + "]: " + attr);
                    break;
                }
                case "verifyText": {
                    String actual = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).getText();
                    if (!actual.equals(value)) throw new AssertionError("Expected: '" + value + "' Got: '" + actual + "'");
                    log("INFO", "   ✔ Text verified: " + actual);
                    break;
                }
                case "verifyElement":
                    if (!wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).isDisplayed())
                        throw new AssertionError("Element not visible");
                    break;
                case "navigate":
                    d.get(value);
                    break;
                case "screenshot":
                    takeScreenshot(d, value.isEmpty() ? "step_" + (idx + 1) : value, ssDir);
                    break;
                case "scrollTo": {
                    WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
                    ((JavascriptExecutor) d).executeScript(
                        "arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", el);
                    break;
                }
                case "hover": {
                    WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
                    new Actions(d).moveToElement(el).perform();
                    break;
                }
                case "jsClick": {
                    WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
                    ((JavascriptExecutor) d).executeScript("arguments[0].click();", el);
                    break;
                }
                case "acceptAlert": {
                    Alert alert = new WebDriverWait(d, Duration.ofSeconds(5)).until(ExpectedConditions.alertIsPresent());
                    if ("dismiss".equals(value)) alert.dismiss(); else alert.accept();
                    break;
                }
                case "switchFrame": {
                    try { d.switchTo().frame(Integer.parseInt(value)); }
                    catch (NumberFormatException e) {
                        d.switchTo().frame(wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(value))));
                    }
                    break;
                }
                case "switchWindow": {
                    String main = d.getWindowHandle();
                    for (String h : d.getWindowHandles()) if (!h.equals(main)) { d.switchTo().window(h); break; }
                    break;
                }
                case "print":
                    log("INFO", "   🖨 " + value);
                    break;
                case "wait":
                    Thread.sleep(Long.parseLong(value.isEmpty() ? "1000" : value));
                    break;
                default:
                    log("INFO", "   ⚠ Unknown action: " + action);
            }
            return true;
        } catch (Exception e) {
            log("FAIL", "   ⚠ " + e.getMessage());
            if (autoSS) takeScreenshot(d, "FAIL_step" + (idx + 1), ssDir);
            return false;
        }
    }

    private static void takeScreenshot(WebDriver d, String name, String dir) {
        try {
            new File(dir).mkdirs();
            String ts   = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
            File   src  = ((TakesScreenshot) d).getScreenshotAs(OutputType.FILE);
            File   dest = new File(dir + "/" + name + "_" + ts + ".png");
            FileUtils.copyFile(src, dest);
            log("INFO", "   📸 Screenshot: " + dest.getAbsolutePath());
        } catch (Exception e) {
            log("FAIL", "   Screenshot error: " + e.getMessage());
        }
    }

    private static void log(String type, String msg) {
        runLog.add(ts() + "|" + type + "|" + msg);
        System.out.println("[" + type + "] " + msg);
    }

    // ═══════════════════════════════════════════════════════════
    //  JAVA CODE GENERATOR
    // ═══════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private static String generateJavaCode(String jsonBody) {
        try {
            Map<String,Object> cfg = parseJson(jsonBody);
            String  url      = str(cfg,  "url",           "https://example.com");
            boolean maximize = bool(cfg, "maximize",      true);
            boolean headless = bool(cfg, "headless",      false);
            int     implWait = num(cfg,  "implicitWait",  10);
            int     pageLoad = num(cfg,  "pageLoad",      30);
            int     resW     = num(cfg,  "resW",          1920);
            int     resH     = num(cfg,  "resH",          1080);
            String  ssDir    = str(cfg,  "screenshotDir", "./screenshots");
            boolean autoSS   = bool(cfg, "autoScreenshot", true);
            List<String>            browsers = (List<String>) cfg.getOrDefault("browsers", List.of("chrome"));
            List<Map<String,Object>> steps   = (List<Map<String,Object>>) cfg.getOrDefault("steps", new ArrayList<>());

            StringBuilder sb = new StringBuilder();
            sb.append("package com.automation.studio;\n\n");
            sb.append("import org.openqa.selenium.*;\n");
            sb.append("import org.openqa.selenium.chrome.*;\n");
            sb.append("import org.openqa.selenium.firefox.*;\n");
            sb.append("import org.openqa.selenium.edge.*;\n");
            sb.append("import org.openqa.selenium.support.ui.*;\n");
            sb.append("import org.openqa.selenium.interactions.*;\n");
            sb.append("import org.apache.commons.io.FileUtils;\n");
            sb.append("import java.io.*;\nimport java.time.*;\nimport java.time.format.*;\n\n");
            sb.append("/** Auto-generated by Selenium Automation Studio v2.0 */\n");
            sb.append("public class GeneratedTest {\n\n");
            sb.append("    WebDriver driver;\n    int pass = 0, fail = 0;\n");
            sb.append("    String ssDir = \"").append(ssDir).append("\";\n\n");
            sb.append("    public static void main(String[] args) throws Exception {\n");
            sb.append("        new GeneratedTest().execute();\n    }\n\n");
            sb.append("    void execute() throws Exception {\n");

            for (String br : browsers) {
                sb.append("        System.out.println(\"===== ").append(br.toUpperCase()).append(" =====\");\n");
                sb.append("        try {\n");
                switch (br.toLowerCase()) {
                    case "firefox":
                        sb.append("            FirefoxOptions o = new FirefoxOptions();\n");
                        if (headless) sb.append("            o.addArguments(\"-headless\");\n");
                        sb.append("            driver = new FirefoxDriver(o);\n");
                        break;
                    case "edge":
                        sb.append("            EdgeOptions o = new EdgeOptions();\n");
                        if (headless) sb.append("            o.addArguments(\"--headless\");\n");
                        sb.append("            driver = new EdgeDriver(o);\n");
                        break;
                    default:
                        sb.append("            ChromeOptions o = new ChromeOptions();\n");
                        if (headless) sb.append("            o.addArguments(\"--headless\",\"--no-sandbox\");\n");
                        sb.append("            driver = new ChromeDriver(o);\n");
                }
                sb.append("            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(").append(implWait).append("));\n");
                sb.append("            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(").append(pageLoad).append("));\n");
                if (maximize) sb.append("            driver.manage().window().maximize();\n");
                else sb.append("            driver.manage().window().setSize(new Dimension(").append(resW).append(",").append(resH).append("));\n");
                sb.append("            driver.get(\"").append(url).append("\");\n");
                sb.append("            runSteps();\n");
                sb.append("        } finally { if(driver!=null) driver.quit(); }\n    }\n\n");
            }

            sb.append("    void runSteps() throws Exception {\n");
            for (int i = 0; i < steps.size(); i++) {
                Map<String,Object> s = steps.get(i);
                String a = str(s,"action","click"), x = str(s,"xpath",""),
                       v = str(s,"value",""),       v2 = str(s,"value2","");
                int    w = num(s,"wait",0);
                sb.append("        // Step ").append(i+1).append(": ").append(a).append("\n");
                sb.append("        step(\"Step ").append(i+1).append("\", () -> {\n");
                switch (a) {
                    case "click":        sb.append("            driver.findElement(By.xpath(\"").append(x).append("\")).click();\n"); break;
                    case "type":         sb.append("            driver.findElement(By.xpath(\"").append(x).append("\")).sendKeys(\"").append(v).append("\");\n"); break;
                    case "clearField":   sb.append("            driver.findElement(By.xpath(\"").append(x).append("\")).clear();\n"); break;
                    case "dropdown":     sb.append("            new Select(driver.findElement(By.xpath(\"").append(x).append("\"))).").append(v.isEmpty()?"selectByVisibleText":v).append("(\"").append(v2).append("\");\n"); break;
                    case "getText":      sb.append("            System.out.println(driver.findElement(By.xpath(\"").append(x).append("\")).getText());\n"); break;
                    case "getAttribute": sb.append("            System.out.println(driver.findElement(By.xpath(\"").append(x).append("\")).getAttribute(\"").append(v).append("\"));\n"); break;
                    case "verifyText":   sb.append("            String t=driver.findElement(By.xpath(\"").append(x).append("\")).getText();if(!t.equals(\"").append(v).append("\"))throw new AssertionError(\"Expected: ").append(v).append(" Got:\"+t);\n"); break;
                    case "navigate":     sb.append("            driver.get(\"").append(v).append("\");\n"); break;
                    case "screenshot":   sb.append("            screenshot(\"").append(v.isEmpty()?"step_"+(i+1):v).append("\");\n"); break;
                    case "scrollTo":     sb.append("            ((JavascriptExecutor)driver).executeScript(\"arguments[0].scrollIntoView(true);\",driver.findElement(By.xpath(\"").append(x).append("\")));\n"); break;
                    case "hover":        sb.append("            new Actions(driver).moveToElement(driver.findElement(By.xpath(\"").append(x).append("\"))).perform();\n"); break;
                    case "jsClick":      sb.append("            ((JavascriptExecutor)driver).executeScript(\"arguments[0].click();\",driver.findElement(By.xpath(\"").append(x).append("\")));\n"); break;
                    case "acceptAlert":  sb.append("            driver.switchTo().alert().").append("dismiss".equals(v)?"dismiss":"accept").append("();\n"); break;
                    case "print":        sb.append("            System.out.println(\"[LOG] ").append(v).append("\");\n"); break;
                    case "wait":         sb.append("            Thread.sleep(").append(v.isEmpty()?"1000":v).append(");\n"); break;
                }
                sb.append("        });\n");
                if (w > 0) sb.append("        Thread.sleep(").append(w).append(");\n");
            }
            sb.append("        System.out.println(\"\\n===== Results: PASS=\"+pass+\" FAIL=\"+fail+\" =====\");\n");
            sb.append("    }\n\n");
            sb.append("    void step(String name, RunnableStep r) {\n");
            sb.append("        System.out.print(\"[RUN] \"+name+\" ... \");\n");
            sb.append("        try { r.run(); pass++; System.out.println(\"PASS\"); }\n");
            sb.append("        catch(Exception e) { fail++; System.out.println(\"FAIL: \"+e.getMessage());\n");
            if (autoSS) sb.append("            screenshot(\"FAIL_\"+name.replaceAll(\"[^a-zA-Z0-9]\",\"_\")); }\n");
            else         sb.append("        }\n");
            sb.append("    }\n\n");
            sb.append("    void screenshot(String name) {\n");
            sb.append("        try { new File(ssDir).mkdirs();\n");
            sb.append("            String ts = DateTimeFormatter.ofPattern(\"yyyyMMdd_HHmmss\").format(LocalDateTime.now());\n");
            sb.append("            FileUtils.copyFile(((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE),\n");
            sb.append("                new File(ssDir+\"/\"+name+\"_\"+ts+\".png\"));\n");
            sb.append("        } catch(Exception e){ System.err.println(\"SS Error: \"+e.getMessage()); }\n    }\n\n");
            sb.append("    @FunctionalInterface interface RunnableStep { void run() throws Exception; }\n}\n");
            return sb.toString();
        } catch (Exception e) {
            return "// Code generation error: " + e.getMessage();
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  HTML UI BUILDER
    // ═══════════════════════════════════════════════════════════
    private static String buildHTML() {

        String css =
            "<style>" +
            ":root{--bg:#0a0c12;--s1:#111420;--s2:#181c2a;--s3:#1e2234;--bd:#252940;--bd2:#303558;" +
            "--acc:#4f8ef7;--acc2:#7c5ce4;--grn:#00e5a0;--red:#ff5270;--yel:#ffca3a;--ora:#ff9f43;" +
            "--tx:#dce1f0;--tx2:#8890aa;--tx3:#4a5070;--mo:'JetBrains Mono',monospace;--sa:'DM Sans',sans-serif;" +
            "--glow-b:0 0 18px rgba(79,142,247,.25);--glow-g:0 0 18px rgba(0,229,160,.25);--glow-r:0 0 18px rgba(255,82,112,.25);}" +
            "*{margin:0;padding:0;box-sizing:border-box;}" +
            "body{font-family:var(--sa);background:var(--bg);color:var(--tx);height:100vh;display:flex;flex-direction:column;overflow:hidden;}" +
            // scrollbar
            "::-webkit-scrollbar{width:4px;height:4px;}" +
            "::-webkit-scrollbar-track{background:transparent;}" +
            "::-webkit-scrollbar-thumb{background:var(--bd2);border-radius:4px;}" +
            // header
            ".hdr{background:var(--s1);border-bottom:1px solid var(--bd);padding:0 18px;height:52px;display:flex;align-items:center;justify-content:space-between;flex-shrink:0;}" +
            ".logo{display:flex;align-items:center;gap:10px;}" +
            ".logo-icon{width:28px;height:28px;background:linear-gradient(135deg,var(--acc),var(--acc2));border-radius:7px;display:flex;align-items:center;justify-content:center;font-size:14px;}" +
            ".logo-txt{font-size:15px;font-weight:600;}" +
            ".logo-ver{font-size:10px;background:linear-gradient(135deg,var(--acc),var(--acc2));-webkit-background-clip:text;-webkit-text-fill-color:transparent;font-family:var(--mo);font-weight:600;}" +
            ".ha{display:flex;gap:6px;align-items:center;}" +
            // buttons
            ".btn{padding:6px 13px;border-radius:7px;border:1px solid var(--bd2);background:transparent;color:var(--tx2);font-family:var(--sa);font-size:12px;font-weight:500;cursor:pointer;transition:all .15s;display:inline-flex;align-items:center;gap:5px;white-space:nowrap;}" +
            ".btn:hover{background:var(--s3);border-color:var(--acc);color:var(--acc);}" +
            ".btn-run{background:var(--grn);border-color:var(--grn);color:#000;font-weight:700;box-shadow:var(--glow-g);}" +
            ".btn-run:hover{background:#00d490;box-shadow:0 0 24px rgba(0,229,160,.4);color:#000;}" +
            ".btn-stop{background:var(--red);border-color:var(--red);color:#fff;display:none;box-shadow:var(--glow-r);}" +
            ".btn-stop:hover{background:#e0405f;color:#fff;}" +
            ".btn-sm{padding:4px 9px;font-size:11px;}" +
            ".btn-acc{background:rgba(79,142,247,.15);border-color:var(--acc);color:var(--acc);}" +
            ".btn-acc:hover{background:rgba(79,142,247,.25);}" +
            ".btn-grn{background:rgba(0,229,160,.12);border-color:var(--grn);color:var(--grn);}" +
            ".btn-grn:hover{background:rgba(0,229,160,.22);}" +
            ".btn-red{border-color:var(--red);color:var(--red);}" +
            ".btn-red:hover{background:rgba(255,82,112,.1);}" +
            // layout
            ".layout{display:flex;flex:1;overflow:hidden;}" +
            // sidebar
            ".sidebar{width:268px;min-width:268px;background:var(--s1);border-right:1px solid var(--bd);overflow-y:auto;display:flex;flex-direction:column;}" +
            ".main{flex:1;display:flex;flex-direction:column;overflow:hidden;}" +
            // right panel
            ".rp{width:320px;min-width:320px;background:var(--s1);border-left:1px solid var(--bd);display:flex;flex-direction:column;}" +
            // panel headers
            ".ph{padding:10px 13px;border-bottom:1px solid var(--bd);display:flex;align-items:center;justify-content:space-between;flex-shrink:0;}" +
            ".pt{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:1px;color:var(--tx3);}" +
            // sections
            ".sec{padding:12px 13px;border-bottom:1px solid var(--bd);}" +
            ".sl{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:.8px;color:var(--tx3);margin-bottom:8px;}" +
            ".fl{margin-bottom:9px;}" +
            ".fl:last-child{margin-bottom:0;}" +
            ".fl label{font-size:11px;color:var(--tx2);display:block;margin-bottom:4px;font-weight:500;}" +
            // inputs
            "input[type=text],input[type=number],select,textarea{width:100%;background:var(--s2);border:1px solid var(--bd2);color:var(--tx);border-radius:6px;padding:6px 9px;font-family:var(--sa);font-size:12px;outline:none;transition:border .15s;}" +
            "input:focus,select:focus{border-color:var(--acc);box-shadow:0 0 0 2px rgba(79,142,247,.1);}" +
            "select option{background:var(--s2);}" +
            // browser grid
            ".bg{display:grid;grid-template-columns:1fr 1fr;gap:6px;}" +
            ".bc{background:var(--s2);border:1px solid var(--bd2);border-radius:7px;padding:8px;cursor:pointer;transition:all .15s;display:flex;align-items:center;gap:7px;user-select:none;}" +
            ".bc:hover{border-color:var(--acc);}" +
            ".bc.on{border-color:var(--acc);background:rgba(79,142,247,.08);box-shadow:inset 0 0 0 1px rgba(79,142,247,.3);}" +
            ".bi{font-size:17px;}" +
            ".bn{font-size:11px;font-weight:600;}" +
            ".cd{width:12px;height:12px;border-radius:50%;border:2px solid var(--bd2);margin-left:auto;transition:all .15s;flex-shrink:0;}" +
            ".bc.on .cd{background:var(--acc);border-color:var(--acc);box-shadow:0 0 6px var(--acc);}" +
            // toggles
            ".tr{display:flex;align-items:center;justify-content:space-between;margin-bottom:8px;}" +
            ".tr:last-child{margin-bottom:0;}" +
            ".tl{font-size:12px;color:var(--tx2);display:flex;align-items:center;gap:5px;}" +
            ".tg{position:relative;width:32px;height:17px;}" +
            ".tg input{display:none;}" +
            ".ts{position:absolute;inset:0;background:var(--bd2);border-radius:20px;cursor:pointer;transition:.2s;}" +
            ".ts::after{content:'';position:absolute;width:11px;height:11px;left:3px;top:3px;background:#fff;border-radius:50%;transition:.2s;}" +
            ".tg input:checked+.ts{background:var(--acc);}" +
            ".tg input:checked+.ts::after{transform:translateX(15px);}" +
            // api toggle accent
            ".tg-api input:checked+.ts{background:var(--ora);}" +
            // res
            ".rr{display:flex;gap:6px;align-items:center;}" +
            ".rr span{color:var(--tx3);font-size:11px;}" +
            ".rr input{width:70px;}" +
            ".r2{display:grid;grid-template-columns:1fr 1fr;gap:7px;}" +
            // step toolbar
            ".stb{padding:9px 13px;border-bottom:1px solid var(--bd);display:flex;align-items:center;justify-content:space-between;background:var(--s1);flex-shrink:0;}" +
            // step container
            ".sc{flex:1;overflow-y:auto;padding:9px;background:var(--bg);}" +
            // step card
            ".sk{background:var(--s1);border:1px solid var(--bd);border-radius:9px;margin-bottom:8px;overflow:hidden;transition:border .2s;}" +
            ".sk:hover{border-color:var(--bd2);}" +
            ".sk.pass{border-left:3px solid var(--grn);}" +
            ".sk.fail{border-left:3px solid var(--red);}" +
            ".sk.run{border-left:3px solid var(--yel);animation:pulse 1s infinite;}" +
            "@keyframes pulse{0%,100%{opacity:1}50%{opacity:.65}}" +
            ".sh{padding:8px 11px;display:flex;align-items:center;gap:8px;cursor:pointer;}" +
            ".sn{width:20px;height:20px;border-radius:5px;background:var(--s3);display:flex;align-items:center;justify-content:center;font-size:9px;font-weight:700;font-family:var(--mo);color:var(--tx3);flex-shrink:0;}" +
            ".sk.pass .sn{background:rgba(0,229,160,.12);color:var(--grn);}" +
            ".sk.fail .sn{background:rgba(255,82,112,.12);color:var(--red);}" +
            ".sk.run .sn{background:rgba(255,202,58,.12);color:var(--yel);}" +
            ".sab{font-size:9px;font-weight:700;font-family:var(--mo);padding:2px 6px;border-radius:4px;background:rgba(79,142,247,.12);color:var(--acc);flex-shrink:0;text-transform:uppercase;letter-spacing:.5px;}" +
            ".ss{flex:1;font-size:11px;color:var(--tx2);overflow:hidden;text-overflow:ellipsis;white-space:nowrap;}" +
            ".sb{padding:10px 11px;border-top:1px solid var(--bd);background:rgba(0,0,0,.2);display:none;}" +
            ".sb.open{display:block;}" +
            ".sf{display:grid;gap:7px;}" +
            ".war{display:flex;align-items:center;gap:7px;margin-top:7px;}" +
            ".war label{font-size:11px;color:var(--tx3);white-space:nowrap;}" +
            ".war input{width:70px;}" +
            ".sar{display:flex;gap:6px;margin-top:9px;align-items:center;}" +
            ".sar .save-ok{font-size:11px;color:var(--grn);display:none;}" +
            // action add panel
            ".asa{padding:10px 13px;border-top:1px solid var(--bd);background:var(--s1);display:none;flex-shrink:0;}" +
            ".ag{display:grid;grid-template-columns:repeat(3,1fr);gap:6px;margin-top:8px;}" +
            ".at{background:var(--s2);border:1px solid var(--bd);border-radius:7px;padding:8px 6px;text-align:center;cursor:pointer;transition:all .15s;}" +
            ".at:hover{border-color:var(--acc);background:rgba(79,142,247,.08);transform:translateY(-1px);}" +
            ".at .ic{font-size:17px;margin-bottom:2px;}" +
            ".at .nm{font-size:9px;font-weight:600;color:var(--tx2);line-height:1.3;text-transform:uppercase;letter-spacing:.4px;}" +
            // log panel
            ".lp{flex:1;overflow-y:auto;padding:8px;font-family:var(--mo);font-size:10.5px;}" +
            ".ll{padding:2px 0;display:flex;gap:7px;border-bottom:1px solid rgba(37,41,64,.5);line-height:1.5;}" +
            ".lt{color:var(--tx3);flex-shrink:0;min-width:57px;}" +
            ".lm{word-break:break-all;}" +
            ".lm.INFO{color:var(--tx2);}" +
            ".lm.PASS{color:var(--grn);}" +
            ".lm.FAIL{color:var(--red);}" +
            ".lm.WARN{color:var(--yel);}" +
            ".lm.REQ{color:#7cd4f7;}" +
            ".lm.RES{color:#c3a6ff;}" +
            // progress bar
            ".pbw{background:var(--bg);border-radius:4px;height:3px;margin:5px 13px;overflow:hidden;flex-shrink:0;}" +
            ".pb{height:100%;background:linear-gradient(90deg,var(--acc),var(--grn));border-radius:4px;transition:width .3s;width:0%;}" +
            // stats row
            ".sr{display:flex;gap:6px;padding:8px 13px;border-bottom:1px solid var(--bd);flex-shrink:0;}" +
            ".sc2{flex:1;background:var(--s2);border-radius:7px;padding:7px;text-align:center;border:1px solid var(--bd);}" +
            ".sn2{font-size:17px;font-weight:700;font-family:var(--mo);}" +
            ".sl2{font-size:9px;color:var(--tx3);text-transform:uppercase;letter-spacing:.5px;margin-top:1px;}" +
            ".sn2.g{color:var(--grn);}.sn2.r{color:var(--red);}.sn2.y{color:var(--yel);}" +
            // empty state
            ".es{display:flex;flex-direction:column;align-items:center;justify-content:center;height:100%;color:var(--tx3);text-align:center;padding:30px;}" +
            ".es .ei{font-size:38px;margin-bottom:10px;opacity:.3;}" +
            ".es p{font-size:12px;line-height:1.7;}" +
            // toast
            ".toast{position:fixed;bottom:18px;right:18px;background:var(--s3);border:1px solid var(--bd2);border-radius:9px;padding:9px 15px;font-size:12px;z-index:999;transform:translateY(60px);opacity:0;transition:all .3s;}" +
            ".toast.show{transform:translateY(0);opacity:1;}" +
            ".toast.ok{border-color:var(--grn);color:var(--grn);background:rgba(0,229,160,.08);}" +
            ".toast.er{border-color:var(--red);color:var(--red);background:rgba(255,82,112,.08);}" +
            // modal
            ".mo{position:fixed;inset:0;background:rgba(0,0,0,.75);z-index:200;display:none;align-items:center;justify-content:center;backdrop-filter:blur(4px);}" +
            ".mo.open{display:flex;}" +
            ".md{background:var(--s1);border:1px solid var(--bd2);border-radius:13px;width:580px;max-height:86vh;overflow-y:auto;box-shadow:0 24px 60px rgba(0,0,0,.6);}" +
            ".mh{padding:16px 20px 13px;border-bottom:1px solid var(--bd);display:flex;align-items:center;justify-content:space-between;}" +
            ".mt{font-size:14px;font-weight:600;}" +
            ".mc{background:none;border:none;color:var(--tx3);cursor:pointer;font-size:17px;line-height:1;}" +
            ".mc:hover{color:var(--tx);}" +
            ".mb{padding:16px 20px;}" +
            ".mf{padding:12px 20px;border-top:1px solid var(--bd);display:flex;gap:8px;justify-content:flex-end;}" +
            ".ca{background:var(--bg);border:1px solid var(--bd);border-radius:7px;padding:13px;font-family:var(--mo);font-size:10.5px;color:#8be0a4;line-height:1.7;overflow-x:auto;white-space:pre;max-height:420px;overflow-y:auto;}" +
            // tabs
            ".tabs{display:flex;border-bottom:1px solid var(--bd);flex-shrink:0;}" +
            ".tab{padding:8px 14px;font-size:11px;font-weight:600;color:var(--tx3);cursor:pointer;border-bottom:2px solid transparent;margin-bottom:-1px;transition:all .15s;display:flex;align-items:center;gap:5px;}" +
            ".tab:hover{color:var(--tx2);}" +
            ".tab.active{color:var(--acc);border-bottom-color:var(--acc);}" +
            ".tab-pane{display:none;flex:1;overflow:hidden;flex-direction:column;}" +
            ".tab-pane.active{display:flex;}" +
            // api badge
            ".api-badge{font-size:9px;padding:1px 5px;border-radius:3px;background:rgba(255,159,67,.15);color:var(--ora);font-family:var(--mo);}" +
            ".api-live{display:inline-block;width:6px;height:6px;border-radius:50%;background:var(--ora);margin-right:3px;animation:blink 1s infinite;}" +
            "@keyframes blink{0%,100%{opacity:1}50%{opacity:.2}}" +
            // tag
            ".tag{font-size:10px;padding:2px 7px;border-radius:20px;}" +
            ".tb{background:rgba(79,142,247,.12);color:var(--acc);}" +
            // divider
            ".sep{height:1px;background:var(--bd);margin:6px 0;}" +
            // log toolbar
            ".ltb{padding:6px 9px;border-bottom:1px solid var(--bd);display:flex;gap:5px;align-items:center;flex-shrink:0;}" +
            "</style>";

        String body =
            "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<title>Selenium Automation Studio v2.0</title>" +
            "<link href='https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;600&family=DM+Sans:wght@300;400;500;600;700&display=swap' rel='stylesheet'>" +
            css +
            "</head><body>" +

            // ── HEADER ──────────────────────────────────────────────────
            "<div class='hdr'>" +
            "<div class='logo'>" +
            "<div class='logo-icon'>&#x1F916;</div>" +
            "<div class='logo-txt'>Selenium Studio</div>" +
            "<div class='logo-ver'>v2.0</div>" +
            "</div>" +
            "<div class='ha'>" +
            "<button class='btn btn-sm' id='btnSave'>&#x1F4BE; Save</button>" +
            "<button class='btn btn-sm' id='btnLoad'>&#x1F4C2; Load</button>" +
            "<button class='btn btn-sm' id='btnCode'>&#x3C;/&#x3E; Code</button>" +
            "<div style='width:1px;height:22px;background:var(--bd);margin:0 2px;'></div>" +
            "<button class='btn btn-run' id='btnRun'>&#x25B6; Run Tests</button>" +
            "<button class='btn btn-stop' id='btnStop'>&#x23F9; Stop</button>" +
            "</div></div>" +

            "<div class='layout'>" +

            // ── SIDEBAR ──────────────────────────────────────────────────
            "<div class='sidebar'>" +
            "<div class='ph'><span class='pt'>&#x2699;&#xFE0F; Configuration</span></div>" +

            "<div class='sec'>" +
            "<div class='sl'>Target URL</div>" +
            "<div class='fl'><input type='text' id='url' placeholder='https://example.com'/></div>" +
            "</div>" +

            "<div class='sec'>" +
            "<div class='sl'>Browser</div>" +
            "<div class='bg'>" +
            "<div class='bc' id='bc-chrome' data-b='chrome'><div class='bi'>&#x1F310;</div><div class='bn'>Chrome</div><div class='cd'></div></div>" +
            "<div class='bc' id='bc-firefox' data-b='firefox'><div class='bi'>&#x1F98A;</div><div class='bn'>Firefox</div><div class='cd'></div></div>" +
            "<div class='bc' id='bc-edge' data-b='edge'><div class='bi'>&#x1F535;</div><div class='bn'>Edge</div><div class='cd'></div></div>" +
            "<div class='bc' id='bc-all' data-b='all'><div class='bi'>&#x26A1;</div><div class='bn'>All</div><div class='cd'></div></div>" +
            "</div></div>" +

            "<div class='sec'>" +
            "<div class='sl'>Window</div>" +
            "<div class='tr'><span class='tl'>&#x1F5A5; Maximize</span><label class='tg'><input type='checkbox' id='maximize' checked><span class='ts'></span></label></div>" +
            "<div class='tr'><span class='tl'>&#x1F47B; Headless Mode</span><label class='tg'><input type='checkbox' id='headless'><span class='ts'></span></label></div>" +
            "<div class='fl' style='margin-top:6px;'><label>Resolution (if not maximized)</label><div class='rr'><input type='number' id='resW' value='1920'/><span>&#xD7;</span><input type='number' id='resH' value='1080'/></div></div>" +
            "</div>" +

            "<div class='sec'>" +
            "<div class='sl'>Timeouts</div>" +
            "<div class='r2'>" +
            "<div class='fl'><label>Implicit (sec)</label><input type='number' id='implWait' value='10'/></div>" +
            "<div class='fl'><label>Page Load (sec)</label><input type='number' id='pageLoad' value='30'/></div>" +
            "</div></div>" +

            "<div class='sec'>" +
            "<div class='sl'>Screenshot</div>" +
            "<div class='tr'><span class='tl'>&#x1F4F8; Auto on Fail</span><label class='tg'><input type='checkbox' id='autoSS' checked><span class='ts'></span></label></div>" +
            "<div class='fl' style='margin-top:6px;'><label>Save Folder</label><input type='text' id='ssDir' value='./screenshots'/></div>" +
            "</div>" +

            "<div class='sec'>" +
            "<div class='sl'>API Capture</div>" +
            "<div class='tr'><span class='tl'><span id='apiLiveInd'></span>&#x1F4E1; Capture Network APIs</span><label class='tg tg-api'><input type='checkbox' id='apiCapture'><span class='ts'></span></label></div>" +
            "<div style='font-size:10px;color:var(--tx3);margin-top:4px;line-height:1.5;'>Chrome only. Logs all HTTP requests &amp; responses in the API tab during test run.</div>" +
            "</div>" +

            "</div>" + // end .sidebar

            // ── MAIN ─────────────────────────────────────────────────────
            "<div class='main'>" +
            "<div class='stb'>" +
            "<div style='display:flex;align-items:center;gap:8px;'>" +
            "<span style='font-size:13px;font-weight:600;'>Test Steps</span>" +
            "<span class='tag tb' id='scnt'>0 steps</span>" +
            "</div>" +
            "<div style='display:flex;gap:6px;'>" +
            "<button class='btn btn-sm' id='btnClear'>&#x1F5D1; Clear All</button>" +
            "<button class='btn btn-sm btn-acc' id='btnAddStep'>&#xFF0B; Add Step</button>" +
            "</div></div>" +

            "<div class='sc' id='sc'>" +
            "<div class='es' id='es'><div class='ei'>&#x1F4CB;</div><p>No steps yet.<br>Click <b>+ Add Step</b> to get started.</p></div>" +
            "</div>" +

            // Action picker
            "<div class='asa' id='asa'>" +
            "<div style='display:flex;align-items:center;justify-content:space-between;'>" +
            "<span style='font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:.8px;color:var(--tx2);'>Choose Action</span>" +
            "<button class='btn btn-sm' id='btnCloseAdd'>&#x2715;</button>" +
            "</div>" +
            "<div class='ag' id='actionGrid'>" +
            "<div class='at' data-action='click'><div class='ic'>&#x1F446;</div><div class='nm'>Click</div></div>" +
            "<div class='at' data-action='type'><div class='ic'>&#x2328;&#xFE0F;</div><div class='nm'>Type Text</div></div>" +
            "<div class='at' data-action='dropdown'><div class='ic'>&#x1F4CB;</div><div class='nm'>Dropdown</div></div>" +
            "<div class='at' data-action='getText'><div class='ic'>&#x1F4D6;</div><div class='nm'>Get Text</div></div>" +
            "<div class='at' data-action='getAttribute'><div class='ic'>&#x1F3F7;&#xFE0F;</div><div class='nm'>Get Attr</div></div>" +
            "<div class='at' data-action='verifyText'><div class='ic'>&#x2705;</div><div class='nm'>Verify Text</div></div>" +
            "<div class='at' data-action='verifyElement'><div class='ic'>&#x1F50D;</div><div class='nm'>Verify Elem</div></div>" +
            "<div class='at' data-action='navigate'><div class='ic'>&#x1F310;</div><div class='nm'>Navigate</div></div>" +
            "<div class='at' data-action='screenshot'><div class='ic'>&#x1F4F8;</div><div class='nm'>Screenshot</div></div>" +
            "<div class='at' data-action='scrollTo'><div class='ic'>&#x2195;&#xFE0F;</div><div class='nm'>Scroll To</div></div>" +
            "<div class='at' data-action='hover'><div class='ic'>&#x1F5B1;</div><div class='nm'>Hover</div></div>" +
            "<div class='at' data-action='clearField'><div class='ic'>&#x1F5D1;&#xFE0F;</div><div class='nm'>Clear Field</div></div>" +
            "<div class='at' data-action='jsClick'><div class='ic'>&#x26A1;</div><div class='nm'>JS Click</div></div>" +
            "<div class='at' data-action='acceptAlert'><div class='ic'>&#x1F514;</div><div class='nm'>Alert</div></div>" +
            "<div class='at' data-action='switchFrame'><div class='ic'>&#x1F5BC;&#xFE0F;</div><div class='nm'>Switch Frame</div></div>" +
            "<div class='at' data-action='switchWindow'><div class='ic'>&#x1F500;</div><div class='nm'>New Window</div></div>" +
            "<div class='at' data-action='print'><div class='ic'>&#x1F4DD;</div><div class='nm'>Print Log</div></div>" +
            "<div class='at' data-action='wait'><div class='ic'>&#x23F3;</div><div class='nm'>Wait</div></div>" +
            "</div></div>" +
            "</div>" + // end .main

            // ── RIGHT PANEL ────────────────────────────────────────────────
            "<div class='rp'>" +
            "<div class='ph'><span class='pt'>&#x1F4CA; Results &amp; Logs</span></div>" +
            "<div class='sr'>" +
            "<div class='sc2'><div class='sn2 y' id='st'>0</div><div class='sl2'>Total</div></div>" +
            "<div class='sc2'><div class='sn2 g' id='sp'>0</div><div class='sl2'>Pass</div></div>" +
            "<div class='sc2'><div class='sn2 r' id='sf'>0</div><div class='sl2'>Fail</div></div>" +
            "</div>" +
            "<div class='pbw'><div class='pb' id='pb'></div></div>" +

            // tabs
            "<div class='tabs'>" +
            "<div class='tab active' id='tab-exec' data-tab='exec'>&#x1F4DC; Exec Log</div>" +
            "<div class='tab' id='tab-api' data-tab='api'>&#x1F4E1; API Log <span class='api-badge' id='apiCnt'>0</span></div>" +
            "</div>" +

            // exec log pane
            "<div class='tab-pane active' id='pane-exec'>" +
            "<div class='ltb'>" +
            "<button class='btn btn-sm' id='btnClearLogs'>&#x1F5D1; Clear</button>" +
            "<button class='btn btn-sm btn-grn' id='btnExportLogs'>&#x1F4E4; Export</button>" +
            "</div>" +
            "<div class='lp' id='lp'><div style='color:var(--tx3);font-size:11px;padding:14px;text-align:center;'>Ready...</div></div>" +
            "</div>" +

            // api log pane
            "<div class='tab-pane' id='pane-api'>" +
            "<div class='ltb'>" +
            "<button class='btn btn-sm' id='btnClearApiLogs'>&#x1F5D1; Clear</button>" +
            "<button class='btn btn-sm btn-grn' id='btnExportApiLogs'>&#x1F4E4; Export</button>" +
            "<button class='btn btn-sm' style='border-color:var(--ora);color:var(--ora);' id='btnExportApiJson'>JSON</button>" +
            "</div>" +
            "<div class='lp' id='alp'><div style='color:var(--tx3);font-size:11px;padding:14px;text-align:center;'>Enable API Capture in sidebar to start logging network requests.</div></div>" +
            "</div>" +

            "</div>" + // end .rp
            "</div>" + // end .layout

            // ── CODE MODAL ────────────────────────────────────────────────
            "<div class='mo' id='codeModal'>" +
            "<div class='md' style='width:620px;'>" +
            "<div class='mh'><span class='mt'>&#x3C;/&#x3E; Generated Java Code</span><button class='mc' id='btnCloseCode'>&#x2715;</button></div>" +
            "<div class='mb'>" +
            "<div style='font-size:11px;color:var(--tx2);margin-bottom:10px;'>Copy and paste into Eclipse / IntelliJ as <b>GeneratedTest.java</b>.</div>" +
            "<div class='ca' id='codeDisp'></div>" +
            "</div>" +
            "<div class='mf'>" +
            "<button class='btn' id='btnCopyCode'>&#x1F4CB; Copy All</button>" +
            "<button class='btn btn-acc' id='btnCloseCode2'>Done</button>" +
            "</div></div></div>" +

            "<div class='toast' id='toast'></div>" +

            // ── JAVASCRIPT ────────────────────────────────────────────────
            "<script>\n" +
            "var steps = [], ctr = 0, polling = null, apiPolling = null;\n" +
            "\n" +
            "var LABELS = {click:'Click',type:'Type Text',dropdown:'Dropdown',getText:'Get Text',\n" +
            "  getAttribute:'Get Attr',verifyText:'Verify Text',verifyElement:'Verify Elem',\n" +
            "  navigate:'Navigate',screenshot:'Screenshot',scrollTo:'Scroll To',hover:'Hover',\n" +
            "  clearField:'Clear Field',jsClick:'JS Click',acceptAlert:'Alert',\n" +
            "  switchFrame:'Switch Frame',switchWindow:'Switch Window',print:'Print Log',wait:'Wait'\n" +
            "};\n" +
            "\n" +
            "// ── Tabs ──\n" +
            "document.querySelectorAll('.tab').forEach(function(tab) {\n" +
            "  tab.addEventListener('click', function() {\n" +
            "    var t = tab.dataset.tab;\n" +
            "    document.querySelectorAll('.tab').forEach(function(x){x.classList.remove('active');});\n" +
            "    document.querySelectorAll('.tab-pane').forEach(function(x){x.classList.remove('active');});\n" +
            "    tab.classList.add('active');\n" +
            "    document.getElementById('pane-' + t).classList.add('active');\n" +
            "  });\n" +
            "});\n" +
            "\n" +
            "// ── Browser toggle ──\n" +
            "document.querySelectorAll('.bc').forEach(function(el) {\n" +
            "  el.addEventListener('click', function() {\n" +
            "    var b = el.dataset.b;\n" +
            "    if (b === 'all') {\n" +
            "      var willOn = !el.classList.contains('on');\n" +
            "      ['chrome','firefox','edge'].forEach(function(x) {\n" +
            "        var c = document.getElementById('bc-' + x);\n" +
            "        willOn ? c.classList.add('on') : c.classList.remove('on');\n" +
            "      });\n" +
            "      willOn ? el.classList.add('on') : el.classList.remove('on');\n" +
            "    } else {\n" +
            "      el.classList.toggle('on');\n" +
            "      var sel = ['chrome','firefox','edge'].filter(function(x) {\n" +
            "        return document.getElementById('bc-' + x).classList.contains('on');\n" +
            "      });\n" +
            "      sel.length === 3 ? document.getElementById('bc-all').classList.add('on')\n" +
            "                       : document.getElementById('bc-all').classList.remove('on');\n" +
            "    }\n" +
            "  });\n" +
            "});\n" +
            "\n" +
            "// ── API Capture toggle ──\n" +
            "document.getElementById('apiCapture').addEventListener('change', function() {\n" +
            "  var enabled = this.checked;\n" +
            "  fetch('/setapicapture', {\n" +
            "    method: 'POST',\n" +
            "    headers: {'Content-Type':'application/json'},\n" +
            "    body: JSON.stringify({enabled: enabled})\n" +
            "  });\n" +
            "  var ind = document.getElementById('apiLiveInd');\n" +
            "  ind.innerHTML = enabled ? '<span class=\"api-live\"></span>' : '';\n" +
            "  toast(enabled ? '📡 API Capture ON' : 'API Capture OFF', enabled ? 'ok' : '');\n" +
            "});\n" +
            "\n" +
            "// ── Add step panel ──\n" +
            "document.getElementById('btnAddStep').addEventListener('click', function() {\n" +
            "  var asa = document.getElementById('asa');\n" +
            "  asa.style.display = (asa.style.display === 'none' || asa.style.display === '') ? 'block' : 'none';\n" +
            "});\n" +
            "document.getElementById('btnCloseAdd').addEventListener('click', function() {\n" +
            "  document.getElementById('asa').style.display = 'none';\n" +
            "});\n" +
            "\n" +
            "// ── Action grid click ──\n" +
            "document.getElementById('actionGrid').addEventListener('click', function(e) {\n" +
            "  var at = e.target.closest('.at');\n" +
            "  if (!at) return;\n" +
            "  var action = at.dataset.action;\n" +
            "  if (!action) return;\n" +
            "  ctr++;\n" +
            "  var newStep = {id:ctr,action:action,xpath:'',value:'',value2:'',wait:0,status:'pending'};\n" +
            "  steps.push(newStep);\n" +
            "  renderSteps();\n" +
            "  updateCnt();\n" +
            "  // Open new step automatically\n" +
            "  var newSb = document.getElementById('sb-' + ctr);\n" +
            "  if (newSb) newSb.classList.add('open');\n" +
            "  document.getElementById('asa').style.display = 'none';\n" +
            "  // Scroll new step into view\n" +
            "  var newSk = document.getElementById('sk-' + ctr);\n" +
            "  if (newSk) newSk.scrollIntoView({behavior:'smooth',block:'nearest'});\n" +
            "  toast('Step added: ' + LABELS[action], 'ok');\n" +
            "});\n" +
            "\n" +
            "// ── Clear all ──\n" +
            "document.getElementById('btnClear').addEventListener('click', function() {\n" +
            "  if (steps.length && !confirm('Delete all steps?')) return;\n" +
            "  steps = []; renderSteps(); updateCnt();\n" +
            "});\n" +
            "\n" +
            "// ── Step list event delegation ──\n" +
            "document.getElementById('sc').addEventListener('click', function(e) {\n" +
            "  var sh  = e.target.closest('.sh');\n" +
            "  var btn = e.target.closest('button[data-act]');\n" +
            "  if (sh && !btn) {\n" +
            "    var sk = sh.closest('.sk');\n" +
            "    if (sk) { var sb = sk.querySelector('.sb'); if (sb) sb.classList.toggle('open'); }\n" +
            "    return;\n" +
            "  }\n" +
            "  if (!btn) return;\n" +
            "  var act = btn.dataset.act;\n" +
            "  var id  = parseInt(btn.dataset.id);\n" +
            "  if (act === 'del') {\n" +
            "    steps = steps.filter(function(s){ return s.id !== id; });\n" +
            "    renderSteps(); updateCnt();\n" +
            "  } else if (act === 'up' || act === 'dn') {\n" +
            "    var idx = steps.findIndex(function(s){ return s.id === id; });\n" +
            "    var ni = act === 'up' ? idx - 1 : idx + 1;\n" +
            "    if (ni >= 0 && ni < steps.length) { var tmp=steps[idx]; steps[idx]=steps[ni]; steps[ni]=tmp; }\n" +
            "    renderSteps();\n" +
            "    var sk2 = document.getElementById('sk-' + id);\n" +
            "    if (sk2) { var sb2 = sk2.querySelector('.sb'); if (sb2) sb2.classList.add('open'); }\n" +
            "  } else if (act === 'save') {\n" +
            "    // Save step — just collapse and show confirmation\n" +
            "    var sk3 = document.getElementById('sk-' + id);\n" +
            "    if (sk3) {\n" +
            "      var sb3 = sk3.querySelector('.sb');\n" +
            "      if (sb3) sb3.classList.remove('open');\n" +
            "      var saveOk = sk3.querySelector('.save-ok');\n" +
            "      if (saveOk) { saveOk.style.display = 'inline'; setTimeout(function(){ saveOk.style.display='none'; }, 1800); }\n" +
            "    }\n" +
            "    toast('Step saved ✓', 'ok');\n" +
            "  }\n" +
            "});\n" +
            "\n" +
            "// ── Field update (change + input) ──\n" +
            "function bindFieldUpdate(evtName) {\n" +
            "  document.getElementById('sc').addEventListener(evtName, function(e) {\n" +
            "    var el = e.target, id = parseInt(el.dataset.sid), fld = el.dataset.fld;\n" +
            "    if (!id || !fld) return;\n" +
            "    var s = steps.find(function(x){ return x.id === id; });\n" +
            "    if (!s) return;\n" +
            "    s[fld] = el.value;\n" +
            "    if (fld === 'value' && s.action === 'dropdown') {\n" +
            "      var openId = id;\n" +
            "      renderSteps();\n" +
            "      var sb = document.getElementById('sb-' + openId);\n" +
            "      if (sb) sb.classList.add('open');\n" +
            "    }\n" +
            "    // Live preview in header\n" +
            "    var sk = document.getElementById('sk-' + id);\n" +
            "    if (sk) { var ss = sk.querySelector('.ss'); if (ss) ss.textContent = s.xpath || s.value || 'Configure...'; }\n" +
            "  });\n" +
            "}\n" +
            "bindFieldUpdate('change');\n" +
            "bindFieldUpdate('input');\n" +
            "\n" +
            "// ── Render ──\n" +
            "function renderSteps() {\n" +
            "  var c = document.getElementById('sc');\n" +
            "  if (!steps.length) {\n" +
            "    c.innerHTML = \"<div class='es'><div class='ei'>&#x1F4CB;</div><p>No steps yet.<br>Click <b>+ Add Step</b> to get started.</p></div>\";\n" +
            "    return;\n" +
            "  }\n" +
            "  var h = '';\n" +
            "  steps.forEach(function(s, i) {\n" +
            "    var ico = s.status==='pass'?'&#x2705;':s.status==='fail'?'&#x274C;':s.status==='run'?'&#x23F3;':'';\n" +
            "    h += \"<div class='sk \" + s.status + \"' id='sk-\" + s.id + \"'>\";\n" +
            "    h += \"<div class='sh'><div class='sn'>\" + (i+1) + \"</div>\";\n" +
            "    h += \"<div class='sab'>\" + (LABELS[s.action]||s.action) + \"</div>\";\n" +
            "    h += \"<div class='ss'>\" + escH(s.xpath || s.value || 'Configure...') + \"</div>\";\n" +
            "    h += \"<div style='font-size:15px;margin-left:4px;flex-shrink:0;'>\" + ico + \"</div></div>\";\n" +
            "    h += \"<div class='sb' id='sb-\" + s.id + \"'>\" + renderFields(s) + \"</div>\";\n" +
            "    h += \"</div>\";\n" +
            "  });\n" +
            "  c.innerHTML = h;\n" +
            "}\n" +
            "\n" +
            "function escH(str) {\n" +
            "  return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/\"/g,'&quot;');\n" +
            "}\n" +
            "\n" +
            "function inp(sid, fld, val, ph) {\n" +
            "  return \"<input type='text' data-sid='\" + sid + \"' data-fld='\" + fld + \"' value='\" + escH(val) + \"' placeholder='\" + escH(ph) + \"'/>\";\n" +
            "}\n" +
            "function numInp(sid, fld, val, ph) {\n" +
            "  return \"<input type='number' data-sid='\" + sid + \"' data-fld='\" + fld + \"' value='\" + escH(String(val)) + \"' placeholder='\" + escH(ph) + \"'/>\";\n" +
            "}\n" +
            "\n" +
            "function renderFields(s) {\n" +
            "  var h = \"<div class='sf'>\", a = s.action;\n" +
            "  if (a === 'navigate') {\n" +
            "    h += \"<div><label>URL to Navigate</label>\" + inp(s.id,'value',s.value,'https://...') + \"</div>\";\n" +
            "  } else if (a === 'print') {\n" +
            "    h += \"<div><label>Log Message</label>\" + inp(s.id,'value',s.value,'Message to print') + \"</div>\";\n" +
            "  } else if (a === 'wait') {\n" +
            "    h += \"<div><label>Wait Duration (ms)</label>\" + numInp(s.id,'value',s.value||1000,'1000') + \"</div>\";\n" +
            "  } else if (a === 'acceptAlert') {\n" +
            "    h += \"<div><label>Action</label><select data-sid='\" + s.id + \"' data-fld='value'>\";\n" +
            "    h += \"<option value='accept'\" + (s.value!=='dismiss'?' selected':'') + \">Accept (OK)</option>\";\n" +
            "    h += \"<option value='dismiss'\" + (s.value==='dismiss'?' selected':'') + \">Dismiss (Cancel)</option>\";\n" +
            "    h += \"</select></div>\";\n" +
            "  } else if (a === 'screenshot') {\n" +
            "    h += \"<div><label>File Name</label>\" + inp(s.id,'value',s.value,'screenshot_name') + \"</div>\";\n" +
            "  } else if (a === 'switchFrame') {\n" +
            "    h += \"<div><label>Frame (XPath or Index)</label>\" + inp(s.id,'value',s.value,'//iframe  or  0') + \"</div>\";\n" +
            "  } else if (a === 'getAttribute') {\n" +
            "    h += \"<div><label>XPath</label>\" + inp(s.id,'xpath',s.xpath,'//input[@id=\\'x\\']') + \"</div>\";\n" +
            "    h += \"<div><label>Attribute Name</label>\" + inp(s.id,'value',s.value,'value / href / class') + \"</div>\";\n" +
            "  } else if (a === 'verifyText') {\n" +
            "    h += \"<div><label>XPath</label>\" + inp(s.id,'xpath',s.xpath,'//h1') + \"</div>\";\n" +
            "    h += \"<div><label>Expected Text</label>\" + inp(s.id,'value',s.value,'Welcome!') + \"</div>\";\n" +
            "  } else if (a === 'dropdown') {\n" +
            "    h += \"<div><label>XPath</label>\" + inp(s.id,'xpath',s.xpath,'//select[@id=\\'dd\\']') + \"</div>\";\n" +
            "    h += \"<div><label>Select Method</label><select data-sid='\" + s.id + \"' data-fld='value'>\";\n" +
            "    ['selectByVisibleText','selectByIndex','selectByValue'].forEach(function(m) {\n" +
            "      h += \"<option value='\" + m + \"'\" + (s.value===m?' selected':'') + \">\" + m + \"</option>\";\n" +
            "    });\n" +
            "    h += \"</select></div>\";\n" +
            "    h += \"<div><label>\" + (s.value==='selectByIndex'?'Index (0,1,2...)':'Text / Value') + \"</label>\";\n" +
            "    h += inp(s.id,'value2',s.value2,s.value==='selectByIndex'?'0':'Option text') + \"</div>\";\n" +
            "  } else {\n" +
            "    h += \"<div><label>XPath</label>\" + inp(s.id,'xpath',s.xpath,'//button[@id=\\'submit\\']') + \"</div>\";\n" +
            "    if (a === 'type') h += \"<div><label>Text to Type</label>\" + inp(s.id,'value',s.value,'Enter text...') + \"</div>\";\n" +
            "  }\n" +
            "  h += \"</div>\";\n" +
            "  h += \"<div class='war'><label>&#x23F1; Wait After (ms)</label>\" + numInp(s.id,'wait',s.wait,'0') + \"</div>\";\n" +
            "  h += \"<div class='sar'>\";\n" +
            "  h += \"<button class='btn btn-sm btn-grn' data-act='save' data-id='\" + s.id + \"'>&#x2714; Save Step</button>\";\n" +
            "  h += \"<span class='save-ok'>&#x2714; Saved!</span>\";\n" +
            "  h += \"<div style='flex:1;'></div>\";\n" +
            "  h += \"<button class='btn btn-sm' data-act='up' data-id='\" + s.id + \"'>&#x2191;</button>\";\n" +
            "  h += \"<button class='btn btn-sm' data-act='dn' data-id='\" + s.id + \"'>&#x2193;</button>\";\n" +
            "  h += \"<button class='btn btn-sm btn-red' data-act='del' data-id='\" + s.id + \"'>&#x1F5D1;</button>\";\n" +
            "  h += \"</div>\";\n" +
            "  return h;\n" +
            "}\n" +
            "\n" +
            "function updateCnt() {\n" +
            "  document.getElementById('scnt').textContent = steps.length + ' step' + (steps.length!==1?'s':'');\n" +
            "}\n" +
            "\n" +
            "function getCfg() {\n" +
            "  var browsers = ['chrome','firefox','edge'].filter(function(b) {\n" +
            "    return document.getElementById('bc-' + b).classList.contains('on');\n" +
            "  });\n" +
            "  if (!browsers.length) browsers = ['chrome'];\n" +
            "  return {\n" +
            "    url:            document.getElementById('url').value || 'https://example.com',\n" +
            "    browsers:       browsers,\n" +
            "    maximize:       document.getElementById('maximize').checked,\n" +
            "    headless:       document.getElementById('headless').checked,\n" +
            "    resW:           parseInt(document.getElementById('resW').value)   || 1920,\n" +
            "    resH:           parseInt(document.getElementById('resH').value)   || 1080,\n" +
            "    implicitWait:   parseInt(document.getElementById('implWait').value) || 10,\n" +
            "    pageLoad:       parseInt(document.getElementById('pageLoad').value) || 30,\n" +
            "    autoScreenshot: document.getElementById('autoSS').checked,\n" +
            "    screenshotDir:  document.getElementById('ssDir').value || './screenshots',\n" +
            "    captureApi:     document.getElementById('apiCapture').checked,\n" +
            "    steps: steps\n" +
            "  };\n" +
            "}\n" +
            "\n" +
            "// ── Run / Stop ──\n" +
            "document.getElementById('btnRun').addEventListener('click', function() {\n" +
            "  if (!steps.length) { toast('Add some steps first!', 'er'); return; }\n" +
            "  document.getElementById('lp').innerHTML = '';\n" +
            "  document.getElementById('alp').innerHTML = '<div style=\"color:var(--tx3);font-size:11px;padding:14px;text-align:center;\">Capturing...</div>';\n" +
            "  document.getElementById('st').textContent = '0';\n" +
            "  document.getElementById('sp').textContent = '0';\n" +
            "  document.getElementById('sf').textContent = '0';\n" +
            "  document.getElementById('pb').style.width = '0%';\n" +
            "  document.getElementById('apiCnt').textContent = '0';\n" +
            "  steps.forEach(function(s){ s.status='pending'; });\n" +
            "  renderSteps();\n" +
            "  document.getElementById('btnRun').style.display = 'none';\n" +
            "  document.getElementById('btnStop').style.display = 'inline-flex';\n" +
            "  fetch('/run', {\n" +
            "    method:'POST', headers:{'Content-Type':'application/json'},\n" +
            "    body: JSON.stringify(getCfg())\n" +
            "  }).then(function() { startPolling(); })\n" +
            "    .catch(function(e) { toast('Server error: ' + e.message, 'er'); resetButtons(); });\n" +
            "});\n" +
            "\n" +
            "document.getElementById('btnStop').addEventListener('click', function() {\n" +
            "  fetch('/stop',{method:'POST'}).then(function(){ resetButtons(); toast('Stopped','er'); });\n" +
            "});\n" +
            "\n" +
            "function startPolling() {\n" +
            "  if (polling) clearInterval(polling);\n" +
            "  polling = setInterval(pollStatus, 600);\n" +
            "  if (apiPolling) clearInterval(apiPolling);\n" +
            "  apiPolling = setInterval(pollApiLogs, 900);\n" +
            "}\n" +
            "\n" +
            "function pollStatus() {\n" +
            "  fetch('/status')\n" +
            "    .then(function(r){ return r.json(); })\n" +
            "    .then(function(d) {\n" +
            "      document.getElementById('sp').textContent = d.pass;\n" +
            "      document.getElementById('sf').textContent = d.fail;\n" +
            "      document.getElementById('st').textContent = d.pass + d.fail;\n" +
            "      if (d.steps) {\n" +
            "        Object.keys(d.steps).forEach(function(k) {\n" +
            "          var idx = parseInt(k);\n" +
            "          if (steps[idx]) {\n" +
            "            steps[idx].status = d.steps[k];\n" +
            "            var el = document.getElementById('sk-' + steps[idx].id);\n" +
            "            if (el) el.className = 'sk ' + d.steps[k];\n" +
            "          }\n" +
            "        });\n" +
            "      }\n" +
            "      if (d.currentStep >= 0 && steps[d.currentStep]) {\n" +
            "        var cur = steps[d.currentStep];\n" +
            "        var el = document.getElementById('sk-' + cur.id);\n" +
            "        if (el && cur.status === 'pending') el.className = 'sk run';\n" +
            "        document.getElementById('pb').style.width = ((d.pass + d.fail) / steps.length * 100) + '%';\n" +
            "      }\n" +
            "      if (!d.running) {\n" +
            "        clearInterval(polling); polling = null;\n" +
            "        clearInterval(apiPolling); apiPolling = null;\n" +
            "        pollApiLogs(); // final api log fetch\n" +
            "        resetButtons();\n" +
            "      }\n" +
            "    }).catch(function(){});\n" +
            "}\n" +
            "\n" +
            "// ── Exec log polling (always running) ──\n" +
            "setInterval(function() {\n" +
            "  fetch('/logs')\n" +
            "    .then(function(r){ return r.json(); })\n" +
            "    .then(function(logs) {\n" +
            "      var lp = document.getElementById('lp');\n" +
            "      if (!logs.length) return;\n" +
            "      lp.innerHTML = '';\n" +
            "      logs.forEach(function(l) {\n" +
            "        var parts = l.split('|');\n" +
            "        if (parts.length < 3) return;\n" +
            "        var div = document.createElement('div');\n" +
            "        div.className = 'll';\n" +
            "        var msg = parts.slice(2).join('|');\n" +
            "        div.innerHTML = '<span class=\"lt\">' + parts[0] + '</span><span class=\"lm ' + parts[1] + '\">' + escH(msg) + '</span>';\n" +
            "        lp.appendChild(div);\n" +
            "      });\n" +
            "      lp.scrollTop = lp.scrollHeight;\n" +
            "    }).catch(function(){});\n" +
            "}, 700);\n" +
            "\n" +
            "// ── API log polling ──\n" +
            "function pollApiLogs() {\n" +
            "  fetch('/apilogs')\n" +
            "    .then(function(r){ return r.json(); })\n" +
            "    .then(function(logs) {\n" +
            "      document.getElementById('apiCnt').textContent = logs.length;\n" +
            "      var alp = document.getElementById('alp');\n" +
            "      if (!logs.length) return;\n" +
            "      alp.innerHTML = '';\n" +
            "      logs.forEach(function(l) {\n" +
            "        var parts = l.split('|');\n" +
            "        if (parts.length < 3) return;\n" +
            "        var div = document.createElement('div');\n" +
            "        div.className = 'll';\n" +
            "        var msg = parts.slice(2).join('|');\n" +
            "        div.innerHTML = '<span class=\"lt\">' + parts[0] + '</span><span class=\"lm ' + parts[1] + '\">' + escH(msg) + '</span>';\n" +
            "        alp.appendChild(div);\n" +
            "      });\n" +
            "      alp.scrollTop = alp.scrollHeight;\n" +
            "    }).catch(function(){});\n" +
            "}\n" +
            "setInterval(pollApiLogs, 1200);\n" +
            "\n" +
            "function resetButtons() {\n" +
            "  document.getElementById('btnRun').style.display = 'inline-flex';\n" +
            "  document.getElementById('btnStop').style.display = 'none';\n" +
            "}\n" +
            "\n" +
            "// ── Clear logs ──\n" +
            "document.getElementById('btnClearLogs').addEventListener('click', function() {\n" +
            "  fetch('/clearlogs',{method:'POST'}).then(function(){\n" +
            "    document.getElementById('lp').innerHTML = '<div style=\"color:var(--tx3);font-size:11px;padding:14px;text-align:center;\">Cleared.</div>';\n" +
            "    toast('Execution log cleared','ok');\n" +
            "  });\n" +
            "});\n" +
            "\n" +
            "document.getElementById('btnClearApiLogs').addEventListener('click', function() {\n" +
            "  fetch('/clearapilogs',{method:'POST'}).then(function(){\n" +
            "    document.getElementById('alp').innerHTML = '<div style=\"color:var(--tx3);font-size:11px;padding:14px;text-align:center;\">Cleared.</div>';\n" +
            "    document.getElementById('apiCnt').textContent = '0';\n" +
            "    toast('API log cleared','ok');\n" +
            "  });\n" +
            "});\n" +
            "\n" +
            "// ── Export logs ──\n" +
            "document.getElementById('btnExportLogs').addEventListener('click', function() {\n" +
            "  fetch('/logs').then(function(r){return r.json();}).then(function(logs){\n" +
            "    var txt = logs.map(function(l){ return l.replace(/\\|/g,' | '); }).join('\\n');\n" +
            "    downloadFile(txt, 'exec_log_' + Date.now() + '.txt', 'text/plain');\n" +
            "    toast('Exec log exported','ok');\n" +
            "  });\n" +
            "});\n" +
            "\n" +
            "document.getElementById('btnExportApiLogs').addEventListener('click', function() {\n" +
            "  fetch('/apilogs').then(function(r){return r.json();}).then(function(logs){\n" +
            "    var txt = logs.map(function(l){ return l.replace(/\\|/g,' | '); }).join('\\n');\n" +
            "    downloadFile(txt, 'api_log_' + Date.now() + '.txt', 'text/plain');\n" +
            "    toast('API log exported (TXT)','ok');\n" +
            "  });\n" +
            "});\n" +
            "\n" +
            "document.getElementById('btnExportApiJson').addEventListener('click', function() {\n" +
            "  fetch('/apilogs').then(function(r){return r.json();}).then(function(logs){\n" +
            "    var parsed = logs.map(function(l) {\n" +
            "      var parts = l.split('|');\n" +
            "      return {time: parts[0]||'', type: parts[1]||'', message: parts.slice(2).join('|')};\n" +
            "    });\n" +
            "    downloadFile(JSON.stringify(parsed, null, 2), 'api_log_' + Date.now() + '.json', 'application/json');\n" +
            "    toast('API log exported (JSON)','ok');\n" +
            "  });\n" +
            "});\n" +
            "\n" +
            "function downloadFile(content, filename, mime) {\n" +
            "  var a = document.createElement('a');\n" +
            "  a.href = URL.createObjectURL(new Blob([content], {type: mime}));\n" +
            "  a.download = filename;\n" +
            "  a.click();\n" +
            "}\n" +
            "\n" +
            "// ── Code modal ──\n" +
            "document.getElementById('btnCode').addEventListener('click', function() {\n" +
            "  fetch('/gencode',{\n" +
            "    method:'POST', headers:{'Content-Type':'application/json'},\n" +
            "    body: JSON.stringify(getCfg())\n" +
            "  }).then(function(r){return r.text();})\n" +
            "    .then(function(code){\n" +
            "      document.getElementById('codeDisp').textContent = code;\n" +
            "      document.getElementById('codeModal').classList.add('open');\n" +
            "    }).catch(function(){ toast('Could not generate code','er'); });\n" +
            "});\n" +
            "document.getElementById('btnCloseCode').addEventListener('click', function(){ document.getElementById('codeModal').classList.remove('open'); });\n" +
            "document.getElementById('btnCloseCode2').addEventListener('click', function(){ document.getElementById('codeModal').classList.remove('open'); });\n" +
            "document.getElementById('btnCopyCode').addEventListener('click', function() {\n" +
            "  navigator.clipboard.writeText(document.getElementById('codeDisp').textContent);\n" +
            "  toast('Copied! \\uD83D\\uDCCB','ok');\n" +
            "});\n" +
            "\n" +
            "// ── Save / Load project ──\n" +
            "document.getElementById('btnSave').addEventListener('click', function() {\n" +
            "  downloadFile(JSON.stringify(getCfg(), null, 2),\n" +
            "    'selenium_project_' + Date.now() + '.json', 'application/json');\n" +
            "  toast('Project saved!','ok');\n" +
            "});\n" +
            "\n" +
            "document.getElementById('btnLoad').addEventListener('click', function() {\n" +
            "  var inp = document.createElement('input');\n" +
            "  inp.type='file'; inp.accept='.json';\n" +
            "  inp.onchange = function(e) {\n" +
            "    var r = new FileReader();\n" +
            "    r.onload = function(ev) {\n" +
            "      try {\n" +
            "        var d = JSON.parse(ev.target.result);\n" +
            "        if (d.url) document.getElementById('url').value = d.url;\n" +
            "        if (d.browsers) {\n" +
            "          ['chrome','firefox','edge','all'].forEach(function(b){\n" +
            "            var c = document.getElementById('bc-' + b);\n" +
            "            d.browsers.includes(b)?c.classList.add('on'):c.classList.remove('on');\n" +
            "          });\n" +
            "        }\n" +
            "        if (d.maximize    !== undefined) document.getElementById('maximize').checked   = d.maximize;\n" +
            "        if (d.headless    !== undefined) document.getElementById('headless').checked   = d.headless;\n" +
            "        if (d.resW) document.getElementById('resW').value = d.resW;\n" +
            "        if (d.resH) document.getElementById('resH').value = d.resH;\n" +
            "        if (d.implicitWait) document.getElementById('implWait').value = d.implicitWait;\n" +
            "        if (d.pageLoad)    document.getElementById('pageLoad').value = d.pageLoad;\n" +
            "        if (d.autoScreenshot !== undefined) document.getElementById('autoSS').checked = d.autoScreenshot;\n" +
            "        if (d.screenshotDir)  document.getElementById('ssDir').value = d.screenshotDir;\n" +
            "        if (d.captureApi  !== undefined) document.getElementById('apiCapture').checked = d.captureApi;\n" +
            "        if (d.steps) {\n" +
            "          steps = d.steps.map(function(s){ return Object.assign({status:'pending'}, s); });\n" +
            "          ctr = steps.length ? Math.max.apply(null, steps.map(function(s){return s.id;})) : 0;\n" +
            "          renderSteps(); updateCnt();\n" +
            "        }\n" +
            "        toast('Project loaded!','ok');\n" +
            "      } catch(e) { toast('Invalid file!','er'); }\n" +
            "    };\n" +
            "    r.readAsText(e.target.files[0]);\n" +
            "  };\n" +
            "  inp.click();\n" +
            "});\n" +
            "\n" +
            "function toast(msg, type) {\n" +
            "  var t = document.getElementById('toast');\n" +
            "  t.textContent = msg;\n" +
            "  t.className = 'toast show ' + (type||'');\n" +
            "  setTimeout(function(){ t.className = 'toast'; }, 2800);\n" +
            "}\n" +
            "</script>\n" +
            "</body></html>";

        return body;
    }

    // ═══════════════════════════════════════════════════════════
    //  MINIMAL JSON PARSER  (Gson preferred, fallback built-in)
    // ═══════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private static Map<String,Object> parseJson(String json) {
        try {
            Class<?> gsonClass = Class.forName("com.google.gson.Gson");
            Object gson = gsonClass.getDeclaredConstructor().newInstance();
            java.lang.reflect.Method fromJson = gsonClass.getMethod("fromJson", String.class, Class.class);
            return (Map<String,Object>) fromJson.invoke(gson, json, Map.class);
        } catch (Exception e) {
            return simpleJsonParse(json);
        }
    }

    private static Map<String,Object> simpleJsonParse(String json) {
        Map<String,Object> map = new LinkedHashMap<>();
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1, json.length() - 1);
        // browsers array
        int ba = json.indexOf("\"browsers\"");
        if (ba >= 0) {
            int bs = json.indexOf("[", ba), be = json.indexOf("]", bs);
            if (bs >= 0 && be >= 0) {
                List<String> browsers = new ArrayList<>();
                for (String s : json.substring(bs + 1, be).split(","))
                    browsers.add(s.trim().replace("\"", ""));
                map.put("browsers", browsers);
            }
        }
        // steps array
        int sa = json.indexOf("\"steps\"");
        if (sa >= 0) {
            int ss = json.indexOf("[", sa), depth = 0, se = ss;
            for (int i = ss; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '[') depth++; else if (c == ']') { depth--; if (depth == 0) { se = i; break; } }
            }
            map.put("steps", parseStepsArray(json.substring(ss + 1, se)));
        }
        // string fields
        for (String k : new String[]{"url", "screenshotDir"}) {
            int ki = json.indexOf("\"" + k + "\""); if (ki < 0) continue;
            int colon = json.indexOf(":", ki), qs = json.indexOf("\"", colon), qe = json.indexOf("\"", qs + 1);
            if (qs >= 0 && qe >= 0) map.put(k, json.substring(qs + 1, qe));
        }
        // boolean fields
        for (String k : new String[]{"maximize", "headless", "autoScreenshot", "captureApi"}) {
            int ki = json.indexOf("\"" + k + "\""); if (ki < 0) continue;
            map.put(k, json.substring(json.indexOf(":", ki) + 1).trim().startsWith("true"));
        }
        // int fields
        for (String k : new String[]{"implicitWait", "pageLoad", "resW", "resH"}) {
            int ki = json.indexOf("\"" + k + "\""); if (ki < 0) continue;
            String rest = json.substring(json.indexOf(":", ki) + 1).trim();
            StringBuilder num = new StringBuilder();
            for (char c : rest.toCharArray()) { if (Character.isDigit(c)) num.append(c); else if (num.length() > 0) break; }
            if (num.length() > 0) map.put(k, Integer.parseInt(num.toString()));
        }
        return map;
    }

    private static List<Map<String,Object>> parseStepsArray(String json) {
        List<Map<String,Object>> list = new ArrayList<>();
        int i = 0;
        while (i < json.length()) {
            int start = json.indexOf("{", i); if (start < 0) break;
            int depth = 0, end = start;
            for (int j = start; j < json.length(); j++) {
                char c = json.charAt(j);
                if (c == '{') depth++; else if (c == '}') { depth--; if (depth == 0) { end = j; break; } }
            }
            String obj = json.substring(start + 1, end);
            Map<String,Object> step = new LinkedHashMap<>();
            for (String k : new String[]{"action", "xpath", "value", "value2"}) {
                int ki = obj.indexOf("\"" + k + "\""); if (ki < 0) continue;
                int colon = obj.indexOf(":", ki), qs = obj.indexOf("\"", colon), qe = obj.indexOf("\"", qs + 1);
                if (qs >= 0 && qe >= 0) step.put(k, obj.substring(qs + 1, qe));
            }
            int wi = obj.indexOf("\"wait\"");
            if (wi >= 0) {
                String rest = obj.substring(obj.indexOf(":", wi) + 1).trim();
                StringBuilder num = new StringBuilder();
                for (char c : rest.toCharArray()) { if (Character.isDigit(c)) num.append(c); else if (num.length() > 0) break; }
                if (num.length() > 0) step.put("wait", Integer.parseInt(num.toString()));
            }
            list.add(step);
            i = end + 1;
        }
        return list;
    }

    // ── JSON helpers ──
    private static String str(Map<String,Object> m, String k, String def) {
        Object v = m.get(k); return v != null ? v.toString() : def;
    }
    private static boolean bool(Map<String,Object> m, String k, boolean def) {
        Object v = m.get(k);
        if (v instanceof Boolean) return (Boolean) v;
        if (v != null) return "true".equals(v.toString());
        return def;
    }
    private static int num(Map<String,Object> m, String k, int def) {
        Object v = m.get(k);
        if (v instanceof Number) return ((Number) v).intValue();
        try { if (v != null) return Integer.parseInt(v.toString()); } catch (Exception ignored) {}
        return def;
    }
}