package com.selenium.studio;

import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v85.network.Network;
import org.openqa.selenium.devtools.v85.network.model.RequestWillBeSent;
import org.openqa.selenium.devtools.v85.network.model.ResponseReceived;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Manages Chrome DevTools Protocol (CDP) API capture.
 *
 * FIX: The original code used raw Event<?> + Object consumer which causes
 * "incompatible type for lambda parameter" because addListener(Event<X>, Consumer<X>)
 * requires matching generic X. Solution: use concrete typed v85 classes directly.
 * If v85 is not the right Chrome version, we fall back to reflection gracefully.
 */
public class ApiCaptureManager {

    private final List<String> apiLog;
    private final Runnable     logger;

    public ApiCaptureManager(List<String> apiLog, Runnable onActivated) {
        this.apiLog = apiLog;
        this.logger = onActivated;
    }

    /**
     * Sets up CDP network listener on the given driver.
     * Returns true if successfully activated.
     */
    public boolean setup(HasDevTools driver) {
        try {
            DevTools devTools = driver.getDevTools();
            devTools.createSession();

            // ── Primary path: use concrete v85 typed classes ──────────────
            // This avoids the Event<?>/Consumer<Object> mismatch entirely.
            try {
                devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

                // Consumer<RequestWillBeSent> — fully typed, no generics issue
                devTools.addListener(
                    Network.requestWillBeSent(),
                    (RequestWillBeSent req) -> {
                        try {
                            String method = req.getRequest().getMethod();
                            String url    = req.getRequest().getUrl();
                            apiLog.add(ts() + "|REQ|" + method + " " + url);
                        } catch (Exception ignored) {}
                    }
                );

                // Consumer<ResponseReceived> — fully typed, no generics issue
                devTools.addListener(
                    Network.responseReceived(),
                    (ResponseReceived res) -> {
                        try {
                            String url    = res.getResponse().getUrl();
                            int    status = res.getResponse().getStatus();
                            apiLog.add(ts() + "|RES|" + status + " " + url);
                        } catch (Exception ignored) {}
                    }
                );

                if (logger != null) logger.run();
                return true;

            } catch (NoClassDefFoundError | Exception versionEx) {
                // ── Fallback: reflection for other CDP versions ────────────
                return setupViaReflection(devTools);
            }

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Reflection-based fallback for Chrome versions where v85 classes differ.
     * Uses raw types (suppressed) to work around the generic Event<?> problem.
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    private boolean setupViaReflection(DevTools devTools) {
        // Try common CDP version class names
        String[] versionClasses = {
            "org.openqa.selenium.devtools.v130.network.Network",
            "org.openqa.selenium.devtools.v129.network.Network",
            "org.openqa.selenium.devtools.v128.network.Network",
            "org.openqa.selenium.devtools.v120.network.Network",
            "org.openqa.selenium.devtools.v114.network.Network",
            "org.openqa.selenium.devtools.v112.network.Network",
            "org.openqa.selenium.devtools.v108.network.Network",
            "org.openqa.selenium.devtools.v85.network.Network"
        };

        for (String className : versionClasses) {
            try {
                Class<?> netClass = Class.forName(className);

                // Enable network via static method
                Object enableCmd = netClass.getMethod("enable",
                    Optional.class, Optional.class, Optional.class)
                    .invoke(null, Optional.empty(), Optional.empty(), Optional.empty());
                devTools.send((org.openqa.selenium.devtools.Command) enableCmd);

                // Get event objects via static methods (not fields — more reliable)
                Object reqEvent  = netClass.getMethod("requestWillBeSent").invoke(null);
                Object respEvent = netClass.getMethod("responseReceived").invoke(null);

                // KEY FIX: Cast Event<?> to raw Event, then Consumer<Object> works
                // because raw Consumer is compatible with raw addListener
                org.openqa.selenium.devtools.Event rawReqEvent  = (org.openqa.selenium.devtools.Event) reqEvent;
                org.openqa.selenium.devtools.Event rawRespEvent = (org.openqa.selenium.devtools.Event) respEvent;

                devTools.addListener(rawReqEvent, (Consumer) (Object event) -> {
                    try {
                        Object request = event.getClass().getMethod("getRequest").invoke(event);
                        String method  = request.getClass().getMethod("getMethod").invoke(request).toString();
                        String url     = request.getClass().getMethod("getUrl").invoke(request).toString();
                        apiLog.add(ts() + "|REQ|" + method + " " + url);
                    } catch (Exception ignored) {}
                });

                devTools.addListener(rawRespEvent, (Consumer) (Object event) -> {
                    try {
                        Object resp   = event.getClass().getMethod("getResponse").invoke(event);
                        String url    = resp.getClass().getMethod("getUrl").invoke(resp).toString();
                        Object statusObj = resp.getClass().getMethod("getStatus").invoke(resp);
                        String status = statusObj != null ? statusObj.toString() : "?";
                        apiLog.add(ts() + "|RES|" + status + " " + url);
                    } catch (Exception ignored) {}
                });

                if (logger != null) logger.run();
                return true;

            } catch (ClassNotFoundException ignored) {
                // Try next version
            } catch (Exception e) {
                // Class found but something else failed — stop trying
                break;
            }
        }
        return false;
    }

    private static String ts() {
        return java.time.format.DateTimeFormatter
            .ofPattern("HH:mm:ss")
            .format(java.time.LocalDateTime.now());
    }
}
