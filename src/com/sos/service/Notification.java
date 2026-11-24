package com.sos.service;

import com.sos.model.Alert;
import com.sos.model.Contact;

/** One job for the dispatcher thread: "notify this contact about this alert". */
public class Notification {
    private final Alert alert;
    private final Contact contact;
    private final int index;   // position of this contact in the alert's delivery list

    public Notification(Alert alert, Contact contact, int index) {
        this.alert = alert;
        this.contact = contact;
        this.index = index;
    }

    public Alert getAlert() { return alert; }
    public Contact getContact() { return contact; }
    public int getIndex() { return index; }
}
