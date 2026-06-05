package com.selenium.studio.server.handlers;

import com.selenium.studio.engine.TestEngine;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class SetApiCaptureHandler extends BaseHandler {

    private final TestEngine engine;

    public SetApiCaptureHandler(TestEngine engine) {
        this.engine = engine;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        String body    = readBody(ex);
        boolean enabled = body.contains("\"enabled\":true");
        engine.captureApi.set(enabled);
        respond(ex, 200, "{\"captureApi\":" + enabled + "}");
    }
}
