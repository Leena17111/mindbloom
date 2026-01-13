package com.mindbloom.dao;
import java.util.List;

import com.mindbloom.model.ConsultationBooking;

public interface ConsultationBookingDao {

    void save(ConsultationBooking booking);

    ConsultationBooking findBySessionId(int sessionId);

    ConsultationBooking findByStudentAndSession(int studentId, int sessionId);
    boolean existsBySessionId(int sessionId);
    List<ConsultationBooking> findByStudent(int studentId);
     List<Object[]> findBookedSessionsWithStudentByCounselor(int counselorId);
     void cancelByStudent(int bookingId, int studentId);
     /**
 * Counselor cancels a booking
 * Used when counselor cancels a session that was already booked
    */
    void cancelByCounselor(int bookingId);
    int countUpcomingBookings(int studentId);

     
}