package com.mindbloom.dao;

import java.util.List;

import com.mindbloom.model.AssessmentResult;

public interface AssessmentResultDao {

    void save(AssessmentResult result);

    List<AssessmentResult> findByStudentId(int studentId);
    
    AssessmentResult findByStudentAndAssessment(int studentId, int assessmentId);
}
