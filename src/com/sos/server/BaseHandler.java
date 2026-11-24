package com.sos.server;

import com.sos.exception.AppException;
import com.sos.exception.AuthenticationException;
import com.sos.exception.InvalidInputException;
import com.sos.service.SessionManager;
import com.sos.util.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** Common code for all API handlers. Subclasses only write route(). */
public abstract class BaseHandler implements HttpHandler {
    protected final SessionManager sessions;

    protected BaseHandler(SessionManager sessions) { this.sessions = sessions; }

    protected abstract void route(HttpExchange ex) throws IOException, AppException;

    @Override
    public final void handle(HttpExchange ex) throws IOException {
        try {
            route(ex);
        } catch (AppException e) {                       // our own errors -> proper status code
            send(ex, e.getStatus(), Json.message("error", e.getMessage()));
        } catch (Exception e) {                          // anything unexpected
            e.printStackTrace();
            send(ex, 500, Json.message("error", "Internal server error"));
        } finally {
            ex.close();
        }
    }

    /** Reads a form body like  name=Ketan&email=a%40b.com  into a Map. */
    protected Map<String, String> readForm(HttpExchange ex) throws IOException {
        InputStream in = ex.getRequestBody();
        String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        return parseParams(body);
    }

    protected Map<String, String> queryParams(HttpExchange ex) {
        String query = ex.getRequestURI().getRawQuery();
        return parseParams(query == null ? "" : query);
    }

    private Map<String, String> parseParams(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw.isEmpty()) return map;
        for (String pair : raw.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(key, value);
        }
        return map;
    }

    protected void send(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);
        OutputStream out = ex.getResponseBody();
        out.write(bytes);
        out.close();
    }

    protected String getToken(HttpExchange ex) {
        String header = ex.getRequestHeaders().getFirst("Authorization");
        if (header != null && header.startsWith("Bearer ")) return header.substring(7);
        return null;
    }

    /** Returns the logged-in user's id or throws AuthenticationException. */
    protected int authenticate(HttpExchange ex) throws AuthenticationException {
        return sessions.getUserId(getToken(ex));
    }

    protected String[] segments(HttpExchange ex) {
        return ex.getRequestURI().getPath().split("/");   // "/api/contacts/5" -> ["", "api", "contacts", "5"]
    }

    protected int parseId(String text) throws InvalidInputException {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid id");
        }
    }

    protected AppException methodNotAllowed() { return new AppException(405, "Method not allowed"); }
}
