package com.dreamcast.automation.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtil {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static PrintWriter fileWriter;

    static {
        try {
            new java.io.File("logs").mkdirs();
            fileWriter = new PrintWriter(new FileWriter("logs/automation.log", true));
        } catch (IOException e) {
            System.err.println("Could not initialize log file: " + e.getMessage());
        }
    }

    public static void info(String message)  { log("INFO ", message); }
    public static void pass(String message)  { log("PASS ", message); }
    public static void fail(String message)  { log("FAIL ", message); }
    public static void warn(String message)  { log("WARN ", message); }
    public static void debug(String message) { log("DEBUG", message); }

    private static void log(String level, String message) {
        String entry = "[" + LocalDateTime.now().format(FMT) + "] [" + level + "] " + message;
        System.out.println(entry);
        if (fileWriter != null) {
            fileWriter.println(entry);
            fileWriter.flush();
        }
    }
}
