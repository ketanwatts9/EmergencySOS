package com.sos.util;

import com.sos.model.Jsonable;

import java.util.List;

/** Builds JSON text with StringBuilder (no library needed). */
public final class Json {
    private Json() { }

    public static String quote(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\t': sb.append("\\t");  break;
                case '\r': break;
                default:   sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    public static String message(String key, String value) {
        return "{" + quote(key) + ":" + quote(value) + "}";
    }

    /** "? extends Jsonable" = a list of Contacts, Alerts, Emergencies ... anything Jsonable (wildcard). */
    public static String array(List<? extends Jsonable> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(items.get(i).toJson());
        }
        return sb.append(']').toString();
    }
}
