package com.mindbloom.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mindbloom.model.Post;

@Repository
@Transactional
public class PostDaoHibernate implements PostDao {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    /* ===========================
       SAVE (INSERT OR UPDATE)
       =========================== */
    @Override
    public void save(Post post) {
        getSession().saveOrUpdate(post);
    }

    /* ===========================
       DELETE
       =========================== */
    @Override
    public void delete(int id) {
        Post post = findById(id);
        if (post != null) {
            getSession().delete(post);
        }
    }

    /* ===========================
       FIND BY ID
       =========================== */
    @Override
    public Post findById(int id) {
        return getSession().get(Post.class, id);
    }

    /* ===========================
       FIND ALL
       =========================== */
    @Override
    public List<Post> findAll() {
        return getSession()
                .createQuery(
                    "FROM Post ORDER BY createdAt DESC",
                    Post.class
                )
                .list();
    }

    /* ===========================
       FIND BY STUDENT ID
       =========================== */
    @Override
    public List<Post> findByStudentId(int studentId) {
        return getSession()
                .createQuery(
                    "FROM Post WHERE studentId = :studentId ORDER BY createdAt DESC",
                    Post.class
                )
                .setParameter("studentId", studentId)
                .list();
    }

    /* ===========================
       FIND BY CATEGORY
       =========================== */
    @Override
    public List<Post> findByCategory(String category) {
        return getSession()
                .createQuery(
                    "FROM Post WHERE category = :category ORDER BY createdAt DESC",
                    Post.class
                )
                .setParameter("category", category)
                .list();
    }

    /* ===========================
       SEARCH BY TITLE
       =========================== */
    @Override
    public List<Post> searchByTitle(String keyword) {
        return getSession()
                .createQuery(
                    "FROM Post WHERE title LIKE :kw ORDER BY createdAt DESC",
                    Post.class
                )
                .setParameter("kw", "%" + keyword + "%")
                .list();
    }

    /* ===========================
       FIND BY STATUS
       =========================== */
    @Override
    public List<Post> findByStatus(String status) {
        return getSession()
                .createQuery(
                    "FROM Post WHERE status = :status ORDER BY createdAt DESC",
                    Post.class
                )
                .setParameter("status", status)
                .list();
    }
}