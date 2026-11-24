package com.sos.repository;

import com.sos.exception.AppException;
import com.sos.model.Identifiable;

import java.util.List;

/** Generic repository: T can be User, Contact or Alert ("T extends Identifiable" = bounded type). */
public interface Repository<T extends Identifiable> {
    void save(T item) throws AppException;        // insert (id == 0) or update
    T findById(int id);                           // null if not found
    List<T> findAll();
    void delete(int id) throws AppException;
}
