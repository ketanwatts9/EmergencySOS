package com.sos.server;

import com.sos.exception.AppException;
import com.sos.model.Contact;
import com.sos.service.ContactService;
import com.sos.service.SessionManager;
import com.sos.util.Json;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

/** GET (optional ?q=word) / POST /api/contacts , PUT/DELETE /api/contacts/{id} */
public class ContactHandler extends BaseHandler {
    private final ContactService contactService;

    public ContactHandler(ContactService contactService, SessionManager sessions) {
        super(sessions);
        this.contactService = contactService;
    }

    @Override
    protected void route(HttpExchange ex) throws IOException, AppException {
        int userId = authenticate(ex);
        String method = ex.getRequestMethod();
        String[] parts = segments(ex);
        boolean hasId = parts.length > 3;

        if (method.equals("GET") && !hasId) {
            send(ex, 200, Json.array(contactService.searchContacts(userId, queryParams(ex).get("q"))));

        } else if (method.equals("POST") && !hasId) {
            Map<String, String> f = readForm(ex);
            Contact c = contactService.addContact(userId, f.get("name"), f.get("phone"),
                    f.get("relation"), f.get("priority"));
            send(ex, 201, c.toJson());

        } else if (method.equals("PUT") && hasId) {
            Map<String, String> f = readForm(ex);
            Contact c = contactService.updateContact(userId, parseId(parts[3]), f.get("name"),
                    f.get("phone"), f.get("relation"), f.get("priority"));
            send(ex, 200, c.toJson());

        } else if (method.equals("DELETE") && hasId) {
            contactService.deleteContact(userId, parseId(parts[3]));
            send(ex, 200, Json.message("message", "Contact deleted"));

        } else {
            throw methodNotAllowed();
        }
    }
}
