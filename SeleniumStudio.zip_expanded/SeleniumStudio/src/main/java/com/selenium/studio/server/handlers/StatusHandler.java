package com.selenium.studio.server.handlers;

import com.selenium.studio.engine.TestEngine;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.stream.Collectors;

public class StatusHandler extends BaseHandler {

    private final TestEngine engine;

    public StatusHandler(TestEngine engine) {
        this.engine = engine;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        String stepMap = engine.stepStatus.entrySet().stream()
            .map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"")
            .collect(Collectors.joining(","));
        String json = "{\"running\":"  + engine.isRunning.get()   +
            ",\"pass\":"               + engine.passCount.get()   +
            ",\"fail\":"               + engine.failCount.get()   +
            ",\"currentStep\":"        + engine.currentStep.get() +
            ",\"captureApi\":"         + engine.captureApi.get()  +
            ",\"steps\":{"             + stepMap + "}}";
        respond(ex, 200, json);
    }
}
