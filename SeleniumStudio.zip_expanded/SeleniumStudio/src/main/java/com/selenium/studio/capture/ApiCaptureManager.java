package com.selenium.studio.capture;

import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v85.network.Network;
import org.openqa.selenium.devtools.v85.network.model.RequestWillBeSent;
import org.openqa.selenium.devtools.v85.network.model.ResponseReceived;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Manages Chrome DevTools Protocol (CDP) network capture.
 * Primary path uses concrete v85 typed classes to avoid Event<?>/Consumer<Object> mismatch.
 * Falls back to reflection for other Chrome versions.
 */
public class ApiCaptureManager {

    private final List<String> apiLog;
    private final Runnable     onActivated;

    public ApiCaptureManager(List<String> apiLog, Runnable onActivated) {
        this.apiLog      = apiLog;
        this.onActivated = onActivated;
    }

    public boolean setup(HasDevTools driver) {
        try {
            DevTools devTools = driver.getDevTools();
            devTools.createSession();

            try {
                devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

                devTools.addListener(Network.requestWillBeSent(), (RequestWillBeSent req) -> {
                    try {
                        apiLog.add(ts() + "|REQ|" + req.getRequest().getMethod() + " " + req.getRequest().getUrl());
                    } catch (Exception ignored) {}
                });

                devTools.addListener(Network.responseReceived(), (ResponseReceived res) -> {
                    try {
                        apiLog.add(ts() + "|RES|" + res.getResponse().getStatus() + " " + res.getResponse().getUrl());
                    } catch (Exception ignored) {}
                });

                if (onActivated != null) onActivated.run();
                return true;

            } catch (NoClassDefFoundError | Exception versionEx) {
                return setupViaReflection(devTools);
            }

        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean setupViaReflection(DevTools devTools) {
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

                Object enableCmd = netClass.getMethod("enable", Optional.class, Optional.class, Optional.class)
                        .invoke(null, Optional.empty(), Optional.empty(), Optional.empty());
                devTools.send((org.openqa.selenium.devtools.Command) enableCmd);

                Object reqEvent  = netClass.getMethod("requestWillBeSent").invoke(null);
                Object respEvent = netClass.getMethod("responseReceived").invoke(null);

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
                        Object resp      = event.getClass().getMethod("getResponse").invoke(event);
                        String url       = resp.getClass().getMethod("getUrl").invoke(resp).toString();
                        Object statusObj = resp.getClass().getMethod("getStatus").invoke(resp);
                        apiLog.add(ts() + "|RES|" + (statusObj != null ? statusObj : "?") + " " + url);
                    } catch (Exception ignored) {}
                });

                if (onActivated != null) onActivated.run();
                return true;

            } catch (ClassNotFoundException ignored) {
            } catch (Exception e) {
                break;
            }
        }
        return false;
    }

    private static String ts() {
        return DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());
    }
}
