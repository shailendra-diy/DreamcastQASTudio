package com.selenium.studio.server.handlers;

import com.selenium.studio.engine.TestEngine;
import com.selenium.studio.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class ApiLogsHandler extends BaseHandler {

    private final TestEngine engine;

    public ApiLogsHandler(TestEngine engine) {
        this.engine = engine;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        respond(ex, 200, JsonUtil.logsToJson(engine.apiLog));
    }
}
