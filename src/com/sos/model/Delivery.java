package com.sos.model;

/** One line of an alert: which contact was notified and the current status. */
public class Delivery {
    private final String contactInfo;
    private String status;

    public Delivery(String contactInfo, String status) {
        this.contactInfo = contactInfo;
        this.status = status;
    }

    public String getContactInfo() { return contactInfo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
