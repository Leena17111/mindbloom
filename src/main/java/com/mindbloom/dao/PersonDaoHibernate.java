package com.mindbloom.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mindbloom.model.Person;

@Repository
@Transactional
public class PersonDaoHibernate implements PersonDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Person findByEmail(String email) {
        List<Person> list = em.createQuery(
                "SELECT p FROM Person p WHERE p.email = :email", Person.class)
                .setParameter("email", email)
                .getResultList();

        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public Person findById(Integer id) {
        return em.find(Person.class, id);
    }

    @Override
    public void save(Person person) {
        if (person.getId() == null) {
            em.persist(person);
        } else {
            em.merge(person);
        }
    }

    @Override
    public List<Person> findByRole(String role) {
        return em.createQuery(
                "SELECT p FROM Person p WHERE p.role = :role", Person.class)
                .setParameter("role", role)
                .getResultList();
    }
}
