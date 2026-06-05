package com.selenium.studio;

import com.sun.net.httpserver.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.edge.*;
import org.openqa.selenium.support.ui.*;
import org.openqa.selenium.interactions.*;
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
 * ╔══════════════════════════════════════════════════════════╗
 * ║         SELENIUM AUTOMATION STUDIO - v1.1 (FIXED)      ║
 * ║  Single file - Run this class, browser opens auto       ║
 * ╠══════════════════════════════════════════════════════════╣
 * ║  HOW TO RUN:                                             ║
 * ║  1. Add to Eclipse as SeleniumStudio.java               ║
 * ║  2. Add Maven dependencies (see below)                  ║
 * ║  3. Run as Java Application                             ║
 * ║  4. Browser opens automatically with the Studio UI      ║
 * ╠══════════════════════════════════════════════════════════╣
 * ║  MAVEN DEPENDENCIES (pom.xml):                          ║
 * ║  <dependency>                                           ║
 * ║    <groupId>org.seleniumhq.selenium</groupId>           ║
 * ║    <artifactId>selenium-java</artifactId>               ║
 * ║    <version>4.18.1</version>                            ║
 * ║  </dependency>                                          ║
 * ║  <dependency>                                           ║
 * ║    <groupId>commons-io</groupId>                        ║
 * ║    <artifactId>commons-io</artifactId>                  ║
 * ║    <version>2.15.1</version>                            ║
 * ║  </dependency>                                          ║
 * ║  <dependency>                                           ║
 * ║    <groupId>com.google.code.gson</groupId>              ║
 * ║    <artifactId>gson</artifactId>                        ║
 * ║    <version>2.10.1</version>                            ║
 * ║  </dependency>                                          ║
 * ╚══════════════════════════════════════════════════════════╝
 */
public class SeleniumStudio {

    private static final int PORT = 8769;
    private static HttpServer server;
    private static WebDriver testDriver;
    private static final List<String> runLog = new CopyOnWriteArrayList<>();
    private static volatile boolean isRunning = false;
    private static volatile int passCount = 0;
    private static volatile int failCount = 0;
    private static volatile int currentStep = -1;
    private static final Map<Integer, String> stepStatus = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   Selenium Automation Studio v1.1   ║");
        System.out.println("╚══════════════════════════════════════╝");
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
            if (os.contains("win"))       Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            else if (os.contains("mac"))  Runtime.getRuntime().exec(new String[]{"open", url});
            else                          Runtime.getRuntime().exec(new String[]{"xdg-open", url});
        } catch (Exception e) {
            System.out.println("Could not auto-open browser. Please visit: " + url);
        }
    }

    private static void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/",         new UIHandler());
        server.createContext("/run",      new RunHandler());
        server.createContext("/stop",     new StopHandler());
        server.createContext("/status",   new StatusHandler());
        server.createContext("/logs",     new LogsHandler());
        server.createContext("/gencode",  new CodeGenHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

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
                catch (Exception e) { runLog.add("ERROR: " + e.getMessage()); isRunning = false; }
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
            String json = "{\"running\":" + isRunning + ",\"pass\":" + passCount +
                ",\"fail\":" + failCount + ",\"currentStep\":" + currentStep +
                ",\"steps\":{" + stepMap + "}}";
            respond(ex, 200, json);
        }
    }

    static class LogsHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (handleCors(ex)) return;
            String logs = runLog.stream()
                .map(l -> "\"" + l.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n") + "\"")
                .collect(Collectors.joining(","));
            respond(ex, 200, "[" + logs + "]");
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

    // Handle CORS preflight — returns true if it was an OPTIONS request (caller should return)
    private static boolean handleCors(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
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
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    @SuppressWarnings("unchecked")
    private static void executeTests(String jsonBody) throws Exception {
        isRunning = true; passCount = 0; failCount = 0; currentStep = -1;
        runLog.clear(); stepStatus.clear();
        Map<String,Object> cfg = parseJson(jsonBody);
        String url       = str(cfg,"url","https://example.com");
        boolean maximize = bool(cfg,"maximize",true);
        boolean headless = bool(cfg,"headless",false);
        int implWait     = num(cfg,"implicitWait",10);
        int pageLoad     = num(cfg,"pageLoad",30);
        int resW         = num(cfg,"resW",1920);
        int resH         = num(cfg,"resH",1080);
        String ssDir     = str(cfg,"screenshotDir","./screenshots");
        boolean autoSS   = bool(cfg,"autoScreenshot",true);
        List<String> browsers = (List<String>) cfg.getOrDefault("browsers", List.of("chrome"));
        List<Map<String,Object>> steps = (List<Map<String,Object>>) cfg.getOrDefault("steps", new ArrayList<>());

        log("INFO", "🚀 Starting Selenium Automation Studio");
        log("INFO", "📋 Steps: " + steps.size() + "  |  Browsers: " + browsers);
        log("INFO", "🌐 URL: " + url);

        for (String browser : browsers) {
            if (!isRunning) break;
            log("INFO", "\n════════ Browser: " + browser.toUpperCase() + " ════════");
            try {
                testDriver = createDriver(browser, headless);
                testDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implWait));
                testDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoad));
                if (maximize) testDriver.manage().window().maximize();
                else testDriver.manage().window().setSize(new Dimension(resW, resH));
                testDriver.get(url);
                log("INFO", "✅ Opened: " + url);
                for (int i = 0; i < steps.size(); i++) {
                    if (!isRunning) break;
                    currentStep = i;
                    Map<String,Object> step = steps.get(i);
                    String action = str(step,"action","click");
                    String xpath  = str(step,"xpath","");
                    String value  = str(step,"value","");
                    String value2 = str(step,"value2","");
                    int waitAfter = num(step,"wait",0);
                    log("INFO", "▶ Step " + (i+1) + ": " + action + (xpath.isEmpty()?"":" | "+xpath));
                    boolean passed = runStep(testDriver, action, xpath, value, value2, i, ssDir, autoSS);
                    if (passed) { stepStatus.put(i, "pass"); passCount++; log("PASS", "✅ Step " + (i+1) + " PASSED"); }
                    else        { stepStatus.put(i, "fail"); failCount++; log("FAIL", "❌ Step " + (i+1) + " FAILED"); }
                    if (waitAfter > 0) Thread.sleep(waitAfter);
                }
            } catch (Exception e) {
                log("FAIL", "💥 Browser setup failed: " + e.getMessage());
            } finally {
                if (testDriver != null) { try { testDriver.quit(); } catch (Exception ignored) {} testDriver = null; }
            }
        }
        currentStep = -1; isRunning = false;
        log("INFO", "\n═══════════════════════════════");
        log(failCount == 0 ? "PASS" : "FAIL", "🏁 DONE — PASS: " + passCount + "  FAIL: " + failCount);
        log("INFO", "═══════════════════════════════");
    }

    private static WebDriver createDriver(String browser, boolean headless) {
        switch (browser.toLowerCase()) {
            case "firefox": { FirefoxOptions o = new FirefoxOptions(); if (headless) o.addArguments("-headless"); return new FirefoxDriver(o); }
            case "edge":    { EdgeOptions o = new EdgeOptions(); if (headless) o.addArguments("--headless","--no-sandbox"); return new EdgeDriver(o); }
            default:        { ChromeOptions o = new ChromeOptions(); if (headless) o.addArguments("--headless","--no-sandbox","--disable-dev-shm-usage"); return new ChromeDriver(o); }
        }
    }

    private static boolean runStep(WebDriver d, String action, String xpath,
                                    String value, String value2, int idx,
                                    String ssDir, boolean autoSS) {
        try {
            WebDriverWait wait = new WebDriverWait(d, Duration.ofSeconds(15));
            switch (action) {
                case "click":         wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath))).click(); break;
                case "type":          wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).sendKeys(value); break;
                case "clearField":    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).clear(); break;
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
                    log("INFO", "   📖 Text: " + text); break;
                }
                case "getAttribute": {
                    String attr = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).getAttribute(value);
                    log("INFO", "   🏷 Attribute [" + value + "]: " + attr); break;
                }
                case "verifyText": {
                    String actual = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).getText();
                    if (!actual.equals(value)) throw new AssertionError("Expected: '" + value + "' Got: '" + actual + "'");
                    log("INFO", "   ✔ Text verified: " + actual); break;
                }
                case "verifyElement":
                    if (!wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).isDisplayed())
                        throw new AssertionError("Element not visible");
                    break;
                case "navigate":     d.get(value); break;
                case "screenshot":   takeScreenshot(d, value.isEmpty() ? "step_" + (idx+1) : value, ssDir); break;
                case "scrollTo": {
                    WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
                    ((JavascriptExecutor) d).executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", el); break;
                }
                case "hover": {
                    WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
                    new Actions(d).moveToElement(el).perform(); break;
                }
                case "jsClick": {
                    WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
                    ((JavascriptExecutor) d).executeScript("arguments[0].click();", el); break;
                }
                case "acceptAlert": {
                    Alert alert = new WebDriverWait(d, Duration.ofSeconds(5)).until(ExpectedConditions.alertIsPresent());
                    if ("dismiss".equals(value)) alert.dismiss(); else alert.accept(); break;
                }
                case "switchFrame": {
                    try { d.switchTo().frame(Integer.parseInt(value)); }
                    catch (NumberFormatException e) { d.switchTo().frame(wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(value)))); }
                    break;
                }
                case "switchWindow": {
                    String main = d.getWindowHandle();
                    for (String h : d.getWindowHandles()) if (!h.equals(main)) { d.switchTo().window(h); break; }
                    break;
                }
                case "print":  log("INFO", "   🖨 " + value); break;
                case "wait":   Thread.sleep(Long.parseLong(value.isEmpty() ? "1000" : value)); break;
                default:       log("INFO", "   ⚠ Unknown action: " + action);
            }
            return true;
        } catch (Exception e) {
            log("FAIL", "   ⚠ " + e.getMessage());
            if (autoSS) takeScreenshot(d, "FAIL_step" + (idx+1), ssDir);
            return false;
        }
    }

    private static void takeScreenshot(WebDriver d, String name, String dir) {
        try {
            new File(dir).mkdirs();
            String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
            File src  = ((TakesScreenshot) d).getScreenshotAs(OutputType.FILE);
            File dest = new File(dir + "/" + name + "_" + ts + ".png");
            FileUtils.copyFile(src, dest);
            log("INFO", "   📸 Screenshot: " + dest.getAbsolutePath());
        } catch (Exception e) { log("FAIL", "   Screenshot error: " + e.getMessage()); }
    }

    private static void log(String type, String msg) {
        String ts = DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());
        runLog.add(ts + "|" + type + "|" + msg);
        System.out.println("[" + type + "] " + msg);
    }

    // ═══════════════════════════════════════════════════════════
    //  Java Code Generator
    // ═══════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private static String generateJavaCode(String jsonBody) {
        try {
            Map<String,Object> cfg = parseJson(jsonBody);
            String url       = str(cfg,"url","https://example.com");
            boolean maximize = bool(cfg,"maximize",true);
            boolean headless = bool(cfg,"headless",false);
            int implWait     = num(cfg,"implicitWait",10);
            int pageLoad     = num(cfg,"pageLoad",30);
            int resW         = num(cfg,"resW",1920);
            int resH         = num(cfg,"resH",1080);
            String ssDir     = str(cfg,"screenshotDir","./screenshots");
            boolean autoSS   = bool(cfg,"autoScreenshot",true);
            List<String> browsers = (List<String>) cfg.getOrDefault("browsers", List.of("chrome"));
            List<Map<String,Object>> steps = (List<Map<String,Object>>) cfg.getOrDefault("steps", new ArrayList<>());

            StringBuilder sb = new StringBuilder();
            sb.append("package com.automation.studio;\n\n");
            sb.append("import org.openqa.selenium.*;\nimport org.openqa.selenium.chrome.*;\n");
            sb.append("import org.openqa.selenium.firefox.*;\nimport org.openqa.selenium.edge.*;\n");
            sb.append("import org.openqa.selenium.support.ui.*;\nimport org.openqa.selenium.interactions.*;\n");
            sb.append("import org.apache.commons.io.FileUtils;\n");
            sb.append("import java.io.*;\nimport java.time.*;\nimport java.time.format.*;\n\n");
            sb.append("public class GeneratedTest {\n\n");
            sb.append("    WebDriver driver;\n    int pass = 0, fail = 0;\n");
            sb.append("    String ssDir = \"").append(ssDir).append("\";\n\n");
            sb.append("    public static void main(String[] args) throws Exception {\n");
            sb.append("        new GeneratedTest().execute();\n    }\n\n");
            sb.append("    void execute() throws Exception {\n");
            for (String br : browsers) {
                sb.append("        System.out.println(\"===== ").append(br.toUpperCase()).append(" =====\");\n");
                sb.append("        try {\n");
                if ("chrome".equals(br)) {
                    sb.append("            ChromeOptions o = new ChromeOptions();\n");
                    if (headless) sb.append("            o.addArguments(\"--headless\",\"--no-sandbox\");\n");
                    sb.append("            driver = new ChromeDriver(o);\n");
                } else if ("firefox".equals(br)) {
                    sb.append("            FirefoxOptions o = new FirefoxOptions();\n");
                    if (headless) sb.append("            o.addArguments(\"-headless\");\n");
                    sb.append("            driver = new FirefoxDriver(o);\n");
                } else {
                    sb.append("            EdgeOptions o = new EdgeOptions();\n");
                    if (headless) sb.append("            o.addArguments(\"--headless\");\n");
                    sb.append("            driver = new EdgeDriver(o);\n");
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
                String a = str(s,"action","click"), x = str(s,"xpath",""), v = str(s,"value",""), v2 = str(s,"value2","");
                int w = num(s,"wait",0);
                sb.append("        // Step ").append(i+1).append(": ").append(a).append("\n");
                sb.append("        step(\"Step ").append(i+1).append("\", () -> {\n");
                switch (a) {
                    case "click":        sb.append("            driver.findElement(By.xpath(\"").append(x).append("\")).click();\n"); break;
                    case "type":         sb.append("            driver.findElement(By.xpath(\"").append(x).append("\")).sendKeys(\"").append(v).append("\");\n"); break;
                    case "clearField":   sb.append("            driver.findElement(By.xpath(\"").append(x).append("\")).clear();\n"); break;
                    case "dropdown":     sb.append("            new Select(driver.findElement(By.xpath(\"").append(x).append("\"))).").append(v.isEmpty()?"selectByVisibleText":v).append("(\"").append(v2).append("\");\n"); break;
                    case "getText":      sb.append("            System.out.println(driver.findElement(By.xpath(\"").append(x).append("\")).getText());\n"); break;
                    case "getAttribute": sb.append("            System.out.println(driver.findElement(By.xpath(\"").append(x).append("\")).getAttribute(\"").append(v).append("\"));\n"); break;
                    case "verifyText":   sb.append("            String t=driver.findElement(By.xpath(\"").append(x).append("\")).getText();if(!t.equals(\"").append(v).append("\"))throw new AssertionError(\"Expected:").append(v).append(" Got:\"+t);\n"); break;
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
            sb.append("    }\n\n");
            sb.append("    void step(String name, RunnableStep r) {\n");
            sb.append("        System.out.print(\"[RUN] \"+name+\" ... \");\n");
            sb.append("        try { r.run(); pass++; System.out.println(\"PASS\"); }\n");
            sb.append("        catch(Exception e) { fail++; System.out.println(\"FAIL: \"+e.getMessage());\n");
            if (autoSS) sb.append("            screenshot(\"FAIL_\"+name.replaceAll(\"[^a-zA-Z0-9]\",\"_\")); }\n");
            else sb.append("        }\n");
            sb.append("    }\n\n");
            sb.append("    void screenshot(String name) {\n");
            sb.append("        try { new File(ssDir).mkdirs();\n");
            sb.append("            String ts=DateTimeFormatter.ofPattern(\"yyyyMMdd_HHmmss\").format(LocalDateTime.now());\n");
            sb.append("            FileUtils.copyFile(((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE),new File(ssDir+\"/\"+name+\"_\"+ts+\".png\"));\n");
            sb.append("        } catch(Exception e){System.err.println(\"SS Error: \"+e.getMessage());}\n    }\n\n");
            sb.append("    @FunctionalInterface interface RunnableStep { void run() throws Exception; }\n}\n");
            return sb.toString();
        } catch (Exception e) { return "// Code generation error: " + e.getMessage(); }
    }

    // ═══════════════════════════════════════════════════════════
    //  BUILD HTML  ── KEY FIX: JS in a text block, no inline escaping hell
    // ═══════════════════════════════════════════════════════════
    private static String buildHTML() {
        // NOTE: JS is written in a separate string with proper escaping.
        // All onclick handlers use data attributes + event delegation to avoid
        // quote-inside-quote escaping nightmares in Java string concatenation.

        String css = "<style>" +
            ":root{--bg:#0f1117;--s1:#1a1d27;--s2:#222535;--s3:#2a2e42;--bd:#2e3347;--bd2:#3d4260;" +
            "--acc:#4f8ef7;--acc2:#7c5ce4;--grn:#00c896;--red:#ff5c5c;--yel:#ffc93c;" +
            "--tx:#e8eaf0;--tx2:#9ca3b8;--tx3:#636880;--mo:'JetBrains Mono',monospace;--sa:'DM Sans',sans-serif;}" +
            "*{margin:0;padding:0;box-sizing:border-box;}" +
            "body{font-family:var(--sa);background:var(--bg);color:var(--tx);height:100vh;display:flex;flex-direction:column;overflow:hidden;}" +
            ".hdr{background:var(--s1);border-bottom:1px solid var(--bd);padding:0 20px;height:56px;display:flex;align-items:center;justify-content:space-between;flex-shrink:0;}" +
            ".logo{display:flex;align-items:center;gap:10px;}" +
            ".logo-icon{width:30px;height:30px;background:linear-gradient(135deg,var(--acc),var(--acc2));border-radius:7px;display:flex;align-items:center;justify-content:center;font-size:15px;}" +
            ".logo-txt{font-size:16px;font-weight:600;}" +
            ".logo-ver{font-size:10px;background:var(--s3);color:var(--acc);padding:2px 8px;border-radius:20px;font-family:var(--mo);}" +
            ".ha{display:flex;gap:8px;}" +
            ".btn{padding:7px 14px;border-radius:7px;border:1px solid var(--bd2);background:transparent;color:var(--tx);font-family:var(--sa);font-size:12px;font-weight:500;cursor:pointer;transition:all .15s;display:inline-flex;align-items:center;gap:5px;}" +
            ".btn:hover{background:var(--s2);border-color:var(--acc);color:var(--acc);}" +
            ".btn-run{background:var(--grn);border-color:var(--grn);color:#000;font-weight:600;}" +
            ".btn-run:hover{background:#00b585;border-color:#00b585;color:#000;}" +
            ".btn-stop{background:var(--red);border-color:var(--red);color:#fff;display:none;}" +
            ".btn-stop:hover{background:#e04444;}" +
            ".layout{display:flex;flex:1;overflow:hidden;}" +
            ".sidebar{width:280px;min-width:280px;background:var(--s1);border-right:1px solid var(--bd);overflow-y:auto;display:flex;flex-direction:column;}" +
            ".main{flex:1;display:flex;flex-direction:column;overflow:hidden;}" +
            ".rp{width:340px;min-width:340px;background:var(--s1);border-left:1px solid var(--bd);display:flex;flex-direction:column;}" +
            ".ph{padding:12px 14px;border-bottom:1px solid var(--bd);display:flex;align-items:center;justify-content:space-between;}" +
            ".pt{font-size:11px;font-weight:600;text-transform:uppercase;letter-spacing:.8px;color:var(--tx2);}" +
            ".sec{padding:14px;border-bottom:1px solid var(--bd);}" +
            ".sl{font-size:10px;font-weight:600;text-transform:uppercase;letter-spacing:.7px;color:var(--tx3);margin-bottom:9px;}" +
            ".fl{margin-bottom:10px;}" +
            ".fl label{font-size:11px;color:var(--tx2);display:block;margin-bottom:4px;font-weight:500;}" +
            "input[type=text],input[type=number],select,textarea{width:100%;background:var(--bg);border:1px solid var(--bd2);color:var(--tx);border-radius:6px;padding:7px 9px;font-family:var(--sa);font-size:12px;outline:none;transition:border .15s;}" +
            "input:focus,select:focus{border-color:var(--acc);}" +
            "select option{background:var(--s2);}" +
            ".bg{display:grid;grid-template-columns:1fr 1fr;gap:7px;}" +
            ".bc{background:var(--bg);border:1px solid var(--bd2);border-radius:6px;padding:9px;cursor:pointer;transition:all .15s;display:flex;align-items:center;gap:7px;user-select:none;}" +
            ".bc:hover{border-color:var(--acc);}" +
            ".bc.on{border-color:var(--acc);background:rgba(79,142,247,.08);}" +
            ".bi{font-size:18px;}" +
            ".bn{font-size:12px;font-weight:500;}" +
            ".cd{width:14px;height:14px;border-radius:50%;border:2px solid var(--bd2);margin-left:auto;transition:all .15s;flex-shrink:0;}" +
            ".bc.on .cd{background:var(--acc);border-color:var(--acc);}" +
            ".tr{display:flex;align-items:center;justify-content:space-between;margin-bottom:9px;}" +
            ".tl{font-size:12px;color:var(--tx2);}" +
            ".tg{position:relative;width:34px;height:18px;}" +
            ".tg input{display:none;}" +
            ".ts{position:absolute;inset:0;background:var(--bd2);border-radius:20px;cursor:pointer;transition:.2s;}" +
            ".ts::after{content:'';position:absolute;width:12px;height:12px;left:3px;top:3px;background:#fff;border-radius:50%;transition:.2s;}" +
            ".tg input:checked+.ts{background:var(--acc);}" +
            ".tg input:checked+.ts::after{transform:translateX(16px);}" +
            ".rr{display:flex;gap:7px;align-items:center;}" +
            ".rr span{color:var(--tx3);font-size:12px;}" +
            ".rr input{width:75px;}" +
            ".r2{display:grid;grid-template-columns:1fr 1fr;gap:7px;}" +
            ".stb{padding:10px 14px;border-bottom:1px solid var(--bd);display:flex;align-items:center;justify-content:space-between;background:var(--s1);}" +
            ".sc{flex:1;overflow-y:auto;padding:10px;background:var(--bg);}" +
            ".sk{background:var(--s1);border:1px solid var(--bd);border-radius:9px;margin-bottom:9px;overflow:hidden;transition:border .2s;}" +
            ".sk:hover{border-color:var(--bd2);}" +
            ".sk.pass{border-left:3px solid var(--grn);}" +
            ".sk.fail{border-left:3px solid var(--red);}" +
            ".sk.run{border-left:3px solid var(--yel);animation:pulse 1s infinite;}" +
            "@keyframes pulse{0%,100%{opacity:1}50%{opacity:.7}}" +
            ".sh{padding:9px 12px;display:flex;align-items:center;gap:9px;cursor:pointer;}" +
            ".sn{width:22px;height:22px;border-radius:5px;background:var(--s3);display:flex;align-items:center;justify-content:center;font-size:10px;font-weight:600;font-family:var(--mo);color:var(--tx2);flex-shrink:0;}" +
            ".sk.pass .sn{background:rgba(0,200,150,.15);color:var(--grn);}" +
            ".sk.fail .sn{background:rgba(255,92,92,.15);color:var(--red);}" +
            ".sk.run .sn{background:rgba(255,201,60,.15);color:var(--yel);}" +
            ".sab{font-size:10px;font-weight:600;font-family:var(--mo);padding:2px 7px;border-radius:4px;background:rgba(79,142,247,.15);color:var(--acc);flex-shrink:0;}" +
            ".ss{flex:1;font-size:12px;color:var(--tx2);overflow:hidden;text-overflow:ellipsis;white-space:nowrap;}" +
            ".sb{padding:10px 12px;border-top:1px solid var(--bd);background:var(--bg);display:none;}" +
            ".sb.open{display:block;}" +
            ".sf{display:grid;gap:8px;}" +
            ".war{display:flex;align-items:center;gap:7px;margin-top:7px;}" +
            ".war label{font-size:11px;color:var(--tx3);white-space:nowrap;}" +
            ".war input{width:75px;}" +
            ".sar{display:flex;gap:7px;margin-top:8px;}" +
            ".asa{padding:10px 14px;border-top:1px solid var(--bd);background:var(--s1);display:none;}" +
            ".ag{display:grid;grid-template-columns:repeat(3,1fr);gap:7px;margin-top:8px;}" +
            ".at{background:var(--bg);border:1px solid var(--bd);border-radius:6px;padding:9px 7px;text-align:center;cursor:pointer;transition:all .15s;}" +
            ".at:hover{border-color:var(--acc);background:rgba(79,142,247,.08);}" +
            ".at .ic{font-size:18px;margin-bottom:3px;}" +
            ".at .nm{font-size:10px;font-weight:500;color:var(--tx2);line-height:1.3;}" +
            ".lp{flex:1;overflow-y:auto;padding:10px;font-family:var(--mo);font-size:11px;}" +
            ".ll{padding:2px 0;display:flex;gap:8px;border-bottom:1px solid rgba(46,51,71,.4);}" +
            ".lt{color:var(--tx3);flex-shrink:0;}" +
            ".lm{word-break:break-all;}" +
            ".lm.INFO{color:var(--tx2);}" +
            ".lm.PASS{color:var(--grn);}" +
            ".lm.FAIL{color:var(--red);}" +
            ".lm.WARN{color:var(--yel);}" +
            ".pbw{background:var(--bg);border-radius:4px;height:3px;margin:6px 14px 0;overflow:hidden;}" +
            ".pb{height:100%;background:var(--acc);border-radius:4px;transition:width .3s;width:0%;}" +
            ".sr{display:flex;gap:7px;padding:9px 14px;border-bottom:1px solid var(--bd);}" +
            ".sc2{flex:1;background:var(--bg);border-radius:6px;padding:7px;text-align:center;border:1px solid var(--bd);}" +
            ".sn2{font-size:18px;font-weight:600;font-family:var(--mo);}" +
            ".sl2{font-size:9px;color:var(--tx3);text-transform:uppercase;letter-spacing:.5px;margin-top:1px;}" +
            ".sn2.g{color:var(--grn);}.sn2.r{color:var(--red);}.sn2.y{color:var(--yel);}" +
            ".es{display:flex;flex-direction:column;align-items:center;justify-content:center;height:100%;color:var(--tx3);text-align:center;padding:30px;}" +
            ".es .ei{font-size:40px;margin-bottom:12px;opacity:.4;}" +
            ".es p{font-size:13px;line-height:1.6;}" +
            ".toast{position:fixed;bottom:20px;right:20px;background:var(--s3);border:1px solid var(--bd2);border-radius:9px;padding:10px 16px;font-size:12px;z-index:999;transform:translateY(60px);opacity:0;transition:all .3s;}" +
            ".toast.show{transform:translateY(0);opacity:1;}" +
            ".toast.ok{border-color:var(--grn);color:var(--grn);}" +
            ".toast.er{border-color:var(--red);color:var(--red);}" +
            ".mo{position:fixed;inset:0;background:rgba(0,0,0,.65);z-index:200;display:none;align-items:center;justify-content:center;}" +
            ".mo.open{display:flex;}" +
            ".md{background:var(--s1);border:1px solid var(--bd2);border-radius:13px;width:560px;max-height:85vh;overflow-y:auto;}" +
            ".mh{padding:18px 22px 14px;border-bottom:1px solid var(--bd);display:flex;align-items:center;justify-content:space-between;}" +
            ".mt{font-size:15px;font-weight:600;}" +
            ".mc{background:none;border:none;color:var(--tx3);cursor:pointer;font-size:18px;}" +
            ".mb{padding:18px 22px;}" +
            ".mf{padding:14px 22px;border-top:1px solid var(--bd);display:flex;gap:8px;justify-content:flex-end;}" +
            ".ca{background:var(--bg);border:1px solid var(--bd);border-radius:6px;padding:14px;font-family:var(--mo);font-size:11px;color:#8be0a4;line-height:1.7;overflow-x:auto;white-space:pre;max-height:420px;overflow-y:auto;}" +
            ".tag{font-size:10px;padding:2px 7px;border-radius:20px;}" +
            ".tb{background:rgba(79,142,247,.15);color:var(--acc);}" +
            "::-webkit-scrollbar{width:4px;}" +
            "::-webkit-scrollbar-thumb{background:var(--bd2);border-radius:4px;}" +
            "</style>";

        String html =
            "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<title>Selenium Automation Studio</title>" +
            "<link href='https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;600&family=DM+Sans:wght@300;400;500;600&display=swap' rel='stylesheet'>" +
            css +
            "</head><body>" +

            // ── HEADER ──
            "<div class='hdr'>" +
            "<div class='logo'><div class='logo-icon'>&#x1F916;</div><div class='logo-txt'>Selenium Studio</div><div class='logo-ver'>v1.1</div></div>" +
            "<div class='ha'>" +
            "<button class='btn' id='btnSave'>&#x1F4BE; Save</button>" +
            "<button class='btn' id='btnLoad'>&#x1F4C2; Load</button>" +
            "<button class='btn' id='btnCode'>&#x1F441; Code</button>" +
            "<button class='btn btn-run' id='btnRun'>&#x25B6; Run</button>" +
            "<button class='btn btn-stop' id='btnStop'>&#x23F9; Stop</button>" +
            "</div></div>" +

            "<div class='layout'>" +

            // ── SIDEBAR ──
            "<div class='sidebar'>" +
            "<div class='ph'><span class='pt'>Configuration</span></div>" +
            "<div class='sec'><div class='sl'>Target URL</div>" +
            "<div class='fl'><label>Website URL</label><input type='text' id='url' placeholder='https://example.com'/></div></div>" +

            "<div class='sec'><div class='sl'>Browser</div>" +
            "<div class='bg'>" +
            "<div class='bc' id='bc-chrome' data-b='chrome'><div class='bi'>&#x1F310;</div><div class='bn'>Chrome</div><div class='cd'></div></div>" +
            "<div class='bc' id='bc-firefox' data-b='firefox'><div class='bi'>&#x1F98A;</div><div class='bn'>Firefox</div><div class='cd'></div></div>" +
            "<div class='bc' id='bc-edge' data-b='edge'><div class='bi'>&#x1F535;</div><div class='bn'>Edge</div><div class='cd'></div></div>" +
            "<div class='bc' id='bc-all' data-b='all'><div class='bi'>&#x26A1;</div><div class='bn'>All</div><div class='cd'></div></div>" +
            "</div></div>" +

            "<div class='sec'><div class='sl'>Window</div>" +
            "<div class='tr'><span class='tl'>&#x1F5A5; Maximize</span><label class='tg'><input type='checkbox' id='maximize' checked><span class='ts'></span></label></div>" +
            "<div class='tr'><span class='tl'>&#x1F47B; Headless</span><label class='tg'><input type='checkbox' id='headless'><span class='ts'></span></label></div>" +
            "<div class='fl'><label>Resolution (if not maximized)</label><div class='rr'><input type='number' id='resW' value='1920'/><span>x</span><input type='number' id='resH' value='1080'/></div></div></div>" +

            "<div class='sec'><div class='sl'>Timeouts</div>" +
            "<div class='r2'><div class='fl'><label>Implicit (sec)</label><input type='number' id='implWait' value='10'/></div>" +
            "<div class='fl'><label>Page Load (sec)</label><input type='number' id='pageLoad' value='30'/></div></div></div>" +

            "<div class='sec'><div class='sl'>Screenshot</div>" +
            "<div class='tr'><span class='tl'>&#x1F4F8; Auto on Fail</span><label class='tg'><input type='checkbox' id='autoSS' checked><span class='ts'></span></label></div>" +
            "<div class='fl'><label>Save Folder</label><input type='text' id='ssDir' value='./screenshots'/></div></div>" +
            "</div>" +

            // ── MAIN ──
            "<div class='main'>" +
            "<div class='stb'>" +
            "<div style='display:flex;align-items:center;gap:9px;'><span style='font-size:13px;font-weight:600;'>Test Steps</span><span class='tag tb' id='scnt'>0 steps</span></div>" +
            "<div style='display:flex;gap:7px;'>" +
            "<button class='btn' style='font-size:11px;padding:5px 10px;' id='btnClear'>&#x1F5D1; Clear</button>" +
            "<button class='btn' style='font-size:11px;padding:5px 10px;background:var(--acc);border-color:var(--acc);color:#fff;' id='btnAddStep'>+ Add Step</button>" +
            "</div></div>" +

            "<div class='sc' id='sc'>" +
            "<div class='es' id='es'><div class='ei'>&#x1F4CB;</div><p>No steps yet.<br>Click <b>+ Add Step</b> to start.</p></div>" +
            "</div>" +

            // Action picker panel
            "<div class='asa' id='asa'>" +
            "<div style='display:flex;align-items:center;justify-content:space-between;margin-bottom:8px;'>" +
            "<span style='font-size:12px;font-weight:600;'>Choose Action Type</span>" +
            "<button class='btn' style='font-size:11px;padding:4px 9px;' id='btnCloseAdd'>&#x2715;</button></div>" +
            "<div class='ag' id='actionGrid'>" +
            "<div class='at' data-action='click'><div class='ic'>&#x1F446;</div><div class='nm'>Click</div></div>" +
            "<div class='at' data-action='type'><div class='ic'>&#x2328;&#xFE0F;</div><div class='nm'>Type Text</div></div>" +
            "<div class='at' data-action='dropdown'><div class='ic'>&#x1F4CB;</div><div class='nm'>Dropdown</div></div>" +
            "<div class='at' data-action='getText'><div class='ic'>&#x1F4D6;</div><div class='nm'>Get Text</div></div>" +
            "<div class='at' data-action='getAttribute'><div class='ic'>&#x1F3F7;</div><div class='nm'>Get Attr</div></div>" +
            "<div class='at' data-action='verifyText'><div class='ic'>&#x2705;</div><div class='nm'>Verify Text</div></div>" +
            "<div class='at' data-action='verifyElement'><div class='ic'>&#x1F50D;</div><div class='nm'>Verify Elem</div></div>" +
            "<div class='at' data-action='navigate'><div class='ic'>&#x1F310;</div><div class='nm'>Navigate</div></div>" +
            "<div class='at' data-action='screenshot'><div class='ic'>&#x1F4F8;</div><div class='nm'>Screenshot</div></div>" +
            "<div class='at' data-action='scrollTo'><div class='ic'>&#x2195;&#xFE0F;</div><div class='nm'>Scroll To</div></div>" +
            "<div class='at' data-action='hover'><div class='ic'>&#x1F5B1;</div><div class='nm'>Hover</div></div>" +
            "<div class='at' data-action='clearField'><div class='ic'>&#x1F5D1;</div><div class='nm'>Clear Field</div></div>" +
            "<div class='at' data-action='jsClick'><div class='ic'>&#x26A1;</div><div class='nm'>JS Click</div></div>" +
            "<div class='at' data-action='acceptAlert'><div class='ic'>&#x1F514;</div><div class='nm'>Alert</div></div>" +
            "<div class='at' data-action='switchFrame'><div class='ic'>&#x1F5BC;</div><div class='nm'>Switch Frame</div></div>" +
            "<div class='at' data-action='switchWindow'><div class='ic'>&#x1F500;</div><div class='nm'>New Window</div></div>" +
            "<div class='at' data-action='print'><div class='ic'>&#x1F5A8;</div><div class='nm'>Print Log</div></div>" +
            "<div class='at' data-action='wait'><div class='ic'>&#x23F3;</div><div class='nm'>Wait</div></div>" +
            "</div></div></div>" +

            // ── RIGHT PANEL ──
            "<div class='rp'>" +
            "<div class='ph'><span class='pt'>Execution Log</span><button class='btn' style='font-size:11px;padding:4px 9px;' id='btnClearLogs'>Clear</button></div>" +
            "<div class='sr'>" +
            "<div class='sc2'><div class='sn2 y' id='st'>0</div><div class='sl2'>Total</div></div>" +
            "<div class='sc2'><div class='sn2 g' id='sp'>0</div><div class='sl2'>Pass</div></div>" +
            "<div class='sc2'><div class='sn2 r' id='sf'>0</div><div class='sl2'>Fail</div></div>" +
            "</div><div class='pbw'><div class='pb' id='pb'></div></div>" +
            "<div class='lp' id='lp'><div style='color:var(--tx3);font-size:11px;padding:16px;text-align:center;'>Ready...</div></div>" +
            "</div>" +

            "</div>" + // end .layout

            // ── CODE MODAL ──
            "<div class='mo' id='codeModal'>" +
            "<div class='md'>" +
            "<div class='mh'><span class='mt'>Generated Java Code</span><button class='mc' id='btnCloseCode'>&#x2715;</button></div>" +
            "<div class='mb'>" +
            "<div style='font-size:12px;color:var(--tx2);margin-bottom:10px;'>Copy and paste into Eclipse as a new Java class.</div>" +
            "<div class='ca' id='codeDisp'></div>" +
            "</div>" +
            "<div class='mf'>" +
            "<button class='btn' id='btnCopyCode'>&#x1F4CB; Copy All</button>" +
            "<button class='btn' style='background:var(--acc);border-color:var(--acc);color:#fff;' id='btnCloseCode2'>Done</button>" +
            "</div></div></div>" +

            "<div class='toast' id='toast'></div>" +

            // ── ALL JAVASCRIPT (clean, no inline onclick escaping) ──
            "<script>\n" +
            "var steps = [], ctr = 0, polling = null;\n" +
            "\n" +
            "var LABELS = {\n" +
            "  click:'Click', type:'Type Text', dropdown:'Dropdown',\n" +
            "  getText:'Get Text', getAttribute:'Get Attr', verifyText:'Verify Text',\n" +
            "  verifyElement:'Verify Elem', navigate:'Navigate', screenshot:'Screenshot',\n" +
            "  scrollTo:'Scroll To', hover:'Hover', clearField:'Clear Field',\n" +
            "  jsClick:'JS Click', acceptAlert:'Alert', switchFrame:'Switch Frame',\n" +
            "  switchWindow:'Switch Window', print:'Print Log', wait:'Wait'\n" +
            "};\n" +
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
            "      var allBtn = document.getElementById('bc-all');\n" +
            "      sel.length === 3 ? allBtn.classList.add('on') : allBtn.classList.remove('on');\n" +
            "    }\n" +
            "  });\n" +
            "});\n" +
            "\n" +
            "// ── Add step panel ──\n" +
            "document.getElementById('btnAddStep').addEventListener('click', function() {\n" +
            "  var asa = document.getElementById('asa');\n" +
            "  asa.style.display = asa.style.display === 'none' || asa.style.display === '' ? 'block' : 'none';\n" +
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
            "  steps.push({id: ctr, action: action, xpath: '', value: '', value2: '', wait: 0, status: 'pending'});\n" +
            "  renderSteps();\n" +
            "  updateCnt();\n" +
            "  document.getElementById('asa').style.display = 'none';\n" +
            "  toast('Step added: ' + LABELS[action], 'ok');\n" +
            "});\n" +
            "\n" +
            "// ── Clear all ──\n" +
            "document.getElementById('btnClear').addEventListener('click', function() {\n" +
            "  if (steps.length && !confirm('Delete all steps?')) return;\n" +
            "  steps = [];\n" +
            "  renderSteps();\n" +
            "  updateCnt();\n" +
            "});\n" +
            "\n" +
            "// ── Step list delegation ──\n" +
            "document.getElementById('sc').addEventListener('click', function(e) {\n" +
            "  var sh = e.target.closest('.sh');\n" +
            "  var btn = e.target.closest('button[data-act]');\n" +
            "  if (sh && !btn) {\n" +
            "    var sk = sh.closest('.sk');\n" +
            "    if (sk) {\n" +
            "      var sb = sk.querySelector('.sb');\n" +
            "      if (sb) sb.classList.toggle('open');\n" +
            "    }\n" +
            "    return;\n" +
            "  }\n" +
            "  if (!btn) return;\n" +
            "  var act = btn.dataset.act;\n" +
            "  var id = parseInt(btn.dataset.id);\n" +
            "  if (act === 'del') {\n" +
            "    steps = steps.filter(function(s) { return s.id !== id; });\n" +
            "    renderSteps(); updateCnt();\n" +
            "  } else if (act === 'up' || act === 'dn') {\n" +
            "    var idx = steps.findIndex(function(s) { return s.id === id; });\n" +
            "    var ni = act === 'up' ? idx - 1 : idx + 1;\n" +
            "    if (ni >= 0 && ni < steps.length) {\n" +
            "      var tmp = steps[idx]; steps[idx] = steps[ni]; steps[ni] = tmp;\n" +
            "    }\n" +
            "    renderSteps();\n" +
            "    // reopen moved step\n" +
            "    var sk2 = document.getElementById('sk-' + id);\n" +
            "    if (sk2) { var sb2 = sk2.querySelector('.sb'); if (sb2) sb2.classList.add('open'); }\n" +
            "  }\n" +
            "});\n" +
            "\n" +
            "// ── Field update delegation (input/select change) ──\n" +
            "document.getElementById('sc').addEventListener('change', function(e) {\n" +
            "  var el = e.target;\n" +
            "  var id = parseInt(el.dataset.sid);\n" +
            "  var fld = el.dataset.fld;\n" +
            "  if (!id || !fld) return;\n" +
            "  var s = steps.find(function(x) { return x.id === id; });\n" +
            "  if (!s) return;\n" +
            "  s[fld] = el.value;\n" +
            "  // For dropdown method change: re-render to update label\n" +
            "  if (fld === 'value' && s.action === 'dropdown') {\n" +
            "    var sk = document.getElementById('sk-' + id);\n" +
            "    var sb = sk ? sk.querySelector('.sb') : null;\n" +
            "    renderSteps();\n" +
            "    var sk2 = document.getElementById('sk-' + id);\n" +
            "    var sb2 = sk2 ? sk2.querySelector('.sb') : null;\n" +
            "    if (sb2) sb2.classList.add('open');\n" +
            "  }\n" +
            "  // Update header preview\n" +
            "  var sk = document.getElementById('sk-' + id);\n" +
            "  if (sk) {\n" +
            "    var ss = sk.querySelector('.ss');\n" +
            "    if (ss) ss.textContent = s.xpath || s.value || 'Configure...';\n" +
            "  }\n" +
            "});\n" +
            "\n" +
            "// Same for input events (live preview)\n" +
            "document.getElementById('sc').addEventListener('input', function(e) {\n" +
            "  var el = e.target;\n" +
            "  var id = parseInt(el.dataset.sid);\n" +
            "  var fld = el.dataset.fld;\n" +
            "  if (!id || !fld) return;\n" +
            "  var s = steps.find(function(x) { return x.id === id; });\n" +
            "  if (!s) return;\n" +
            "  s[fld] = el.value;\n" +
            "  var sk = document.getElementById('sk-' + id);\n" +
            "  if (sk) {\n" +
            "    var ss = sk.querySelector('.ss');\n" +
            "    if (ss) ss.textContent = s.xpath || s.value || 'Configure...';\n" +
            "  }\n" +
            "});\n" +
            "\n" +
            "function renderSteps() {\n" +
            "  var c = document.getElementById('sc');\n" +
            "  if (steps.length === 0) {\n" +
            "    c.innerHTML = \"<div class='es' id='es'><div class='ei'>&#x1F4CB;</div><p>No steps yet.<br>Click <b>+ Add Step</b> to start.</p></div>\";\n" +
            "    return;\n" +
            "  }\n" +
            "  var h = '';\n" +
            "  steps.forEach(function(s, i) {\n" +
            "    var ico = s.status==='pass'?'&#x2705;':s.status==='fail'?'&#x274C;':s.status==='run'?'&#x23F3;':'';\n" +
            "    h += \"<div class='sk \" + s.status + \"' id='sk-\" + s.id + \"'>\";\n" +
            "    h += \"<div class='sh'><div class='sn'>\" + (i+1) + \"</div>\";\n" +
            "    h += \"<div class='sab'>\" + LABELS[s.action] + \"</div>\";\n" +
            "    h += \"<div class='ss'>\" + escH(s.xpath || s.value || 'Configure...') + \"</div>\";\n" +
            "    h += \"<div style='font-size:16px;margin-left:auto;flex-shrink:0;'>\" + ico + \"</div></div>\";\n" +
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
            "function inp(sid, fld, val, placeholder) {\n" +
            "  return \"<input type='text' data-sid='\" + sid + \"' data-fld='\" + fld + \"' value='\" + escH(val) + \"' placeholder='\" + escH(placeholder) + \"'/>\";\n" +
            "}\n" +
            "function numInp(sid, fld, val, placeholder) {\n" +
            "  return \"<input type='number' data-sid='\" + sid + \"' data-fld='\" + fld + \"' value='\" + escH(val) + \"' placeholder='\" + escH(placeholder) + \"'/>\";\n" +
            "}\n" +
            "\n" +
            "function renderFields(s) {\n" +
            "  var h = \"<div class='sf'>\", a = s.action;\n" +
            "  if (a === 'navigate') {\n" +
            "    h += \"<div><label>URL</label>\" + inp(s.id,'value',s.value,'https://...') + \"</div>\";\n" +
            "  } else if (a === 'print') {\n" +
            "    h += \"<div><label>Message</label>\" + inp(s.id,'value',s.value,'Log message') + \"</div>\";\n" +
            "  } else if (a === 'wait') {\n" +
            "    h += \"<div><label>Wait (ms)</label>\" + numInp(s.id,'value',s.value||'1000','1000') + \"</div>\";\n" +
            "  } else if (a === 'acceptAlert') {\n" +
            "    h += \"<div><label>Action</label><select data-sid='\" + s.id + \"' data-fld='value'>\";\n" +
            "    h += \"<option value='accept'\" + (s.value!=='dismiss'?' selected':'') + \">Accept (OK)</option>\";\n" +
            "    h += \"<option value='dismiss'\" + (s.value==='dismiss'?' selected':'') + \">Dismiss (Cancel)</option>\";\n" +
            "    h += \"</select></div>\";\n" +
            "  } else if (a === 'screenshot') {\n" +
            "    h += \"<div><label>File Name</label>\" + inp(s.id,'value',s.value,'screenshot_name') + \"</div>\";\n" +
            "  } else if (a === 'switchFrame') {\n" +
            "    h += \"<div><label>Frame (XPath / Index)</label>\" + inp(s.id,'value',s.value,'//iframe or 0') + \"</div>\";\n" +
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
            "    var lbl = s.value==='selectByIndex' ? 'Index (0,1,2...)' : 'Text / Value';\n" +
            "    var ph  = s.value==='selectByIndex' ? '0' : 'Option text';\n" +
            "    h += \"<div><label>\" + lbl + \"</label>\" + inp(s.id,'value2',s.value2,ph) + \"</div>\";\n" +
            "  } else {\n" +
            "    h += \"<div><label>XPath</label>\" + inp(s.id,'xpath',s.xpath,'//button[@id=\\'submit\\']') + \"</div>\";\n" +
            "    if (a === 'type') {\n" +
            "      h += \"<div><label>Text to Type</label>\" + inp(s.id,'value',s.value,'Enter text...') + \"</div>\";\n" +
            "    }\n" +
            "  }\n" +
            "  h += \"</div>\";\n" +
            "  h += \"<div class='war'><label>&#x23F1; Wait After (ms):</label>\" + numInp(s.id,'wait',s.wait,'0') + \"</div>\";\n" +
            "  h += \"<div class='sar'>\";\n" +
            "  h += \"<button class='btn' style='font-size:11px;padding:4px 9px;' data-act='up' data-id='\" + s.id + \"'>&#x2191; Up</button>\";\n" +
            "  h += \"<button class='btn' style='font-size:11px;padding:4px 9px;' data-act='dn' data-id='\" + s.id + \"'>&#x2193; Down</button>\";\n" +
            "  h += \"<button class='btn' style='font-size:11px;padding:4px 9px;border-color:var(--red);color:var(--red);' data-act='del' data-id='\" + s.id + \"'>&#x1F5D1; Delete</button>\";\n" +
            "  h += \"</div>\";\n" +
            "  return h;\n" +
            "}\n" +
            "\n" +
            "function updateCnt() {\n" +
            "  document.getElementById('scnt').textContent = steps.length + ' step' + (steps.length !== 1 ? 's' : '');\n" +
            "}\n" +
            "\n" +
            "function getCfg() {\n" +
            "  var browsers = ['chrome','firefox','edge'].filter(function(b) {\n" +
            "    return document.getElementById('bc-' + b).classList.contains('on');\n" +
            "  });\n" +
            "  if (browsers.length === 0) browsers = ['chrome'];\n" +
            "  return {\n" +
            "    url: document.getElementById('url').value || 'https://example.com',\n" +
            "    browsers: browsers,\n" +
            "    maximize: document.getElementById('maximize').checked,\n" +
            "    headless: document.getElementById('headless').checked,\n" +
            "    resW: parseInt(document.getElementById('resW').value) || 1920,\n" +
            "    resH: parseInt(document.getElementById('resH').value) || 1080,\n" +
            "    implicitWait: parseInt(document.getElementById('implWait').value) || 10,\n" +
            "    pageLoad: parseInt(document.getElementById('pageLoad').value) || 30,\n" +
            "    autoScreenshot: document.getElementById('autoSS').checked,\n" +
            "    screenshotDir: document.getElementById('ssDir').value || './screenshots',\n" +
            "    steps: steps\n" +
            "  };\n" +
            "}\n" +
            "\n" +
            "// ── Run / Stop ──\n" +
            "document.getElementById('btnRun').addEventListener('click', function() {\n" +
            "  if (steps.length === 0) { toast('Add some steps first!', 'er'); return; }\n" +
            "  document.getElementById('lp').innerHTML = '';\n" +
            "  document.getElementById('st').textContent = '0';\n" +
            "  document.getElementById('sp').textContent = '0';\n" +
            "  document.getElementById('sf').textContent = '0';\n" +
            "  document.getElementById('pb').style.width = '0%';\n" +
            "  steps.forEach(function(s) { s.status = 'pending'; });\n" +
            "  renderSteps();\n" +
            "  document.getElementById('btnRun').style.display = 'none';\n" +
            "  document.getElementById('btnStop').style.display = 'inline-flex';\n" +
            "  fetch('/run', {\n" +
            "    method: 'POST',\n" +
            "    headers: {'Content-Type':'application/json'},\n" +
            "    body: JSON.stringify(getCfg())\n" +
            "  }).then(function() { startPolling(); })\n" +
            "    .catch(function(e) { toast('Server error: ' + e.message, 'er'); resetButtons(); });\n" +
            "});\n" +
            "\n" +
            "document.getElementById('btnStop').addEventListener('click', function() {\n" +
            "  fetch('/stop', {method:'POST'})\n" +
            "    .then(function() { resetButtons(); toast('Stopped', 'er'); });\n" +
            "});\n" +
            "\n" +
            "function startPolling() {\n" +
            "  if (polling) clearInterval(polling);\n" +
            "  polling = setInterval(pollStatus, 600);\n" +
            "}\n" +
            "\n" +
            "function pollStatus() {\n" +
            "  fetch('/status')\n" +
            "    .then(function(r) { return r.json(); })\n" +
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
            "      if (!d.running) { clearInterval(polling); polling = null; resetButtons(); }\n" +
            "    }).catch(function() {});\n" +
            "}\n" +
            "\n" +
            "function resetButtons() {\n" +
            "  document.getElementById('btnRun').style.display = 'inline-flex';\n" +
            "  document.getElementById('btnStop').style.display = 'none';\n" +
            "}\n" +
            "\n" +
            "// ── Log polling ──\n" +
            "setInterval(function() {\n" +
            "  fetch('/logs')\n" +
            "    .then(function(r) { return r.json(); })\n" +
            "    .then(function(logs) {\n" +
            "      var lp = document.getElementById('lp');\n" +
            "      lp.innerHTML = '';\n" +
            "      logs.forEach(function(l) {\n" +
            "        var parts = l.split('|');\n" +
            "        if (parts.length < 3) return;\n" +
            "        var div = document.createElement('div');\n" +
            "        div.className = 'll';\n" +
            "        div.innerHTML = '<span class=\"lt\">' + parts[0] + '</span><span class=\"lm ' + parts[1] + '\">' + parts.slice(2).join('|') + '</span>';\n" +
            "        lp.appendChild(div);\n" +
            "      });\n" +
            "      lp.scrollTop = lp.scrollHeight;\n" +
            "    }).catch(function() {});\n" +
            "}, 800);\n" +
            "\n" +
            "document.getElementById('btnClearLogs').addEventListener('click', function() {\n" +
            "  document.getElementById('lp').innerHTML = '<div style=\"color:var(--tx3);font-size:11px;padding:16px;text-align:center;\">Cleared.</div>';\n" +
            "});\n" +
            "\n" +
            "// ── Code modal ──\n" +
            "document.getElementById('btnCode').addEventListener('click', function() {\n" +
            "  fetch('/gencode', {\n" +
            "    method: 'POST',\n" +
            "    headers: {'Content-Type':'application/json'},\n" +
            "    body: JSON.stringify(getCfg())\n" +
            "  }).then(function(r) { return r.text(); })\n" +
            "    .then(function(code) {\n" +
            "      document.getElementById('codeDisp').textContent = code;\n" +
            "      document.getElementById('codeModal').classList.add('open');\n" +
            "    }).catch(function() { toast('Could not generate code', 'er'); });\n" +
            "});\n" +
            "document.getElementById('btnCloseCode').addEventListener('click', function() {\n" +
            "  document.getElementById('codeModal').classList.remove('open');\n" +
            "});\n" +
            "document.getElementById('btnCloseCode2').addEventListener('click', function() {\n" +
            "  document.getElementById('codeModal').classList.remove('open');\n" +
            "});\n" +
            "document.getElementById('btnCopyCode').addEventListener('click', function() {\n" +
            "  navigator.clipboard.writeText(document.getElementById('codeDisp').textContent);\n" +
            "  toast('Copied! \\uD83D\\uDCCB', 'ok');\n" +
            "});\n" +
            "\n" +
            "// ── Save / Load ──\n" +
            "document.getElementById('btnSave').addEventListener('click', function() {\n" +
            "  var data = JSON.stringify(getCfg(), null, 2);\n" +
            "  var a = document.createElement('a');\n" +
            "  a.href = URL.createObjectURL(new Blob([data], {type:'application/json'}));\n" +
            "  a.download = 'selenium_project_' + Date.now() + '.json';\n" +
            "  a.click();\n" +
            "  toast('Saved!', 'ok');\n" +
            "});\n" +
            "\n" +
            "document.getElementById('btnLoad').addEventListener('click', function() {\n" +
            "  var inp = document.createElement('input');\n" +
            "  inp.type = 'file'; inp.accept = '.json';\n" +
            "  inp.onchange = function(e) {\n" +
            "    var r = new FileReader();\n" +
            "    r.onload = function(ev) {\n" +
            "      try {\n" +
            "        var d = JSON.parse(ev.target.result);\n" +
            "        if (d.url) document.getElementById('url').value = d.url;\n" +
            "        if (d.browsers) {\n" +
            "          ['chrome','firefox','edge','all'].forEach(function(b) {\n" +
            "            var c = document.getElementById('bc-' + b);\n" +
            "            d.browsers.includes(b) ? c.classList.add('on') : c.classList.remove('on');\n" +
            "          });\n" +
            "        }\n" +
            "        if (d.maximize !== undefined) document.getElementById('maximize').checked = d.maximize;\n" +
            "        if (d.headless !== undefined) document.getElementById('headless').checked = d.headless;\n" +
            "        if (d.resW) document.getElementById('resW').value = d.resW;\n" +
            "        if (d.resH) document.getElementById('resH').value = d.resH;\n" +
            "        if (d.implicitWait) document.getElementById('implWait').value = d.implicitWait;\n" +
            "        if (d.pageLoad) document.getElementById('pageLoad').value = d.pageLoad;\n" +
            "        if (d.autoScreenshot !== undefined) document.getElementById('autoSS').checked = d.autoScreenshot;\n" +
            "        if (d.screenshotDir) document.getElementById('ssDir').value = d.screenshotDir;\n" +
            "        if (d.steps) {\n" +
            "          steps = d.steps;\n" +
            "          ctr = steps.length ? Math.max.apply(null, steps.map(function(s) { return s.id; })) : 0;\n" +
            "          renderSteps(); updateCnt();\n" +
            "        }\n" +
            "        toast('Loaded!', 'ok');\n" +
            "      } catch(e) { toast('Invalid file!', 'er'); }\n" +
            "    };\n" +
            "    r.readAsText(e.target.files[0]);\n" +
            "  };\n" +
            "  inp.click();\n" +
            "});\n" +
            "\n" +
            "function toast(msg, type) {\n" +
            "  var t = document.getElementById('toast');\n" +
            "  t.textContent = msg;\n" +
            "  t.className = 'toast show ' + (type || '');\n" +
            "  setTimeout(function() { t.className = 'toast'; }, 2800);\n" +
            "}\n" +
            "</script>\n" +
            "</body></html>";

        return html;
    }

    // ═══════════════════════════════════════════════════════════
    //  Minimal JSON Parser
    // ═══════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private static Map<String,Object> parseJson(String json) {
        try {
            Class<?> gsonClass = Class.forName("com.google.gson.Gson");
            Object gson = gsonClass.getDeclaredConstructor().newInstance();
            java.lang.reflect.Method fromJson = gsonClass.getMethod("fromJson", String.class, Class.class);
            return (Map<String,Object>) fromJson.invoke(gson, json, Map.class);
        } catch (Exception e) { return simpleJsonParse(json); }
    }

    private static Map<String,Object> simpleJsonParse(String json) {
        Map<String,Object> map = new LinkedHashMap<>();
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1, json.length()-1);
        int ba = json.indexOf("\"browsers\"");
        if (ba >= 0) {
            int bs = json.indexOf("[", ba), be = json.indexOf("]", bs);
            if (bs >= 0 && be >= 0) {
                String arr = json.substring(bs+1, be);
                List<String> browsers = new ArrayList<>();
                for (String s : arr.split(",")) browsers.add(s.trim().replace("\"",""));
                map.put("browsers", browsers);
            }
        }
        int sa = json.indexOf("\"steps\"");
        if (sa >= 0) {
            int ss = json.indexOf("[", sa), depth = 0, se = ss;
            for (int i = ss; i < json.length(); i++) {
                if (json.charAt(i) == '[') depth++;
                else if (json.charAt(i) == ']') { depth--; if (depth==0){se=i;break;} }
            }
            map.put("steps", parseStepsArray(json.substring(ss+1, se)));
        }
        for (String k : new String[]{"url","screenshotDir"}) {
            int ki = json.indexOf("\""+k+"\""); if (ki < 0) continue;
            int colon = json.indexOf(":", ki), qs = json.indexOf("\"", colon), qe = json.indexOf("\"", qs+1);
            if (qs >= 0 && qe >= 0) map.put(k, json.substring(qs+1, qe));
        }
        for (String k : new String[]{"maximize","headless","autoScreenshot"}) {
            int ki = json.indexOf("\""+k+"\""); if (ki < 0) continue;
            map.put(k, json.substring(json.indexOf(":", ki)+1).trim().startsWith("true"));
        }
        for (String k : new String[]{"implicitWait","pageLoad","resW","resH"}) {
            int ki = json.indexOf("\""+k+"\""); if (ki < 0) continue;
            String rest = json.substring(json.indexOf(":", ki)+1).trim();
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
                if (json.charAt(j) == '{') depth++;
                else if (json.charAt(j) == '}') { depth--; if(depth==0){end=j;break;} }
            }
            String obj = json.substring(start+1, end);
            Map<String,Object> step = new LinkedHashMap<>();
            for (String k : new String[]{"action","xpath","value","value2"}) {
                int ki = obj.indexOf("\""+k+"\""); if (ki < 0) continue;
                int colon = obj.indexOf(":", ki), qs = obj.indexOf("\"", colon), qe = obj.indexOf("\"", qs+1);
                if (qs >= 0 && qe >= 0) step.put(k, obj.substring(qs+1, qe));
            }
            int wi = obj.indexOf("\"wait\"");
            if (wi >= 0) {
                String rest = obj.substring(obj.indexOf(":", wi)+1).trim();
                StringBuilder num = new StringBuilder();
                for (char c : rest.toCharArray()) { if (Character.isDigit(c)) num.append(c); else if (num.length() > 0) break; }
                if (num.length() > 0) step.put("wait", Integer.parseInt(num.toString()));
            }
            list.add(step); i = end + 1;
        }
        return list;
    }

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