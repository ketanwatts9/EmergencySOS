package com.sos.repository;

import com.sos.exception.AppException;
import com.sos.model.User;
import com.sos.util.CsvUtil;

public class FileUserRepository extends CsvRepository<User> implements UserRepository {

    public FileUserRepository(String path) throws AppException { super(path); }

    @Override
    protected String toCsv(User u) {
        return u.getId() + "," + CsvUtil.encode(u.getName()) + "," + CsvUtil.encode(u.getEmail())
                + "," + CsvUtil.encode(u.getPassword());
    }

    @Override
    protected User fromCsv(String line) {
        String[] p = line.split(",", -1);
        User u = new User(CsvUtil.decode(p[1]), CsvUtil.decode(p[2]), CsvUtil.decode(p[3]));
        u.setId(Integer.parseInt(p[0]));
        return u;
    }

    @Override
    public synchronized User findByEmail(String email) {
        for (User u : items.values()) {
            if (u.getEmail().equals(email)) return u;
        }
        return null;
    }
}
