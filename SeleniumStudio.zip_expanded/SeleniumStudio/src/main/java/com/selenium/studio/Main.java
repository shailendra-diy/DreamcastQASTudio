package com.selenium.studio;

import com.selenium.studio.server.StudioServer;

/**
 * Entry point for Selenium Automation Studio v2.0.
 *
 * HOW TO RUN:
 *   mvn package
 *   java -jar target/selenium-studio-2.0.jar
 *   Then open http://localhost:8769
 *
 * Or run this class directly from Eclipse as a Java Application.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        StudioServer.main(args);
    }
}
