package com.sos.repository;

import com.sos.exception.AppException;
import com.sos.model.Alert;
import com.sos.model.Delivery;
import com.sos.model.Emergency;
import com.sos.model.EmergencyFactory;
import com.sos.util.CsvUtil;

import java.util.ArrayList;
import java.util.List;

/** CSV line: id,userId,categoryKey,location,description,timeMillis,deliveries,resolved  (deliveries = info^status~info^status),message */
public class FileAlertRepository extends CsvRepository<Alert> implements AlertRepository {

    public FileAlertRepository(String path) throws AppException { super(path); }

    @Override
    protected String toCsv(Alert a) {
        StringBuilder deliveries = new StringBuilder();
        for (Delivery d : a.getDeliveriesSnapshot()) {
            if (deliveries.length() > 0) deliveries.append('~');
            deliveries.append(CsvUtil.encode(d.getContactInfo())).append('^').append(CsvUtil.encode(d.getStatus()));
        }
        return a.getId() + "," + a.getUserId() + "," + a.getEmergency().getKey() + ","
                + CsvUtil.encode(a.getLocation()) + "," + CsvUtil.encode(a.getDescription()) + ","
                + a.getCreatedAt() + "," + deliveries + "," + (a.isResolved() ? "1" : "0") + ","
                + CsvUtil.encode(a.getMessage());
    }

    @Override
    protected Alert fromCsv(String line) throws AppException {
        String[] p = line.split(",", -1);
        List<Delivery> deliveries = new ArrayList<>();
        if (!p[6].isEmpty()) {
            for (String part : p[6].split("~")) {
                String[] pair = part.split("\\^", -1);
                deliveries.add(new Delivery(CsvUtil.decode(pair[0]), CsvUtil.decode(pair[1])));
            }
        }
        Emergency emergency = EmergencyFactory.create(p[2]);
        String location = CsvUtil.decode(p[3]);
        String description = CsvUtil.decode(p[4]);
        String message = p.length > 8 ? CsvUtil.decode(p[8]) : emergency.buildMessage("User", location, description);
        Alert a = new Alert(Integer.parseInt(p[1]), emergency, location, description, message,
                Long.parseLong(p[5]), deliveries, p.length > 7 && p[7].equals("1"));
        a.setId(Integer.parseInt(p[0]));
        return a;
    }

    @Override
    public synchronized List<Alert> findByUserId(int userId) {
        List<Alert> result = new ArrayList<>();
        for (Alert a : items.values()) {
            if (a.getUserId() == userId) result.add(a);
        }
        return result;
    }
}
