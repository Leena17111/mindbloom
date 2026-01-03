package com.mindbloom.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mindbloom.model.StudentResourceProgress;

@Repository
@Transactional
public class StudentResourceProgressDaoHibernate
        implements StudentResourceProgressDao {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public void save(StudentResourceProgress progress) {
        getSession().saveOrUpdate(progress);
    }

    @Override
    public StudentResourceProgress findByStudentAndResource(
            int studentId, int resourceId) {

        return getSession()
                .createQuery(
                        "FROM StudentResourceProgress " +
                        "WHERE studentId = :sid AND resourceId = :rid",
                        StudentResourceProgress.class)
                .setParameter("sid", studentId)
                .setParameter("rid", resourceId)
                .uniqueResult();
    }

    @Override
    public List<StudentResourceProgress> findByStudentId(int studentId) {
        return getSession()
                .createQuery(
                        "FROM StudentResourceProgress WHERE studentId = :sid",
                        StudentResourceProgress.class)
                .setParameter("sid", studentId)
                .list();
    }
}
