package com.sos.service;

import com.sos.exception.AppException;
import com.sos.exception.InvalidInputException;
import com.sos.exception.NotFoundException;
import com.sos.model.Alert;
import com.sos.model.Contact;
import com.sos.model.Delivery;
import com.sos.model.Emergency;
import com.sos.model.EmergencyFactory;
import com.sos.repository.AlertRepository;
import com.sos.util.Validator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AlertService {
    private final AlertRepository alertRepository;
    private final ContactService contactService;
    private final AuthService authService;
    private final SharedQueue<Notification> queue;

    public AlertService(AlertRepository alertRepository, ContactService contactService,
                        AuthService authService, SharedQueue<Notification> queue) {
        this.alertRepository = alertRepository;
        this.contactService = contactService;
        this.authService = authService;
        this.queue = queue;
    }

    /** Creates the alert, then hands one job per contact to the dispatcher thread. */
    public Alert triggerSos(int userId, String categoryKey, String location, String description)
            throws AppException {
        Emergency emergency = EmergencyFactory.create(categoryKey);
        location = Validator.requireText(location, "Location", 100);
        description = Validator.requireText(description, "Incident details", 300);

        List<Contact> contacts = contactService.getContacts(userId);
        if (contacts.isEmpty())
            throw new InvalidInputException("Add at least one emergency contact before sending SOS");

        // PriorityQueue gives the most important contact first (uses Contact.compareTo)
        PriorityQueue<Contact> byPriority = new PriorityQueue<>(contacts);
        List<Contact> order = new ArrayList<>();
        List<Delivery> deliveries = new ArrayList<>();
        while (!byPriority.isEmpty()) {
            Contact c = byPriority.poll();
            order.add(c);
            deliveries.add(new Delivery("[P" + c.getPriority() + "] " + c.getName()
                    + " (" + c.getRelation() + ") - " + c.getPhone(), Alert.PENDING));
        }

        String userName = authService.getUser(userId).getName();
        String message = emergency.buildMessage(userName, location, description);
        Alert alert = new Alert(userId, emergency, location, description, message, deliveries);
        alertRepository.save(alert);

        try {
            for (int i = 0; i < order.size(); i++) {
                queue.put(new Notification(alert, order.get(i), i));   // producer
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(500, "SOS was interrupted");
        }
        return alert;
    }

    /**
     * History using Stream API + lambda: filter by category and keyword, newest first, optional limit.
     * categoryKey null / "ALL" = every category. keyword null / "" = no search. limit 0 = no limit.
     */
    public List<Alert> getHistory(int userId, String categoryKey, String keyword, int limit)
            throws AppException {
        Comparator<Alert> newestFirst = (a, b) -> {
            int byTime = Long.compare(b.getCreatedAt(), a.getCreatedAt());
            return byTime != 0 ? byTime : Integer.compare(b.getId(), a.getId());
        };

        Stream<Alert> stream = alertRepository.findByUserId(userId).stream();
        if (categoryKey != null && !categoryKey.isEmpty() && !categoryKey.equalsIgnoreCase("ALL")) {
            final String wanted = EmergencyFactory.create(categoryKey).getKey();
            stream = stream.filter(a -> a.getEmergency().getKey().equals(wanted));
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            final String word = keyword.trim().toLowerCase();
            stream = stream.filter(a -> a.getLocation().toLowerCase().contains(word)
                    || a.getDescription().toLowerCase().contains(word));
        }
        stream = stream.sorted(newestFirst);
        if (limit > 0) stream = stream.limit(limit);
        return stream.collect(Collectors.toList());
    }

    /** "I am safe" button: marks one of the user's own alerts as resolved. */
    public Alert resolveAlert(int userId, int alertId) throws AppException {
        Alert alert = alertRepository.findById(alertId);
        if (alert == null || alert.getUserId() != userId)
            throw new NotFoundException("Alert not found");
        alert.markResolved();
        alertRepository.save(alert);
        return alert;
    }

    /** How many alerts per category. TreeMap keeps the labels in alphabetical order. */
    public Map<String, Integer> getStats(int userId) {
        Map<String, Integer> stats = new TreeMap<>();
        Iterator<Alert> it = alertRepository.findByUserId(userId).iterator();
        while (it.hasNext()) {
            String label = it.next().getEmergency().getLabel();
            Integer old = stats.get(label);
            stats.put(label, old == null ? 1 : old + 1);   // Integer wrapper + autoboxing
        }
        return stats;
    }
}
