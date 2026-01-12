package com.mindbloom.model;

import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(
    name = "consultation_booking",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"session_id"})
    }
)
public class ConsultationBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // ===== SESSION (FK) =====
    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private ConsultationSession session;

    // ===== STUDENT (FK → person.id) =====
    @Column(name = "student_id", nullable = false)
    private int studentId;

    // ===== STATUS =====
    // BOOKED / CANCELLED
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "booked_at", nullable = false)
    private LocalDateTime bookedAt;

    // ===== getters / setters =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ConsultationSession getSession() {
        return session;
    }

    public void setSession(ConsultationSession session) {
        this.session = session;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }
}