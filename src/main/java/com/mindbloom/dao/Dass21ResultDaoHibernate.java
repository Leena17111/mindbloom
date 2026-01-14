package com.mindbloom.dao;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mindbloom.model.Dass21Result;

@Repository
@Transactional
public class Dass21ResultDaoHibernate implements Dass21ResultDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void save(Dass21Result result) {
        sessionFactory.getCurrentSession().save(result);
    }
}
