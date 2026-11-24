package com.sos.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Serves the HTML / CSS / JS files of the "web" folder (file handling with byte streams). */
public class StaticFileHandler implements HttpHandler {
    private final File root;

    public StaticFileHandler(String folder) throws IOException {
        this.root = new File(folder).getCanonicalFile();
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            File file = new File(root, path).getCanonicalFile();
            if (!file.getPath().startsWith(root.getPath())) {          // blocks ../../ tricks
                reply(ex, 403, "text/plain", "Forbidden".getBytes());
            } else if (!file.isFile()) {
                reply(ex, 404, "text/plain", "Not found".getBytes());
            } else {
                reply(ex, 200, contentType(file.getName()), readBytes(file));
            }
        } finally {
            ex.close();
        }
    }

    private byte[] readBytes(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int count;
            while ((count = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, count);
            }
            return buffer.toByteArray();
        } finally {
            in.close();
        }
    }

    private void reply(HttpExchange ex, int status, String type, byte[] bytes) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(status, bytes.length);
        OutputStream out = ex.getResponseBody();
        out.write(bytes);
        out.close();
    }

    private String contentType(String name) {
        if (name.endsWith(".html")) return "text/html; charset=UTF-8";
        if (name.endsWith(".css"))  return "text/css; charset=UTF-8";
        if (name.endsWith(".js"))   return "application/javascript; charset=UTF-8";
        if (name.endsWith(".svg"))  return "image/svg+xml";
        if (name.endsWith(".png"))  return "image/png";
        if (name.endsWith(".ico"))  return "image/x-icon";
        if (name.endsWith(".json")) return "application/json; charset=UTF-8";
        return "application/octet-stream";
    }
}
