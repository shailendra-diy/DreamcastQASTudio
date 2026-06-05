package com.selenium.studio.server.handlers;

import com.selenium.studio.codegen.CodeGenerator;
import com.selenium.studio.model.TestConfig;
import com.selenium.studio.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CodeGenHandler extends BaseHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        String body  = readBody(ex);
        TestConfig cfg   = JsonUtil.parseConfig(body);
        String     code  = CodeGenerator.generate(cfg);
        byte[]     bytes = code.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type",                 "text/plain; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        ex.sendResponseHeaders(200, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }
}
