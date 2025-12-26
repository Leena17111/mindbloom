package com.mindbloom.dao;

import com.mindbloom.model.Assessment;

public interface AssessmentDao {
    Assessment findByResourceId(Long resourceId);
    void save(Assessment assessment);
}
