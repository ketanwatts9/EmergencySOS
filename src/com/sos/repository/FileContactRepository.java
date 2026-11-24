package com.sos.repository;

import com.sos.exception.AppException;
import com.sos.model.Contact;
import com.sos.util.CsvUtil;

import java.util.ArrayList;
import java.util.List;

public class FileContactRepository extends CsvRepository<Contact> implements ContactRepository {

    public FileContactRepository(String path) throws AppException { super(path); }

    @Override
    protected String toCsv(Contact c) {
        return c.getId() + "," + c.getUserId() + "," + CsvUtil.encode(c.getName()) + ","
                + CsvUtil.encode(c.getPhone()) + "," + CsvUtil.encode(c.getRelation()) + "," + c.getPriority();
    }

    @Override
    protected Contact fromCsv(String line) {
        String[] p = line.split(",", -1);
        Contact c = new Contact(Integer.parseInt(p[1]), CsvUtil.decode(p[2]), CsvUtil.decode(p[3]),
                CsvUtil.decode(p[4]), Integer.parseInt(p[5]));
        c.setId(Integer.parseInt(p[0]));
        return c;
    }

    @Override
    public synchronized List<Contact> findByUserId(int userId) {
        List<Contact> result = new ArrayList<>();
        for (Contact c : items.values()) {
            if (c.getUserId() == userId) result.add(c);
        }
        return result;
    }
}
