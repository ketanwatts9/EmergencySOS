package com.sos.model;

/** Anything that can describe itself as JSON text for the browser. */
public interface Jsonable {
    String toJson();
}
