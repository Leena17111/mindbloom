package com.mindbloom.dao;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mindbloom.model.Dass21Question;

@Repository
@Transactional
public class Dass21QuestionDaoHibernate implements Dass21QuestionDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public List<Dass21Question> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("from Dass21Question", Dass21Question.class)
                .getResultList();
    }

    @Override
    public Dass21Question findById(int id) {
        return sessionFactory.getCurrentSession()
                .get(Dass21Question.class, id);
    }
}
