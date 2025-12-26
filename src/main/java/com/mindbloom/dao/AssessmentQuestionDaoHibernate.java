package com.mindbloom.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mindbloom.model.AssessmentQuestion;

@Repository
@Transactional
public class AssessmentQuestionDaoHibernate implements AssessmentQuestionDao {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public void save(AssessmentQuestion question) {
        getSession().save(question);
    }

    @Override
    public void deleteByAssessmentId(Long assessmentId) {
        getSession()
            .createQuery("DELETE FROM AssessmentQuestion WHERE assessmentId = :aid")
            .setParameter("aid", assessmentId)
            .executeUpdate();
    }

    @Override
    public List<AssessmentQuestion> findByAssessmentId(Long assessmentId) {
        return getSession()
            .createQuery(
                "FROM AssessmentQuestion WHERE assessmentId = :aid ORDER BY questionOrder",
                AssessmentQuestion.class
            )
            .setParameter("aid", assessmentId)
            .list();
    }
}
