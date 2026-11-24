package com.sos.server;

import com.sos.exception.AppException;
import com.sos.model.Alert;
import com.sos.model.EmergencyFactory;
import com.sos.service.AlertService;
import com.sos.service.SessionManager;
import com.sos.util.Json;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

/**
 * GET /api/alerts/categories | GET /api/alerts/stats | GET /api/alerts?category=X&q=word&limit=N
 * POST /api/alerts | PUT /api/alerts/{id}/resolve
 */
public class AlertHandler extends BaseHandler {
    private final AlertService alertService;

    public AlertHandler(AlertService alertService, SessionManager sessions) {
        super(sessions);
        this.alertService = alertService;
    }

    @Override
    protected void route(HttpExchange ex) throws IOException, AppException {
        int userId = authenticate(ex);
        String method = ex.getRequestMethod();
        String[] parts = segments(ex);
        String sub = parts.length > 3 ? parts[3] : "";

        if (method.equals("GET") && sub.equals("categories")) {
            send(ex, 200, Json.array(EmergencyFactory.all()));

        } else if (method.equals("GET") && sub.equals("stats")) {
            send(ex, 200, statsJson(alertService.getStats(userId)));

        } else if (method.equals("PUT") && parts.length > 4 && parts[4].equals("resolve")) {
            Alert alert = alertService.resolveAlert(userId, parseId(parts[3]));
            send(ex, 200, alert.toJson());

        } else if (method.equals("GET")) {
            Map<String, String> q = queryParams(ex);
            send(ex, 200, Json.array(alertService.getHistory(userId, q.get("category"),
                    q.get("q"), parseLimit(q.get("limit")))));

        } else if (method.equals("POST")) {
            Map<String, String> f = readForm(ex);
            Alert alert = alertService.triggerSos(userId, f.get("category"), f.get("location"), f.get("description"));
            send(ex, 201, alert.toJson());

        } else {
            throw methodNotAllowed();
        }
    }

    private int parseLimit(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;   // missing or invalid -> no limit
        }
    }

    private String statsJson(Map<String, Integer> stats) {
        StringBuilder sb = new StringBuilder("[");
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            if (sb.length() > 1) sb.append(',');
            sb.append("{\"label\":").append(Json.quote(entry.getKey()))
              .append(",\"count\":").append(entry.getValue()).append('}');
        }
        return sb.append(']').toString();
    }
}
