package com.mindbloom.dao;

import java.util.List;

import com.mindbloom.model.AssessmentQuestion;

public interface AssessmentQuestionDao {
    void save(AssessmentQuestion question);
    void deleteByAssessmentId(int assessmentId);
    List<AssessmentQuestion> findByAssessmentId(int assessmentId);
}
