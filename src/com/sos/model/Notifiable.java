package com.sos.model;

/** Anything that can receive an SOS alert (our Contact class). */
public interface Notifiable {
    String sendAlert(Alert alert);
}
