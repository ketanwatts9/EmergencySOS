package com.sos.model;

/** Anything that is stored in a repository has an id. */
public interface Identifiable {
    int getId();
    void setId(int id);
}
