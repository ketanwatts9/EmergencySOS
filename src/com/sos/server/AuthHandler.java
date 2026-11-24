package com.sos.server;

import com.sos.exception.AppException;
import com.sos.model.User;
import com.sos.service.AuthService;
import com.sos.service.SessionManager;
import com.sos.util.Json;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

/** POST /api/auth/register | /login | /logout */
public class AuthHandler extends BaseHandler {
    private final AuthService authService;

    public AuthHandler(AuthService authService, SessionManager sessions) {
        super(sessions);
        this.authService = authService;
    }

    @Override
    protected void route(HttpExchange ex) throws IOException, AppException {
        if (!ex.getRequestMethod().equals("POST")) throw methodNotAllowed();

        String[] parts = segments(ex);
        String action = parts.length > 3 ? parts[3] : "";

        if (action.equals("register")) {
            Map<String, String> f = readForm(ex);
            authService.register(f.get("name"), f.get("email"), f.get("password"));
            send(ex, 201, Json.message("message", "Registration successful. Please login."));

        } else if (action.equals("login")) {
            Map<String, String> f = readForm(ex);
            String token = authService.login(f.get("email"), f.get("password"));
            User user = authService.getUser(sessions.getUserId(token));
            send(ex, 200, "{\"token\":" + Json.quote(token) + ",\"user\":" + user.toJson() + "}");

        } else if (action.equals("logout")) {
            authService.logout(getToken(ex));
            send(ex, 200, Json.message("message", "Logged out"));

        } else {
            throw new AppException(404, "Unknown auth action");
        }
    }
}
