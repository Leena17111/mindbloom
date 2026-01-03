package com.mindbloom.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mindbloom.dao.AssessmentDao;
import com.mindbloom.dao.AssessmentQuestionDao;
import com.mindbloom.dao.AssessmentResultDao;
import com.mindbloom.dao.EmergencyAlertDao;
import com.mindbloom.dao.MentalHealthResourceDao;
import com.mindbloom.dao.PersonDao;
import com.mindbloom.dao.StudentResourceProgressDao;
import com.mindbloom.model.Assessment;
import com.mindbloom.model.AssessmentQuestion;
import com.mindbloom.model.AssessmentResult;
import com.mindbloom.model.EmergencyAlert;
import com.mindbloom.model.MentalHealthResource;
import com.mindbloom.model.Person;
import com.mindbloom.model.StudentResourceProgress;
import com.mindbloom.service.EmailService;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private MentalHealthResourceDao resourceDao;

    @Autowired
    private AssessmentDao assessmentDao;

    @Autowired
    private AssessmentQuestionDao questionDao;

    @Autowired
    private AssessmentResultDao assessmentResultDao;

    @Autowired
    private StudentResourceProgressDao progressDao;

    @Autowired
    private EmergencyAlertDao emergencyAlertDao;

    @Autowired
    private PersonDao personDao;

    @Autowired
    private EmailService emailService;

    /* =========================
       DASHBOARD
       ========================= */
    @GetMapping("/dashboard")
    public String studentDashboard(Model model, HttpSession session) {

        int studentId = getLoggedInStudentId(session);

        int completedResources =
                progressDao.findByStudentId(studentId).size();

        int completedAssessments =
                assessmentResultDao.findByStudentId(studentId).size();

        model.addAttribute("completedResources", completedResources);
        model.addAttribute("completedAssessments", completedAssessments);

        return "student/dashboard";
    }

    /* =========================
       LIST RESOURCES
       ========================= */
    @GetMapping("/resources")
    public String listResources(Model model, HttpSession session) {

        int studentId = getLoggedInStudentId(session);

        List<MentalHealthResource> resources = resourceDao.findAll();

        Map<Integer, String> statusMap = new HashMap<>();
        Map<Integer, Boolean> assessmentMap = new HashMap<>();

        for (MentalHealthResource r : resources) {

            StudentResourceProgress progress =
                    progressDao.findByStudentAndResource(studentId, r.getId());

            if (progress == null) {
                statusMap.put(r.getId(), "NOT_STARTED");
            } else if (progress.getCompletedAt() == null) {
                statusMap.put(r.getId(), "IN_PROGRESS");
            } else {
                statusMap.put(r.getId(), "COMPLETED");
            }

            assessmentMap.put(
                r.getId(),
                assessmentDao.findByResourceId(r.getId()) != null
            );
        }

        model.addAttribute("resources", resources);
        model.addAttribute("statusMap", statusMap);
        model.addAttribute("assessmentMap", assessmentMap);

        return "student/resources";
    }

    /* =========================
       RESOURCE DETAIL
       ========================= */
    @GetMapping("/resources/{id}")
    public String viewResource(
            @PathVariable int id,
            Model model,
            HttpSession session) {

        int studentId = getLoggedInStudentId(session);

        MentalHealthResource resource = resourceDao.findById(id);

        StudentResourceProgress progress =
                progressDao.findByStudentAndResource(studentId, id);

        if (progress == null) {
            progress = new StudentResourceProgress();
            progress.setStudentId(studentId);
            progress.setResourceId(id);
            progress.setStartedAt(LocalDateTime.now());
            progressDao.save(progress);
        }

        boolean completed = progress.getCompletedAt() != null;

        Assessment assessment = assessmentDao.findByResourceId(id);

        //  FIX 1: assessmentDone must require completedAt
        AssessmentResult result =
                assessment != null
                ? assessmentResultDao.findByStudentAndAssessment(studentId, assessment.getId())
                : null;

        boolean assessmentDone =
                result != null && result.getCompletedAt() != null;

        model.addAttribute("resource", resource);
        model.addAttribute("completed", completed);
        model.addAttribute("hasAssessment", assessment != null);
        model.addAttribute("assessmentDone", assessmentDone);

        return "student/resource-detail";
    }

    /* =========================
       MARK RESOURCE COMPLETED
       ========================= */
    @PostMapping("/resources/{id}/complete")
    public String completeResource(
            @PathVariable int id,
            HttpSession session) {

        int studentId = getLoggedInStudentId(session);

        StudentResourceProgress progress =
                progressDao.findByStudentAndResource(studentId, id);

        if (progress != null && progress.getCompletedAt() == null) {
            progress.setCompletedAt(LocalDateTime.now());
            progressDao.save(progress);
        }

        return "redirect:/student/resources/" + id;
    }

    @GetMapping("/resources/{id}/assessment")
    public String takeAssessment(
            @PathVariable int id,
            Model model,
            HttpSession session) {

        int studentId = getLoggedInStudentId(session);

        StudentResourceProgress progress =
                progressDao.findByStudentAndResource(studentId, id);

        // Must be COMPLETED before assessment
        if (progress == null || progress.getCompletedAt() == null) {
            return "redirect:/student/resources/" + id;
        }

        Assessment assessment = assessmentDao.findByResourceId(id);
        if (assessment == null) {
            return "redirect:/student/resources/" + id;
        }

        List<AssessmentQuestion> questions =
                questionDao.findByAssessmentId(assessment.getId());

        MentalHealthResource resource = resourceDao.findById(id);

        model.addAttribute("resource", resource); 
        model.addAttribute("assessment", assessment);
        model.addAttribute("questions", questions);

        return "student/assessment";
    }

    /* =========================
       SUBMIT ASSESSMENT
       ========================= */
    @PostMapping("/assessment/{id}/submit")
    public String submitAssessment(
            @PathVariable int id,
            @RequestParam Map<String, String> params,
            HttpSession session) {

        int studentId = getLoggedInStudentId(session);

    
        AssessmentResult existing =
                assessmentResultDao.findByStudentAndAssessment(studentId, id);

        if (existing == null) {

            List<AssessmentQuestion> questions =
                    questionDao.findByAssessmentId(id);

            int score = 0;

            for (AssessmentQuestion q : questions) {
                String selected = params.get("q" + q.getId());
                if (q.getCorrectOption().equals(selected)) {
                    score++;
                }
            }

            AssessmentResult result = new AssessmentResult();
            result.setStudentId(studentId);
            result.setAssessmentId(id);
            result.setScore(score);
            result.setPointsEarned(10);
            result.setCompletedAt(LocalDateTime.now());

            assessmentResultDao.save(result);
        }

        return "redirect:/student/assessment/" + id + "/result";
    }

    /* =========================
       ASSESSMENT RESULT
       ========================= */
    @GetMapping("/assessment/{id}/result")
    public String viewAssessmentResult(
            @PathVariable int id,
            HttpSession session,
            Model model) {

        int studentId = getLoggedInStudentId(session);

        AssessmentResult result =
                assessmentResultDao.findByStudentAndAssessment(studentId, id);

        if (result == null) {
            return "redirect:/student/dashboard";
        }

        int totalPoints =
                assessmentResultDao.findByStudentId(studentId).size() * 10;

        model.addAttribute("score", result.getScore());
        model.addAttribute("points", result.getPointsEarned());
        model.addAttribute("totalPoints", totalPoints);
        model.addAttribute("level", calculateLevel(totalPoints));
        model.addAttribute("badge", calculateBadge(totalPoints));

        return "student/assessment-result";
    }

    /* =========================
       EMERGENCY
       ========================= */
    @GetMapping("/emergency")
    public String emergency() {
        return "student/emergency";
    }

    @PostMapping("/emergency/trigger")
    public String triggerEmergency(HttpSession session) {

        Integer studentId = getLoggedInStudentId(session);

        if (studentId == null) {
            return "redirect:/login";
        }

        EmergencyAlert alert = new EmergencyAlert();
        alert.setStudentId(studentId);
        alert.setTriggeredAt(LocalDateTime.now());
        alert.setStatus("NEW");

        emergencyAlertDao.save(alert);

        return "redirect:/student/emergency";
    }

    /* =========================
       FORGOT PASSWORD FLOW
       ========================= */

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "student/forgot-password";
    }

    /* =========================
       HELPERS
       ========================= */
    private Integer getLoggedInStudentId(HttpSession session) {

        Person user = (Person) session.getAttribute("loggedUser");

        if (user == null) {
            return null;
        }

        return user.getId();
    }

    private String calculateLevel(int points) {
        if (points >= 50) return "Expert";
        if (points >= 30) return "Advanced";
        if (points >= 10) return "Intermediate";
        return "Beginner";
    }

    private String calculateBadge(int points) {
        if (points >= 50) return "Gold";
        if (points >= 30) return "Silver";
        if (points >= 10) return "Bronze";
        return "Starter";
    }

    /* =========================
       PASSWORD RESET FLOW
       ========================= */

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam("email") String email,
            Model model) {

        Person person = personDao.findByEmail(email);

        if (person == null) {
            model.addAttribute("error", "Email not found");
            return "student/forgot-password";
        }

        String code = String.valueOf((int)(Math.random() * 900000) + 100000);

        person.setResetCode(code);
        person.setResetCodeExpiry(LocalDateTime.now().plusMinutes(10));
        personDao.save(person);

        boolean sent = emailService.sendVerificationCode(email, code);

        if (!sent) {
            model.addAttribute("error", "Failed to send email");
            return "student/forgot-password";
        }

        model.addAttribute("email", email);
        return "student/verify-code";
    }

    @PostMapping("/verify-code")
    public String verifyCode(
            @RequestParam("email") String email,
            @RequestParam("code") String code,
            Model model) {

        Person person = personDao.findByEmail(email);

        if (person == null ||
            person.getResetCode() == null ||
            !code.equals(person.getResetCode()) ||
            person.getResetCodeExpiry().isBefore(LocalDateTime.now())) {

            model.addAttribute("error", "Invalid or expired code");
            model.addAttribute("email", email);
            return "student/verify-code";
        }

        model.addAttribute("email", email);
        return "student/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("email", email);
            return "student/reset-password";
        }

        Person person = personDao.findByEmail(email);

        if (person == null) {
            model.addAttribute("error", "Something went wrong");
            return "student/reset-password";
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(password);

        person.setPassword(hashedPassword);
        person.setResetCode(null);
        person.setResetCodeExpiry(null);
        personDao.save(person);

        return "redirect:/login";
    }

}
