package com.mindbloom.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mindbloom.model.Assessment;

@Repository
@Transactional
public class AssessmentDaoHibernate implements AssessmentDao {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Assessment findByResourceId(int resourceId) {
        return getSession()
                .createQuery(
                        "FROM Assessment WHERE resourceId = :rid",
                        Assessment.class
                )
                .setParameter("rid", resourceId)
                .uniqueResult();
    }

    @Override
    public void save(Assessment assessment) {
        getSession().save(assessment);
    }

    @Override
    public List<Assessment> findAll() {
        return getSession()
                .createQuery("FROM Assessment", Assessment.class)
                .list();
    }
}
