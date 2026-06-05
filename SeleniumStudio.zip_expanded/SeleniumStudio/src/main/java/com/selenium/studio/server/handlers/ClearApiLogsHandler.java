package com.selenium.studio.server.handlers;

import com.selenium.studio.engine.TestEngine;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class ClearApiLogsHandler extends BaseHandler {

    private final TestEngine engine;

    public ClearApiLogsHandler(TestEngine engine) {
        this.engine = engine;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        engine.apiLog.clear();
        respond(ex, 200, "{\"cleared\":true}");
    }
}
