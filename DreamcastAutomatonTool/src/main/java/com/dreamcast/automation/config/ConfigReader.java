package com.dreamcast.automation.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    private static final Properties props = new Properties();
    private static final String CONFIG_PATH = "src/test/resources/config.properties";

    static {
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Could not load config.properties: " + e.getMessage());
        }
    }

    public static String get(String key) {
        String value = props.getProperty(key);
        if (value == null) throw new RuntimeException("Property not found: " + key);
        return value.trim();
    }

    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue).trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBool(String key) {
        return Boolean.parseBoolean(get(key));
    }

    // Convenience shortcuts
    public static String browser()          { return get("browser", "chrome"); }
    public static boolean headless()        { return getBool("headless"); }
    public static String baseUrl()          { return get("base.url"); }
    public static String loginUrl()         { return get("login.url"); }
    public static String username()         { return get("username"); }
    public static String password()         { return get("password"); }
    public static int implicitWait()        { return getInt("implicit.wait"); }
    public static int explicitWait()        { return getInt("explicit.wait"); }
    public static int pageLoadTimeout()     { return getInt("page.load.timeout"); }
    public static String screenshotDir()    { return get("screenshot.dir"); }
    public static boolean screenshotOnFail(){ return getBool("screenshot.on.fail"); }
    public static String reportDir()        { return get("report.dir"); }
    public static String reportName()       { return get("report.name"); }
    public static int retryCount()          { return getInt("retry.count"); }
    public static String excelPath()        { return get("excel.path"); }
}
