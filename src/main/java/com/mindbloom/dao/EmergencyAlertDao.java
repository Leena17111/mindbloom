package com.mindbloom.dao;

import java.util.List;

import com.mindbloom.model.EmergencyAlert;

public interface EmergencyAlertDao {

    void save(EmergencyAlert alert);

    List<EmergencyAlert> findAll();

    EmergencyAlert findById(int id);
}
