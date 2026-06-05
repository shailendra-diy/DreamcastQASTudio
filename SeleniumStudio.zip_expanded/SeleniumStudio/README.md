# Selenium Automation Studio v2.0

Visual browser automation tool — build and run Selenium tests without writing code.

---

## 📋 Requirements

- Java 17+
- Maven 3.6+
- Chrome / Firefox / Edge installed
- WebDriver auto-managed by Selenium Manager (no manual driver download needed)

---

## 🚀 How to Run

### Option 1 — Run from Eclipse

1. `File → Import → Existing Maven Projects` → select this folder
2. Right-click project → `Maven → Update Project`
3. Open `src/main/java/com/selenium/studio/Main.java`
4. Right-click → `Run As → Java Application`
5. Browser opens automatically at `http://localhost:8769`

### Option 2 — Run from command line

```bash
mvn package -q
java -jar target/selenium-studio-2.0.jar
```

Then open: **http://localhost:8769**

---

## 📁 Project Structure

```
SeleniumStudio/
├── pom.xml                          ← Maven dependencies
├── README.md
└── src/main/java/com/selenium/studio/
    ├── Main.java                    ← Entry point (run this)
    ├── SeleniumStudioV2.java        ← HTTP server + route handlers
    ├── HtmlBuilder.java             ← Builds the web UI HTML
    ├── TestEngine.java              ← Orchestrates test execution
    ├── StepRunner.java              ← Runs individual test steps
    ├── DriverFactory.java           ← Creates Chrome/Firefox/Edge drivers
    ├── ApiCaptureManager.java       ← CDP network capture (FIXED)
    ├── CodeGenerator.java           ← Generates standalone Java test file
    ├── TestConfig.java              ← Configuration model
    ├── TestStep.java                ← Step model
    └── JsonUtil.java                ← Gson-based JSON parsing
```

---

## ✅ Features

| Feature | Description |
|---|---|
| 18 Actions | Click, Type, Dropdown, Get Text, Verify Text, Navigate, Screenshot, Scroll, Hover, JS Click, Alert, Switch Frame/Window, Print, Wait |
| Multi-browser | Chrome, Firefox, Edge — run all at once |
| API Capture | CDP network logging (Chrome only) — request/response log |
| Code Generator | Exports as standalone `GeneratedTest.java` |
| Save / Load | Projects saved as `.json` files |
| Auto Screenshot | Captures screenshot on step failure |
| Export Logs | Exec log as `.txt`, API log as `.txt` or `.json` |

---

## 🔧 CDP API Capture Fix

**Original error:**
```
The method addListener(Event<X>, Consumer<X>) is not applicable 
for the arguments (Event<capture#7-of ?>, (Object event) -> {})
```

**Root cause:** Java generics — `Event<?>` (wildcard) cannot be matched to `Consumer<Object>`.

**Fix in `ApiCaptureManager.java`:**
- **Primary path:** Uses concrete typed `v85` classes (`RequestWillBeSent`, `ResponseReceived`) so the compiler knows the exact type `X` — no wildcard issue.
- **Fallback:** Casts `Event<?>` to raw `Event` (with `@SuppressWarnings("rawtypes")`) and uses raw `Consumer` — raw types bypass the generic check entirely.

---

## 📦 Dependencies (pom.xml)

| Dependency | Version | Purpose |
|---|---|---|
| selenium-java | 4.18.1 | Core Selenium |
| selenium-devtools-v85 | 4.18.1 | CDP API capture |
| commons-io | 2.15.1 | Screenshot file saving |
| gson | 2.10.1 | JSON parsing |
| slf4j-simple | 2.0.9 | Logging |

---

## 💡 Tips

- Chrome browser selection must be ON (blue) before running
- API Capture only works with Chrome (not Firefox/Edge)
- Use `./screenshots` folder — it's created automatically
- Save your project before closing — use the **Save** button (top right)
