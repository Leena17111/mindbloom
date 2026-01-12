package com.mindbloom.dao;

import java.util.List;

import com.mindbloom.model.Assessment;

public interface AssessmentDao {
    Assessment findByResourceId(int resourceId);
    void save(Assessment assessment);
    List<Assessment> findAll();
}
