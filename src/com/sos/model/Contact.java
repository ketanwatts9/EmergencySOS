package com.sos.model;

import com.sos.util.Json;

/** Comparable: contacts sort by priority (1 = notify first), then by name. */
public class Contact implements Identifiable, Jsonable, Notifiable, Comparable<Contact> {
    private int id;
    private final int userId;
    private String name;
    private String phone;
    private String relation;
    private int priority;

    public Contact(int userId, String name, String phone, String relation, int priority) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.relation = relation;
        this.priority = priority;
    }

    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getRelation() { return relation; }
    public int getPriority() { return priority; }

    public void update(String name, String phone, String relation, int priority) {
        this.name = name;
        this.phone = phone;
        this.relation = relation;
        this.priority = priority;
    }

    @Override
    public int compareTo(Contact other) {
        if (this.priority != other.priority) return Integer.compare(this.priority, other.priority);
        return this.name.compareToIgnoreCase(other.name);
    }

    /** Simulated SMS: only builds the message text, nothing is really sent. */
    @Override
    public String sendAlert(Alert alert) {
        StringBuilder sb = new StringBuilder();
        sb.append("SMS (simulated) to ").append(name).append(" [").append(phone).append("]: ");
        sb.append(alert.getMessage());
        return sb.toString();
    }

    @Override
    public String toJson() {
        return "{\"id\":" + id + ",\"name\":" + Json.quote(name) + ",\"phone\":" + Json.quote(phone)
                + ",\"relation\":" + Json.quote(relation) + ",\"priority\":" + priority + "}";
    }
}
