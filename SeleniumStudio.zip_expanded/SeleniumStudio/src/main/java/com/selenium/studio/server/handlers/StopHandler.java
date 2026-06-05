package com.selenium.studio.server.handlers;

import com.selenium.studio.engine.TestEngine;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class StopHandler extends BaseHandler {

    private final TestEngine engine;

    public StopHandler(TestEngine engine) {
        this.engine = engine;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        engine.stop();
        respond(ex, 200, "{\"stopped\":true}");
    }
}
