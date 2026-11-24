package com.sos.model;

import com.sos.util.Json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * One SOS alert. The dispatcher THREAD changes delivery statuses while the HTTP thread
 * may read them, so the methods touching shared fields are synchronized.
 */
public class Alert implements Identifiable, Jsonable {
    public static final String PENDING = "PENDING";
    public static final String DELIVERED = "DELIVERED (simulated)";

    private int id;
    private final int userId;
    private final Emergency emergency;
    private final String location;
    private final String description;
    private final String message;      // the text sent to every contact (simulated)
    private final long createdAt;
    private final List<Delivery> deliveries;
    private boolean resolved;          // true after the user presses "I am safe"

    /** Constructor 1: a brand new alert (time = now, not resolved). */
    public Alert(int userId, Emergency emergency, String location, String description,
                 String message, List<Delivery> deliveries) {
        this(userId, emergency, location, description, message, System.currentTimeMillis(), deliveries, false);
    }

    /** Constructor 2: used when an old alert is loaded from the CSV file. */
    public Alert(int userId, Emergency emergency, String location, String description,
                 String message, long createdAt, List<Delivery> deliveries, boolean resolved) {
        this.userId = userId;
        this.emergency = emergency;
        this.location = location;
        this.description = description;
        this.message = message;
        this.createdAt = createdAt;
        this.deliveries = deliveries;
        this.resolved = resolved;
    }

    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public Emergency getEmergency() { return emergency; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getMessage() { return message; }
    public long getCreatedAt() { return createdAt; }

    public synchronized boolean isResolved() { return resolved; }
    public synchronized void markResolved() { this.resolved = true; }

    public synchronized void markDelivered(int index) {
        deliveries.get(index).setStatus(DELIVERED);
    }

    public synchronized boolean isAllDelivered() {
        for (Delivery d : deliveries) {
            if (!d.getStatus().equals(DELIVERED)) return false;
        }
        return true;
    }

    /** A copy of the list, safe to read while the other thread is working. */
    public synchronized List<Delivery> getDeliveriesSnapshot() {
        List<Delivery> copy = new ArrayList<>();
        for (Delivery d : deliveries) copy.add(new Delivery(d.getContactInfo(), d.getStatus()));
        return copy;
    }

    public String getFormattedDate() {
        return new SimpleDateFormat("dd MMM yyyy").format(new Date(createdAt));
    }

    public String getFormattedClock() {
        return new SimpleDateFormat("hh:mm a").format(new Date(createdAt));
    }

    public String getFormattedTime() {
        return getFormattedDate() + ", " + getFormattedClock();
    }

    @Override
    public synchronized String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"id\":").append(id);
        sb.append(",\"category\":").append(Json.quote(emergency.getKey()));
        sb.append(",\"categoryLabel\":").append(Json.quote(emergency.getLabel()));
        sb.append(",\"severity\":").append(emergency.getSeverity());
        sb.append(",\"advice\":").append(Json.quote(emergency.getAdvice()));
        sb.append(",\"location\":").append(Json.quote(location));
        sb.append(",\"description\":").append(Json.quote(description));
        sb.append(",\"message\":").append(Json.quote(message));
        sb.append(",\"date\":").append(Json.quote(getFormattedDate()));
        sb.append(",\"clock\":").append(Json.quote(getFormattedClock()));
        sb.append(",\"time\":").append(Json.quote(getFormattedTime()));
        sb.append(",\"timestamp\":").append(createdAt);
        sb.append(",\"resolved\":").append(resolved);
        sb.append(",\"allDelivered\":").append(isAllDelivered());
        sb.append(",\"deliveries\":[");
        for (int i = 0; i < deliveries.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append("{\"contact\":").append(Json.quote(deliveries.get(i).getContactInfo()));
            sb.append(",\"status\":").append(Json.quote(deliveries.get(i).getStatus())).append('}');
        }
        sb.append("]}");
        return sb.toString();
    }
}
