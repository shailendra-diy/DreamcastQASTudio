package com.selenium.studio;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds all configuration for a test run — URL, browser, timeouts, steps, etc.
 */
public class TestConfig {

    private String       url            = "https://example.com";
    private List<String> browsers       = new ArrayList<>();
    private boolean      maximize       = true;
    private boolean      headless       = false;
    private int          resW           = 1920;
    private int          resH           = 1080;
    private int          implicitWait   = 10;
    private int          pageLoad       = 30;
    private boolean      autoScreenshot = true;
    private String       screenshotDir  = "./screenshots";
    private boolean      captureApi     = false;
    private List<TestStep> steps        = new ArrayList<>();

    public TestConfig() {
        browsers.add("chrome");
    }

    // ── Getters ──────────────────────────────────────────────
    public String         getUrl()            { return url; }
    public List<String>   getBrowsers()       { return browsers; }
    public boolean        isMaximize()        { return maximize; }
    public boolean        isHeadless()        { return headless; }
    public int            getResW()           { return resW; }
    public int            getResH()           { return resH; }
    public int            getImplicitWait()   { return implicitWait; }
    public int            getPageLoad()       { return pageLoad; }
    public boolean        isAutoScreenshot()  { return autoScreenshot; }
    public String         getScreenshotDir()  { return screenshotDir; }
    public boolean        isCaptureApi()      { return captureApi; }
    public List<TestStep> getSteps()          { return steps; }

    // ── Setters ──────────────────────────────────────────────
    public void setUrl(String url)                    { this.url = url; }
    public void setBrowsers(List<String> browsers)    { this.browsers = browsers; }
    public void setMaximize(boolean maximize)         { this.maximize = maximize; }
    public void setHeadless(boolean headless)         { this.headless = headless; }
    public void setResW(int resW)                     { this.resW = resW; }
    public void setResH(int resH)                     { this.resH = resH; }
    public void setImplicitWait(int implicitWait)     { this.implicitWait = implicitWait; }
    public void setPageLoad(int pageLoad)             { this.pageLoad = pageLoad; }
    public void setAutoScreenshot(boolean autoSS)     { this.autoScreenshot = autoSS; }
    public void setScreenshotDir(String screenshotDir){ this.screenshotDir = screenshotDir; }
    public void setCaptureApi(boolean captureApi)     { this.captureApi = captureApi; }
    public void setSteps(List<TestStep> steps)        { this.steps = steps; }
}
