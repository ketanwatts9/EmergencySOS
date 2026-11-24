package com.sos.repository;

import com.sos.model.Alert;

import java.util.List;

public interface AlertRepository extends Repository<Alert> {
    List<Alert> findByUserId(int userId);
}
