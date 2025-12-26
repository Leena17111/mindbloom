package com.mindbloom.dao;

import java.util.List;

import com.mindbloom.model.AssessmentQuestion;

public interface AssessmentQuestionDao {
    void save(AssessmentQuestion question);
    void deleteByAssessmentId(Long assessmentId);
    List<AssessmentQuestion> findByAssessmentId(Long assessmentId);
}
