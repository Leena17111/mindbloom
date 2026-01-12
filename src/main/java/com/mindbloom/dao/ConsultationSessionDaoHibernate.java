package com.mindbloom.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mindbloom.model.ConsultationSession;

@Repository
public class ConsultationSessionDaoHibernate implements ConsultationSessionDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void save(ConsultationSession sessionObj) {
        Session session = null;
        Transaction tx = null;

        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();

            session.saveOrUpdate(sessionObj);

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    public ConsultationSession findById(int id) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            return session.get(ConsultationSession.class, id);
        } finally {
            if (session != null) session.close();
        }
    }

    /* =====================================================
       STEP 3.1 FIX (CRITICAL)
       Counselor sees ONLY sessions THEY created
       This prevents:
       - session mixing
       - refresh/login inconsistency
       - multi-tab bugs
       ===================================================== */
    @Override
    public List<ConsultationSession> findByCounselor(int counselorId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();

            return session.createQuery(
                "FROM ConsultationSession " +
                "WHERE counselorId = :cid " +          // 🔒 counselor isolation
                "ORDER BY sessionDate DESC, startTime DESC",
                ConsultationSession.class
            )
            .setParameter("cid", counselorId)
            .getResultList();

        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    public void cancelByCounselor(int sessionId, int counselorId) {
        Session session = null;
        Transaction tx = null;

        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();

            ConsultationSession cs =
                session.get(ConsultationSession.class, sessionId);

            if (cs == null) {
                tx.commit();
                return;
            }

            // 🔒 security: counselor can cancel ONLY their own session
            if (cs.getCounselorId() != counselorId) {
                tx.commit();
                return;
            }

            // already cancelled → do nothing
            if ("CANCELLED".equalsIgnoreCase(cs.getStatus())) {
                tx.commit();
                return;
            }

            cs.setStatus("CANCELLED");
            cs.setCancelledAt(LocalDateTime.now());

            session.update(cs);
            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

    /* =====================================================
       STEP 3.2 CONFIRMATION
       Students see ONLY AVAILABLE sessions
       Status is read from DB (NOT derived from bookings)
       ===================================================== */
    @Override
    public List<ConsultationSession> findAllAvailable() {

        Session session = null;

        try {
            session = sessionFactory.openSession();

            return session.createQuery(
                "FROM ConsultationSession " +
                "WHERE status = 'AVAILABLE' " +
                "ORDER BY sessionDate, startTime",
                ConsultationSession.class
            ).getResultList();

        } finally {
            if (session != null) session.close();
        }
    }

    /* =====================================================
       Used when student cancels booking
       Makes session bookable again
       ===================================================== */
    @Override
    public void markAvailable(int sessionId) {
        Session session = null;
        Transaction tx = null;

        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();

            ConsultationSession cs =
                session.get(ConsultationSession.class, sessionId);

            if (cs != null) {
                cs.setStatus("AVAILABLE");
                session.update(cs);
            }

            tx.commit();
        } finally {
            if (session != null) session.close();
        }
    }
}