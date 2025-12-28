package com.mindbloom.dao;

import com.mindbloom.model.Assessment;

public interface AssessmentDao {
    Assessment findByResourceId(int resourceId);
    void save(Assessment assessment);
}
