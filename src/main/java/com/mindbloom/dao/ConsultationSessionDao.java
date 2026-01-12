package com.mindbloom.dao;

import java.util.List;
import com.mindbloom.model.ConsultationSession;

public interface ConsultationSessionDao {

    void save(ConsultationSession session);

    ConsultationSession findById(int id);

    List<ConsultationSession> findByCounselor(int counselorId);

    /* =========================
       STUDENT – AVAILABLE SESSIONS
       ========================= */
    List<ConsultationSession> findAllAvailable();
    /* ✅ ADD THIS */
    void cancelByCounselor(int sessionId, int counselorId);
    void markAvailable(int sessionId);

}