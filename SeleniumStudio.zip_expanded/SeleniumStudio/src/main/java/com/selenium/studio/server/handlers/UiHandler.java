package com.selenium.studio.server.handlers;

import com.selenium.studio.ui.HtmlBuilder;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class UiHandler extends BaseHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String html  = HtmlBuilder.build();
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(200, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }
}
