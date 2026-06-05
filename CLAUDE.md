# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Workspace Overview

This is an Eclipse Maven workspace containing two Java projects:

1. **DreamcastAutomatonTool** — A Selenium-based test automation framework with multiple test class implementations, Swing-based UI builders, and web scraping tools.
2. **SeleniumStudio** (in `SeleniumStudio.zip_expanded/SeleniumStudio/`) — A visual no-code browser automation tool that serves a local web UI for building and running Selenium tests.

Both projects use Java 17 and Maven 3.6+.

## Build & Run Commands

### SeleniumStudio (the primary runnable tool)

```bash
cd SeleniumStudio.zip_expanded/SeleniumStudio
mvn package -q
java -jar target/selenium-studio-2.0.jar
# Then open http://localhost:8769
```

Or run `Main.java` directly from Eclipse as a Java Application.

### DreamcastAutomatonTool

```bash
cd DreamcastAutomatonTool
mvn compile          # compile only
mvn test             # run all tests
mvn test -Dtest=ClassName#methodName   # run a single test
mvn package          # build JAR
mvn clean            # clean build output
```

Run individual test classes from Eclipse: right-click class → Run As → JUnit/TestNG Test.

## SeleniumStudio Architecture

The studio runs as an embedded HTTP server (port 8769) using `com.sparkjava:spark-core`. All source lives in `src/main/java/com/selenium/studio/`:

| Class | Role |
|---|---|
| `Main.java` | Entry point — starts the server |
| `SeleniumStudioV2.java` | HTTP server + all route handlers |
| `HtmlBuilder.java` | Generates the entire web UI as a Java string |
| `TestEngine.java` | Orchestrates multi-browser test execution |
| `StepRunner.java` | Executes individual test steps against a WebDriver |
| `DriverFactory.java` | Creates Chrome/Firefox/Edge drivers via Selenium Manager |
| `ApiCaptureManager.java` | CDP network capture (Chrome only, v85 devtools API) |
| `CodeGenerator.java` | Exports test config as standalone `GeneratedTest.java` |
| `TestConfig.java` / `TestStep.java` | Data models for test scripts |
| `JsonUtil.java` | Gson-based JSON parsing |

**HTTP Routes:** `GET /` (UI), `POST /run`, `POST /stop`, `GET /status`, `GET /logs`, `POST /clearlogs`, `GET /apilogs`, `POST /clearapilogs`, `POST /gencode`, `POST /setapicapture`

**Test scripts** are saved as JSON files (e.g., `data/scripts/script_*.json`) with a structure of `{name, url, steps: [{action, selector, value}]}`.

## DreamcastAutomatonTool Structure

Test classes in `src/test/java/` are organized by package:

- `dreamcastauto/` — Multiple versioned automation builder implementations (Swing GUI + WebDriver)
- `seleniumcommand/` — Site-specific automation (Flipkart, XPath utilities)
- `com/selenium/studio/` — SeleniumStudio test variants
- `tktplz/` — Tktplz site automation

The `data/` directory holds scripts, results, and scraped data. Screenshots go to `screenshots/`.

## Key Dependencies

| Dependency | Version | Purpose |
|---|---|---|
| `selenium-java` | 4.18.1 | Browser automation core |
| `webdrivermanager` | 5.8.0 | Auto browser driver management (DreamcastAutomatonTool only) |
| `testng` | 7.11.0 | Test framework |
| `junit-jupiter` | 5.11.0 | Test framework |
| `poi` / `poi-ooxml` | 5.4.0 | Excel file read/write |
| `extentreports` | 5.1.2 | HTML test reports |
| `rest-assured` | 5.4.0 | API testing |
| `spark-core` | 2.9.4 | Embedded HTTP server (DreamcastAutomatonTool) |
| `gson` | 2.10.1 | JSON parsing (SeleniumStudio) |
| `selenium-devtools-v85` | 4.18.1 | CDP API capture (SeleniumStudio) |

## Notes

- **No CI/CD configured** — all testing is local.
- **Browser drivers** are auto-managed by Selenium Manager (no manual download needed).
- **API Capture** (CDP) only works with Chrome, not Firefox/Edge.
- The `ApiCaptureManager` uses concrete `v85` typed classes to avoid Java wildcard generics issues with `Event<?>` listeners — keep this pattern when extending.
- Eclipse `.classpath` and `.project` files are present; Maven is the source of truth for dependencies.
