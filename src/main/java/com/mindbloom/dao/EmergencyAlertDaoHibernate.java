package com.mindbloom.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mindbloom.model.EmergencyAlert;

@Repository
@Transactional
public class EmergencyAlertDaoHibernate implements EmergencyAlertDao {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public void save(EmergencyAlert alert) {
        getSession().saveOrUpdate(alert);
    }

    @Override
    public List<EmergencyAlert> findAll() {
        return getSession()
                .createQuery("FROM EmergencyAlert ORDER BY triggeredAt DESC", EmergencyAlert.class)
                .list();
    }

    @Override
    public EmergencyAlert findById(int id) {
        return getSession().get(EmergencyAlert.class, id);
    }
}
