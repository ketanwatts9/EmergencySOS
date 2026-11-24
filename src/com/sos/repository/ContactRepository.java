package com.sos.repository;

import com.sos.model.Contact;

import java.util.List;

public interface ContactRepository extends Repository<Contact> {
    List<Contact> findByUserId(int userId);
}
