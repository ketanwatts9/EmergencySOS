package com.sos.server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * The only "web" part of the Java code: it listens on a port and gives each URL to a handler.
 * Everything else (services, repositories, models) is plain core Java.
 */
public class AppServer {
    private final HttpServer server;

    public AppServer(int port, AuthHandler auth, ContactHandler contacts,
                     AlertHandler alerts, StaticFileHandler files) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/auth", auth);
        server.createContext("/api/contacts", contacts);
        server.createContext("/api/alerts", alerts);
        server.createContext("/", files);          // everything else = HTML/CSS/JS files
        server.setExecutor(null);                  // null = one default thread handles requests
    }

    public void start() { server.start(); }
}
