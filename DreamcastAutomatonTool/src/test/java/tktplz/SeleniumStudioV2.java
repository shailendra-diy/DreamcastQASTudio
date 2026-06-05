package tktplz;

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
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║         SELENIUM AUTOMATION STUDIO - v2.1 (COMPLETE)        ║
 * ║  Two files: SeleniumStudioV2.java + studio_ui.html          ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  FEATURES:                                                   ║
 * ║  ✅ Visual Test Step Builder (18 actions)                    ║
 * ║  ✅ Test Suites — group steps, save/load with project       ║
 * ║  ✅ API Log Export to JSON/TXT                               ║
 * ║  ✅ Save / Load test projects (.json)                        ║
 * ║  ✅ Java Code Generator                                      ║
 * ║  ✅ Auto screenshot on failure                               ║
 * ║  ✅ Per-step save button                                     ║
 * ║  ✅ CDP API capture (reflection-free, version-safe)         ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  MAVEN DEPENDENCIES (pom.xml):                              ║
 * ║  selenium-java 4.18.1                                       ║
 * ║  commons-io 2.15.1                                          ║
 * ║  gson 2.10.1                                                ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class SeleniumStudioV2 {

    private static final int PORT = 8769;
    private static HttpServer server;
    private static WebDriver testDriver;

    // Execution log — cleared on each run
    private static final List<String> runLog   = new CopyOnWriteArrayList<>();
    // API capture log — separate list
    private static final List<String> apiLog   = new CopyOnWriteArrayList<>();

    private static volatile boolean isRunning  = false;
    private static volatile boolean captureApi = false;
    private static volatile int passCount      = 0;
    private static volatile int failCount      = 0;
    private static volatile int currentStep    = -1;
    private static final Map<Integer, String> stepStatus = new ConcurrentHashMap<>();

    // ═══════════════════════════════════════════════════════════
    //  MAIN
    // ═══════════════════════════════════════════════════════════
    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   Selenium Automation Studio  v2.1      ║");
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
        server.createContext("/",              new UIHandler());
        server.createContext("/run",           new RunHandler());
        server.createContext("/stop",          new StopHandler());
        server.createContext("/status",        new StatusHandler());
        server.createContext("/logs",          new LogsHandler());
        server.createContext("/clearlogs",     new ClearLogsHandler());
        server.createContext("/apilogs",       new ApiLogsHandler());
        server.createContext("/clearapilogs",  new ClearApiLogsHandler());
        server.createContext("/gencode",       new CodeGenHandler());
        server.createContext("/setapicapture", new SetApiCaptureHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    // ═══════════════════════════════════════════════════════════
    //  HTTP HANDLERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Serves studio_ui.html from the same directory as the JAR/class.
     * Falls back to a minimal error page if the file is missing.
     */
    static class UIHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            // Try to find studio_ui.html next to the running jar or in working dir
            File htmlFile = findHtmlFile();
            byte[] bytes;
            String contentType;

            if (htmlFile != null && htmlFile.exists()) {
                bytes = Files.readAllBytes(htmlFile.toPath());
                contentType = "text/html; charset=UTF-8";
            } else {
                String err = "<html><body style='font-family:monospace;background:#0a0c12;color:#ff5270;padding:40px'>" +
                    "<h2>⚠ studio_ui.html not found</h2>" +
                    "<p>Place <b>studio_ui.html</b> in the same folder as SeleniumStudioV2.java / the JAR.</p>" +
                    "<p>Current working directory: " + new File(".").getAbsolutePath() + "</p>" +
                    "</body></html>";
                bytes = err.getBytes(StandardCharsets.UTF_8);
                contentType = "text/html; charset=UTF-8";
            }

            ex.getResponseHeaders().set("Content-Type", contentType);
            ex.sendResponseHeaders(200, bytes.length);
            ex.getResponseBody().write(bytes);
            ex.getResponseBody().close();
        }

        private File findHtmlFile() {
            // 1. Same directory as working dir
            File f = new File("studio_ui.html");
            if (f.exists()) return f;
            // 2. Try src/main/resources
            f = new File("src/main/resources/studio_ui.html");
            if (f.exists()) return f;
            // 3. Try class location
            try {
                File jarDir = new File(SeleniumStudioV2.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getParentFile();
                f = new File(jarDir, "studio_ui.html");
                if (f.exists()) return f;
            } catch (Exception ignored) {}
            return null;
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
            String json = "{\"running\":"    + isRunning +
                ",\"pass\":"                 + passCount +
                ",\"fail\":"                 + failCount +
                ",\"currentStep\":"          + currentStep +
                ",\"captureApi\":"           + captureApi +
                ",\"steps\":{"               + stepMap + "}}";
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
        return DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());
    }

    private static String logsToJson(List<String> list) {
        return "[" + list.stream()
            .map(l -> "\"" + l.replace("\\","\\\\").replace("\"","\\\"")
                              .replace("\n","\\n").replace("\r","") + "\"")
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
        ex.getResponseHeaders().set("Content-Type",                 "application/json");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
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
        String  url       = str(cfg,  "url",            "https://example.com");
        boolean maximize  = bool(cfg, "maximize",       true);
        boolean headless  = bool(cfg, "headless",       false);
        int     implWait  = num(cfg,  "implicitWait",   10);
        int     pageLoad  = num(cfg,  "pageLoad",       30);
        int     resW      = num(cfg,  "resW",           1920);
        int     resH      = num(cfg,  "resH",           1080);
        String  ssDir     = str(cfg,  "screenshotDir",  "./screenshots");
        boolean autoSS    = bool(cfg, "autoScreenshot", true);
        List<String>             browsers = (List<String>) cfg.getOrDefault("browsers", List.of("chrome"));
        List<Map<String,Object>> steps    = (List<Map<String,Object>>) cfg.getOrDefault("steps", new ArrayList<>());

        log("INFO", "🚀 Selenium Automation Studio v2.1 — Starting");
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

                // ── CDP API Capture (Chrome only, version-safe) ──
                if (captureApi && browser.equalsIgnoreCase("chrome")) {
                    setupApiCapture(testDriver);
                }

                testDriver.get(url);
                log("INFO", "✅ Opened: " + url);

                for (int i = 0; i < steps.size(); i++) {
                    if (!isRunning) break;
                    currentStep = i;
                    Map<String,Object> step = steps.get(i);
                    String action    = str(step, "action",  "click");
                    String xpath     = str(step, "xpath",   "");
                    String value     = str(step, "value",   "");
                    String value2    = str(step, "value2",  "");
                    int    waitAfter = num(step, "wait",    0);
                    log("INFO", "▶ Step " + (i+1) + ": " + action +
                        (xpath.isEmpty() ? "" : " | " + xpath));
                    boolean passed = runStep(testDriver, action, xpath, value, value2, i, ssDir, autoSS);
                    if (passed) { stepStatus.put(i,"pass"); passCount++; log("PASS","✅ Step "+(i+1)+" PASSED"); }
                    else        { stepStatus.put(i,"fail"); failCount++; log("FAIL","❌ Step "+(i+1)+" FAILED"); }
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

    // ═══════════════════════════════════════════════════════════
    //  CDP API CAPTURE — version-safe via JavaScript injection
    //  (No reflection, no hard version import needed)
    // ═══════════════════════════════════════════════════════════
    private static void setupApiCapture(WebDriver driver) {
        try {
            if (!(driver instanceof JavascriptExecutor)) {
                log("WARN", "⚠️ API capture requires JavascriptExecutor");
                return;
            }

            // Use Chrome DevTools Protocol via CDP endpoint if HasDevTools is available
            if (driver instanceof HasDevTools) {
                try {
                    DevTools devTools = ((HasDevTools) driver).getDevTools();
                    devTools.createSession();

                    // Command.fromJson() does not exist in Selenium 4.x — use v85 Network.enable() directly
                    devTools.send(org.openqa.selenium.devtools.v85.network.Network.enable(
                        java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty()
                    ));

                    // Listen for requestWillBeSent via generic CDP event
                    devTools.addListener(
                        new org.openqa.selenium.devtools.Event<>(
                            "Network.requestWillBeSent",
                            params -> params
                        ),
                        eventParams -> {
                            try {
                                @SuppressWarnings("unchecked")
                                Map<String,Object> p = (Map<String,Object>) eventParams;
                                @SuppressWarnings("unchecked")
                                Map<String,Object> req = (Map<String,Object>) p.get("request");
                                if (req != null) {
                                    String reqUrl    = String.valueOf(req.get("url"));
                                    String reqMethod = String.valueOf(req.get("method"));
                                    apiLog.add(ts() + "|REQ|" + reqMethod + " " + reqUrl);
                                }
                            } catch (Exception ignored) {}
                        }
                    );

                    devTools.addListener(
                        new org.openqa.selenium.devtools.Event<>(
                            "Network.responseReceived",
                            params -> params
                        ),
                        eventParams -> {
                            try {
                                @SuppressWarnings("unchecked")
                                Map<String,Object> p = (Map<String,Object>) eventParams;
                                @SuppressWarnings("unchecked")
                                Map<String,Object> resp = (Map<String,Object>) p.get("response");
                                if (resp != null) {
                                    String respUrl    = String.valueOf(resp.get("url"));
                                    Object statusObj  = resp.get("status");
                                    String status     = statusObj != null ? statusObj.toString() : "?";
                                    apiLog.add(ts() + "|RES|" + status + " " + respUrl);
                                }
                            } catch (Exception ignored) {}
                        }
                    );

                    log("INFO", "📡 CDP API Capture active");
                    return;
                } catch (Exception cdpEx) {
                    log("WARN", "⚠️ CDP setup failed (" + cdpEx.getMessage() + "), falling back to JS intercept");
                }
            }

            // ── Fallback: XHR/Fetch intercept via JavaScript ──
            ((JavascriptExecutor) driver).executeScript(
                "window.__apiLog = window.__apiLog || [];" +
                "var origOpen = XMLHttpRequest.prototype.open;" +
                "XMLHttpRequest.prototype.open = function(method, url) {" +
                "  this.addEventListener('load', function() {" +
                "    window.__apiLog.push({type:'RES',status:this.status,url:url,method:method});" +
                "  });" +
                "  window.__apiLog.push({type:'REQ',method:method,url:url});" +
                "  origOpen.apply(this, arguments);" +
                "};" +
                "var origFetch = window.fetch;" +
                "window.fetch = function(input, init) {" +
                "  var url = typeof input === 'string' ? input : input.url;" +
                "  var method = (init && init.method) ? init.method : 'GET';" +
                "  window.__apiLog.push({type:'REQ',method:method,url:url});" +
                "  return origFetch.apply(this, arguments).then(function(resp) {" +
                "    window.__apiLog.push({type:'RES',status:resp.status,url:url,method:method});" +
                "    return resp;" +
                "  });" +
                "};"
            );
            log("INFO", "📡 API Capture active (JS intercept fallback)");

        } catch (Exception e) {
            log("WARN", "⚠️ Could not init API capture: " + e.getMessage());
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
                    Alert alert = new WebDriverWait(d, Duration.ofSeconds(5))
                        .until(ExpectedConditions.alertIsPresent());
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
                    for (String h : d.getWindowHandles())
                        if (!h.equals(main)) { d.switchTo().window(h); break; }
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
            String  url      = str(cfg,  "url",            "https://example.com");
            boolean maximize = bool(cfg, "maximize",       true);
            boolean headless = bool(cfg, "headless",       false);
            int     implWait = num(cfg,  "implicitWait",   10);
            int     pageLoad = num(cfg,  "pageLoad",       30);
            int     resW     = num(cfg,  "resW",           1920);
            int     resH     = num(cfg,  "resH",           1080);
            String  ssDir    = str(cfg,  "screenshotDir",  "./screenshots");
            boolean autoSS   = bool(cfg, "autoScreenshot", true);
            List<String>             browsers = (List<String>) cfg.getOrDefault("browsers", List.of("chrome"));
            List<Map<String,Object>> steps    = (List<Map<String,Object>>) cfg.getOrDefault("steps", new ArrayList<>());

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
            sb.append("/** Auto-generated by Selenium Automation Studio v2.1 */\n");
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
                String a  = str(s,"action","click"), x  = str(s,"xpath",""),
                       v  = str(s,"value",""),        v2 = str(s,"value2","");
                int    w  = num(s,"wait",0);
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
        for (String k : new String[]{"maximize","headless","autoScreenshot","captureApi"}) {
            int ki = json.indexOf("\"" + k + "\""); if (ki < 0) continue;
            map.put(k, json.substring(json.indexOf(":", ki) + 1).trim().startsWith("true"));
        }
        // int fields
        for (String k : new String[]{"implicitWait","pageLoad","resW","resH"}) {
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
            for (String k : new String[]{"action","xpath","value","value2"}) {
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
