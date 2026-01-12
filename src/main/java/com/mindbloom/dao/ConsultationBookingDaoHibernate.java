package com.mindbloom.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mindbloom.model.ConsultationBooking;

@Repository
public class ConsultationBookingDaoHibernate implements ConsultationBookingDao {

    @Autowired
    private SessionFactory sessionFactory;

    /* =========================
       SAVE / UPDATE BOOKING
       ========================= */
    @Override
    public void save(ConsultationBooking booking) {

        Session session = null;
        Transaction tx = null;

        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();

            session.saveOrUpdate(booking);

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

    /* =========================
       FIND BY SESSION ID
       ========================= */
    @Override
    public ConsultationBooking findBySessionId(int sessionId) {

        Session session = null;

        try {
            session = sessionFactory.openSession();

            return session.createQuery(
                "FROM ConsultationBooking WHERE session.id = :sid",
                ConsultationBooking.class
            )
            .setParameter("sid", sessionId)
            .uniqueResult();

        } finally {
            if (session != null) session.close();
        }
    }

    /* =========================
       FIND BY STUDENT + SESSION
       ========================= */
    @Override
    public ConsultationBooking findByStudentAndSession(int studentId, int sessionId) {

        Session session = null;

        try {
            session = sessionFactory.openSession();

            return session.createQuery(
                "FROM ConsultationBooking WHERE studentId = :sid AND session.id = :sessId",
                ConsultationBooking.class
            )
            .setParameter("sid", studentId)
            .setParameter("sessId", sessionId)
            .uniqueResult();

        } finally {
            if (session != null) session.close();
        }
    }

    /* =========================
       CHECK IF SESSION IS BOOKED
       ========================= */
    @Override
    public boolean existsBySessionId(int sessionId) {

        Session session = null;

        try {
            session = sessionFactory.openSession();

            Long count = session.createQuery(
                // ✅ STEP 2 FIX (CRITICAL)
                // ONLY count ACTIVE bookings
                "SELECT COUNT(b.id) " +
                "FROM ConsultationBooking b " +
                "WHERE b.session.id = :sid " +
                "AND b.status = 'BOOKED'",
                Long.class
            )
            .setParameter("sid", sessionId)
            .uniqueResult();

            return count != null && count > 0;

        } finally {
            if (session != null) session.close();
        }
    }

    /* =========================
       FIND BOOKINGS BY STUDENT
       ========================= */
    @Override
    public List<ConsultationBooking> findByStudent(int studentId) {

        Session session = null;

        try {
            session = sessionFactory.openSession();

            return session.createQuery(
                // show ONLY active bookings
                "FROM ConsultationBooking " +
                "WHERE studentId = :sid AND status = 'BOOKED' " +
                "ORDER BY bookedAt DESC",
                ConsultationBooking.class
            )
            .setParameter("sid", studentId)
            .getResultList();

        } finally {
            if (session != null) session.close();
        }
    }

    /* =========================
       COUNSELOR: VIEW BOOKED SESSIONS + STUDENT NAME
       ========================= */
    @Override
    public List<Object[]> findBookedSessionsWithStudentByCounselor(int counselorId) {

        Session session = null;

        try {
            session = sessionFactory.openSession();

            return session.createQuery(
                "SELECT b, p.name " +
                "FROM ConsultationBooking b " +
                "JOIN Person p ON b.studentId = p.id " +
                "WHERE b.session.counselorId = :cid " +
                "AND b.status = 'BOOKED' " +
                "ORDER BY b.bookedAt DESC",
                Object[].class
            )
            .setParameter("cid", counselorId)
            .getResultList();

        } finally {
            if (session != null) session.close();
        }
    }

    /* =========================
       STUDENT CANCEL BOOKING
       ========================= */
    @Override
    public void cancelByStudent(int bookingId, int studentId) {

        Session session = null;
        Transaction tx = null;

        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();

            ConsultationBooking booking =
                session.get(ConsultationBooking.class, bookingId);

            if (booking != null && booking.getStudentId() == studentId) {
                booking.setStatus("CANCELLED_BY_STUDENT");
                session.update(booking);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

    /* =========================
       COUNSELOR CANCEL BOOKING
       ========================= */
    @Override
    public void cancelByCounselor(int bookingId) {

        Session session = null;
        Transaction tx = null;

        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();

            ConsultationBooking booking =
                    session.get(ConsultationBooking.class, bookingId);

            if (booking != null) {
                booking.setStatus("CANCELLED_BY_COUNSELOR");
                booking.getSession().setStatus("CANCELLED");

                session.update(booking);
                session.update(booking.getSession());
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }
}