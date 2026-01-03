package com.mindbloom.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mindbloom.model.MentalHealthResource;

@Repository
@Transactional
public class MentalHealthResourceDaoHibernate implements MentalHealthResourceDao {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    /* ===========================
       SAVE (INSERT OR UPDATE)
       =========================== */
    @Override
    public void save(MentalHealthResource resource) {
        getSession().saveOrUpdate(resource);
    }

    /* ===========================
       DELETE
       =========================== */
    @Override
    public void delete(int id) {
        MentalHealthResource resource = findById(id);
        if (resource != null) {
            getSession().delete(resource);
        }
    }

    /* ===========================
       FIND BY ID
       =========================== */
    @Override
    public MentalHealthResource findById(int id) {
        return getSession().get(MentalHealthResource.class, id);
    }

    /* ===========================
       FIND ALL
       =========================== */
    @Override
    public List<MentalHealthResource> findAll() {
        return getSession()
                .createQuery(
                    "FROM MentalHealthResource ORDER BY createdAt DESC",
                    MentalHealthResource.class
                )
                .list();
    }

    /* ===========================
       SEARCH BY TITLE
       =========================== */
    @Override
    public List<MentalHealthResource> searchByTitle(String keyword) {
        return getSession()
                .createQuery(
                    "FROM MentalHealthResource WHERE title LIKE :kw",
                    MentalHealthResource.class
                )
                .setParameter("kw", "%" + keyword + "%")
                .list();
    }

    @Override
    public List<MentalHealthResource> findByCategory(String category) {
    return sessionFactory.getCurrentSession()
            .createQuery(
                "FROM MentalHealthResource WHERE category = :category",
                MentalHealthResource.class
            )
            .setParameter("category", category)
            .list();
}

}
