package com.sos.repository;

import com.sos.model.User;

public interface UserRepository extends Repository<User> {
    User findByEmail(String email);   // null if not found
}
