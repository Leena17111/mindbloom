package com.mindbloom.controller;

import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mindbloom.dao.AssessmentResultDao;
import com.mindbloom.dao.EmergencyAlertDao;
import com.mindbloom.model.AssessmentResult;
import com.mindbloom.model.EmergencyAlert;
import com.mindbloom.model.Person;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private AssessmentResultDao assessmentResultDao;

    @Autowired
    private EmergencyAlertDao emergencyAlertDao;

    // =========================
    // Student Dashboard
    // =========================
    @GetMapping("/dashboard")
    public String studentDashboard() {
        return "student/dashboard";
    }

    // =========================
    // Assessment Result Page
    // =========================
    @GetMapping("/assessment/{id}/result")
    public String viewResult(
            @PathVariable Long id,
            Model model,
            HttpSession session
    ) {

        int studentId = getLoggedInStudentId(session);

        List<AssessmentResult> results =
                assessmentResultDao.findByStudentId(studentId);

        if (results.isEmpty()) {
            return "redirect:/student/dashboard";
        }

        // latest result (most recent first assumed)
        AssessmentResult latest = results.get(0);

        int totalPoints = results.size() * 10;

        String level = calculateLevel(totalPoints);
        String badge = calculateBadge(totalPoints);

        model.addAttribute("score", latest.getScore());
        model.addAttribute("points", latest.getPointsEarned());
        model.addAttribute("totalPoints", totalPoints);
        model.addAttribute("level", level);
        model.addAttribute("badge", badge);

        return "student/assessment-result";
    }

    // =========================
    // Emergency / Panic Button
    // =========================
    @GetMapping("/emergency")
    public String emergencyPage() {
        return "student/emergency";
    }

    @PostMapping("/emergency/trigger")
    public String triggerEmergency(HttpSession session) {

        int studentId = getLoggedInStudentId(session);

        EmergencyAlert alert = new EmergencyAlert();
        alert.setStudentId(studentId);
        alert.setTriggeredAt(LocalDateTime.now());
        alert.setStatus("NEW");

        emergencyAlertDao.save(alert);

        return "redirect:/student/emergency";
    }

    // =========================
    // Helper Methods
    // =========================
    private int getLoggedInStudentId(HttpSession session) {
        Person loggedUser = (Person) session.getAttribute("loggedUser");
        return loggedUser.getId();
    }

    private String calculateLevel(int totalPoints) {
        if (totalPoints >= 50) return "Expert";
        if (totalPoints >= 30) return "Advanced";
        if (totalPoints >= 10) return "Intermediate";
        return "Beginner";
    }

    private String calculateBadge(int totalPoints) {
        if (totalPoints >= 50) return "Platinum";
        if (totalPoints >= 30) return "Gold";
        if (totalPoints >= 10) return "Silver";
        return "Bronze";
    }
}
