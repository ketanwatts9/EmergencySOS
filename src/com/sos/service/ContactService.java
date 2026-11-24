package com.sos.service;

import com.sos.exception.AppException;
import com.sos.exception.DuplicateException;
import com.sos.exception.InvalidInputException;
import com.sos.exception.NotFoundException;
import com.sos.model.Contact;
import com.sos.repository.ContactRepository;
import com.sos.util.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactService {
    private static final int MAX_CONTACTS = 10;

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public synchronized Contact addContact(int userId, String name, String phone,
                                           String relation, String priority) throws AppException {
        name = Validator.toTitleCase(Validator.requireText(name, "Name", 50));
        phone = Validator.validatePhone(phone);
        relation = Validator.requireText(relation, "Relation", 30);
        int prio = Validator.parsePriority(priority);

        List<Contact> existing = contactRepository.findByUserId(userId);
        if (existing.size() >= MAX_CONTACTS)
            throw new InvalidInputException("You can save at most " + MAX_CONTACTS + " contacts");
        if (phoneAlreadyUsed(existing, phone, 0))
            throw new DuplicateException("A contact with this phone number already exists");

        Contact contact = new Contact(userId, name, phone, relation, prio);
        contactRepository.save(contact);
        return contact;
    }

    /** Contacts in the order they were saved. */
    public List<Contact> getContacts(int userId) {
        return contactRepository.findByUserId(userId);
    }

    /** Contacts sorted by priority: Collections.sort() uses Contact.compareTo(). */
    public List<Contact> getContactsSorted(int userId) {
        List<Contact> list = contactRepository.findByUserId(userId);
        Collections.sort(list);
        return list;
    }

    /**
     * Linear search: look at every contact and keep the ones whose name, relation or phone
     * contains the keyword (ignoring upper/lower case). Empty keyword = all contacts.
     */
    public List<Contact> searchContacts(int userId, String keyword) {
        List<Contact> all = getContactsSorted(userId);
        if (keyword == null || keyword.trim().isEmpty()) return all;

        String word = keyword.trim().toLowerCase();
        List<Contact> found = new ArrayList<>();
        for (Contact c : all) {
            if (c.getName().toLowerCase().contains(word)
                    || c.getRelation().toLowerCase().contains(word)
                    || c.getPhone().contains(word)) {
                found.add(c);
            }
        }
        return found;
    }

    public synchronized Contact updateContact(int userId, int contactId, String name, String phone,
                                              String relation, String priority) throws AppException {
        Contact contact = getOwnedContact(userId, contactId);
        name = Validator.toTitleCase(Validator.requireText(name, "Name", 50));
        phone = Validator.validatePhone(phone);
        relation = Validator.requireText(relation, "Relation", 30);
        int prio = Validator.parsePriority(priority);

        if (phoneAlreadyUsed(contactRepository.findByUserId(userId), phone, contactId))
            throw new DuplicateException("A contact with this phone number already exists");

        contact.update(name, phone, relation, prio);
        contactRepository.save(contact);
        return contact;
    }

    public synchronized void deleteContact(int userId, int contactId) throws AppException {
        getOwnedContact(userId, contactId);
        contactRepository.delete(contactId);
    }

    /** HashSet gives a fast "does this phone already exist?" check. */
    private boolean phoneAlreadyUsed(List<Contact> contacts, String phone, int ignoreId) {
        Set<String> phones = new HashSet<>();
        for (Contact c : contacts) {
            if (c.getId() != ignoreId) phones.add(c.getPhone());
        }
        return phones.contains(phone);
    }

    /** A user must never touch somebody else's contact. */
    private Contact getOwnedContact(int userId, int contactId) throws NotFoundException {
        Contact contact = contactRepository.findById(contactId);
        if (contact == null || contact.getUserId() != userId)
            throw new NotFoundException("Contact not found");
        return contact;
    }
}
