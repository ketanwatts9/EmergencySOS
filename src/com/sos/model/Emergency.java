package com.sos.model;

import com.sos.util.Json;

/** Abstract parent. Each emergency type decides its own severity and advice (polymorphism). */
public abstract class Emergency implements Jsonable {
    private final String key;
    private final String label;

    protected Emergency(String key, String label) {
        this.key = key;
        this.label = label;
    }

    public String getKey() { return key; }
    public String getLabel() { return label; }

    public abstract int getSeverity();    // 1 (low) to 5 (critical)
    public abstract String getAdvice();   // first-aid / safety tip shown to the user
    public abstract String getContactInstruction();   // what the CONTACTS are asked to do

    /** The text that "is sent" to every contact (simulated). Uses String / StringBuilder methods. */
    public String buildMessage(String userName, String location, String details) {
        String cleanDetails = details.trim();
        while (cleanDetails.endsWith(".")) {
            cleanDetails = cleanDetails.substring(0, cleanDetails.length() - 1);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("EMERGENCY ALERT [").append(label).append(", severity ").append(getSeverity()).append("/5] ");
        sb.append(userName).append(" needs help at ").append(location).append(". ");
        sb.append("Details: ").append(cleanDetails).append(". ");
        sb.append(getContactInstruction());
        return sb.toString();
    }

    @Override
    public String toJson() {
        return "{\"key\":" + Json.quote(key) + ",\"label\":" + Json.quote(label)
                + ",\"severity\":" + getSeverity() + ",\"advice\":" + Json.quote(getAdvice()) + "}";
    }
}
