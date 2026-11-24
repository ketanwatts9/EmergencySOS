package com.sos.util;

/** Makes text safe to store in a CSV line (commas and new lines would break the format). */
public final class CsvUtil {
    private CsvUtil() { }

    public static String encode(String s) {
        return s.replace("%", "%25")
                .replace(",", "%2C")
                .replace("\n", "%0A")
                .replace("\r", "")
                .replace("~", "%7E")
                .replace("^", "%5E");
    }

    public static String decode(String s) {
        return s.replace("%5E", "^")
                .replace("%7E", "~")
                .replace("%0A", "\n")
                .replace("%2C", ",")
                .replace("%25", "%");   // must be last
    }
}
