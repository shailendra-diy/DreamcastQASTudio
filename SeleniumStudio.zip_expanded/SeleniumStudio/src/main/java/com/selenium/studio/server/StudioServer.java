package com.selenium.studio.server;

import com.selenium.studio.engine.TestEngine;
import com.selenium.studio.server.handlers.*;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class StudioServer {

    private static final int PORT = 8769;

    private static final TestEngine engine = new TestEngine();

    public static void main(String[] args) throws Exception {
        System.out.println("============================================");
        System.out.println("   Selenium Automation Studio  v2.0");
        System.out.println("============================================");
        System.out.println("Starting server on port " + PORT + "...");

        startServer();

        String url = "http://localhost:" + PORT + "/";
        System.out.println("Studio running at: " + url);
        System.out.println("Opening browser...");
        openBrowser(url);
        System.out.println("Press Ctrl+C to stop.");
    }

    private static void startServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/",             new UiHandler());
        server.createContext("/run",          new RunHandler(engine));
        server.createContext("/stop",         new StopHandler(engine));
        server.createContext("/status",       new StatusHandler(engine));
        server.createContext("/logs",         new LogsHandler(engine));
        server.createContext("/clearlogs",    new ClearLogsHandler(engine));
        server.createContext("/apilogs",      new ApiLogsHandler(engine));
        server.createContext("/clearapilogs", new ClearApiLogsHandler(engine));
        server.createContext("/gencode",      new CodeGenHandler());
        server.createContext("/setapicapture",new SetApiCaptureHandler(engine));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
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
