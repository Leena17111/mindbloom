package com.mindbloom.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mindbloom.model.AssessmentResult;

@Repository
@Transactional
public class AssessmentResultDaoHibernate implements AssessmentResultDao {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public void save(AssessmentResult result) {
        getSession().save(result);
    }

    @Override
    public List<AssessmentResult> findByStudentId(int studentId) {
        return getSession()
                .createQuery(
                        "FROM AssessmentResult r WHERE r.studentId = :studentId ORDER BY r.completedAt DESC",
                        AssessmentResult.class
                )
                .setParameter("studentId", studentId)
                .list();
    }
}
