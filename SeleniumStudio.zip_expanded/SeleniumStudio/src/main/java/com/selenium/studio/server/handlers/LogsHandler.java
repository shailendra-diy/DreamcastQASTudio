package com.selenium.studio.server.handlers;

import com.selenium.studio.engine.TestEngine;
import com.selenium.studio.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class LogsHandler extends BaseHandler {

    private final TestEngine engine;

    public LogsHandler(TestEngine engine) {
        this.engine = engine;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        respond(ex, 200, JsonUtil.logsToJson(engine.runLog));
    }
}
