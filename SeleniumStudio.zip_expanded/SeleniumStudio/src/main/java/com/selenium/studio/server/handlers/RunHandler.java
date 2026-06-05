package com.selenium.studio.server.handlers;

import com.selenium.studio.engine.TestEngine;
import com.selenium.studio.model.TestConfig;
import com.selenium.studio.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class RunHandler extends BaseHandler {

    private final TestEngine engine;

    public RunHandler(TestEngine engine) {
        this.engine = engine;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        if (engine.isRunning.get()) {
            respond(ex, 409, "{\"error\":\"Already running\"}");
            return;
        }
        String body = readBody(ex);
        respond(ex, 200, "{\"started\":true}");
        new Thread(() -> {
            try {
                TestConfig cfg = JsonUtil.parseConfig(body);
                engine.execute(cfg);
            } catch (Exception e) {
                engine.runLog.add(ts() + "|FAIL|ERROR: " + e.getMessage());
                engine.isRunning.set(false);
            }
        }).start();
    }
}
