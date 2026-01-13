package com.mindbloom.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mindbloom.dao.AssessmentDao;
import com.mindbloom.dao.AssessmentQuestionDao;
import com.mindbloom.dao.AssessmentResultDao;
import com.mindbloom.dao.ConsultationBookingDao;
import com.mindbloom.dao.ConsultationSessionDao;
import com.mindbloom.dao.EmergencyAlertDao;
import com.mindbloom.dao.MentalHealthResourceDao;
import com.mindbloom.dao.PersonDao;
import com.mindbloom.dao.StudentResourceProgressDao;
import com.mindbloom.model.Assessment;
import com.mindbloom.model.AssessmentQuestion;
import com.mindbloom.model.AssessmentResult;
import com.mindbloom.model.ConsultationBooking;
import com.mindbloom.model.ConsultationSession;
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

    @Autowired
    private ConsultationBookingDao consultationBookingDao;
    
    @Autowired
    private ConsultationSessionDao consultationSessionDao;

    @GetMapping("/dashboard")
public String studentDashboard(Model model) {

    Integer studentId = getLoggedInStudentId();
    if (studentId == null) return "redirect:/login";

    Person student = (Person) SecurityContextHolder
            .getContext().getAuthentication().getPrincipal();

    int completedResources = progressDao.countCompletedResources(studentId);
    int completedAssessments =
            assessmentResultDao.findByStudentId(studentId).size();
    int upcomingCount =
            consultationBookingDao.countUpcomingBookings(studentId);

    String dailyTip =
            "Take 5 minutes today to practice deep breathing. It can help reduce stress and improve focus.";

    model.addAttribute("student", student);
    model.addAttribute("completedResources", completedResources);
    model.addAttribute("completedAssessments", completedAssessments);
    model.addAttribute("upcomingSession", upcomingCount);
    model.addAttribute("dailyTip", dailyTip);

    return "student/dashboard";
}


    @GetMapping("/progress")
public String viewProgress(Model model) {

    Integer studentId = getLoggedInStudentId();
    if (studentId == null) return "redirect:/login";

    int totalResources = resourceDao.findAll().size();
    int totalAssessments = assessmentDao.findAll().size();

    int completedResources = progressDao.countCompletedResources(studentId);
    int inProgressResources = progressDao.countInProgressResources(studentId);

    int resourcesPercent = totalResources == 0 ? 0 :
            (int) Math.round((completedResources * 100.0) / totalResources);

    int inProgressPercent = totalResources == 0 ? 0 :
            (int) Math.round((inProgressResources * 100.0) / totalResources);

    int completedAssessments =
            assessmentResultDao.findByStudentId(studentId).size();

    int assessmentsPercent = totalAssessments == 0 ? 0 :
            (int) Math.round((completedAssessments * 100.0) / totalAssessments);

    int totalPoints = assessmentResultDao.findByStudentId(studentId)
            .stream()
            .mapToInt(AssessmentResult::getPointsEarned)
            .sum();

    String level = calculateLevel(totalPoints);

    model.addAttribute("level", level);
    model.addAttribute("totalPoints", totalPoints);
    model.addAttribute("totalResources", totalResources);
    model.addAttribute("completedResources", completedResources);
    model.addAttribute("inProgressResources", inProgressResources);
    model.addAttribute("totalAssessments", totalAssessments);
    model.addAttribute("completedAssessments", completedAssessments);
    model.addAttribute("resourcesPercent", resourcesPercent);
    model.addAttribute("inProgressPercent", inProgressPercent);
    model.addAttribute("assessmentsPercent", assessmentsPercent);

    return "student/progress";
}
@GetMapping("/resources")
public String listResources(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String category,
        Model model) {

    Integer studentId = getLoggedInStudentId();
    if (studentId == null) return "redirect:/login";

    List<MentalHealthResource> allResources = resourceDao.findAll();
    List<MentalHealthResource> filteredResources = new ArrayList<>();

    for (MentalHealthResource r : allResources) {

        // CATEGORY FILTER
        if (category != null && !category.isEmpty()
                && !category.equals(r.getCategory())) {
            continue;
        }

        // SEARCH FILTER
        if (search != null && !search.trim().isEmpty()
                && !r.getTitle().toLowerCase().contains(search.toLowerCase())) {
            continue;
        }

        filteredResources.add(r);
    }

    Map<Integer, String> statusMap = new HashMap<>();

    for (MentalHealthResource r : filteredResources) {
        StudentResourceProgress progress =
                progressDao.findByStudentAndResource(studentId, r.getId());

        if (progress == null) statusMap.put(r.getId(), "NOT_STARTED");
        else if (progress.getCompletedAt() == null) statusMap.put(r.getId(), "IN_PROGRESS");
        else statusMap.put(r.getId(), "COMPLETED");
    }

    model.addAttribute("resources", filteredResources);
    model.addAttribute("statusMap", statusMap);
    model.addAttribute("search", search);
    model.addAttribute("category", category);

    return "student/resources";
}



    /* =========================
   RESOURCE DETAIL
   ========================= */
@GetMapping("/resources/{id}")
public String viewResource(
        @PathVariable int id,
        Model model) {

    Integer studentId = getLoggedInStudentId();
    if (studentId == null) {
        return "redirect:/login";
    }

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
public String completeResource(@PathVariable int id) {

    Integer studentId = getLoggedInStudentId();
    if (studentId == null) {
        return "redirect:/login";
    }

    StudentResourceProgress progress =
            progressDao.findByStudentAndResource(studentId, id);

    if (progress != null && progress.getCompletedAt() == null) {
        progress.setCompletedAt(LocalDateTime.now());
        progressDao.save(progress);
    }

    return "redirect:/student/resources/" + id;
}

@GetMapping("/assessments")
public String listAssessments(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String category,
        Model model) {

    Integer studentId = getLoggedInStudentId();
    if (studentId == null) return "redirect:/login";

    List<Assessment> allAssessments = assessmentDao.findAll();
    List<Assessment> filteredAssessments = new ArrayList<>();

    Map<Integer, String> statusMap = new HashMap<>();
    Map<Integer, MentalHealthResource> resourceMap = new HashMap<>();

    for (Assessment a : allAssessments) {

        MentalHealthResource resource =
                resourceDao.findById(a.getResourceId());

        // ✅ FIX 1: orphan safety
        if (resource == null) {
            continue;
        }

        // ✅ CATEGORY FILTER (RESOURCE-BASED)
        if (category != null && !category.isEmpty()
                && !category.equals(resource.getCategory())) {
            continue;
        }

        // ✅ SEARCH FILTER (RESOURCE TITLE)
        if (search != null && !search.trim().isEmpty()
                && !resource.getTitle().toLowerCase().contains(search.toLowerCase())) {
            continue;
        }

        filteredAssessments.add(a);
        resourceMap.put(a.getId(), resource);

        StudentResourceProgress progress =
                progressDao.findByStudentAndResource(studentId, resource.getId());

        if (progress == null || progress.getCompletedAt() == null) {
            statusMap.put(a.getId(), "LOCKED");
            continue;
        }

        AssessmentResult result =
                assessmentResultDao.findByStudentAndAssessment(studentId, a.getId());

        statusMap.put(a.getId(), result == null ? "NOT_TAKEN" : "COMPLETED");
    }

    model.addAttribute("assessments", filteredAssessments);
    model.addAttribute("statusMap", statusMap);
    model.addAttribute("resourceMap", resourceMap);
    model.addAttribute("search", search);
    model.addAttribute("category", category);

    return "student/assessment-list";
}


/* =========================
   SUBMIT ASSESSMENT
   ========================= */
@PostMapping("/assessment/{id}/submit")
public String submitAssessment(
        @PathVariable int id,
        @RequestParam Map<String, String> params) {

    Integer studentId = getLoggedInStudentId();
    if (studentId == null) {
        return "redirect:/login";
    }

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
        Model model) {

    Integer studentId = getLoggedInStudentId();
    if (studentId == null) {
        return "redirect:/login";
    }

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
public String triggerEmergency() {

    Integer studentId = getLoggedInStudentId();
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
    private Integer getLoggedInStudentId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !auth.isAuthenticated()
            || auth.getPrincipal().equals("anonymousUser")) {
        return null;
    }

    Person student = (Person) auth.getPrincipal();
    return student.getId();
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
   VIEW AVAILABLE SESSIONS
   ========================= */
@GetMapping("/sessions")
public String viewAvailableSessions(Model model) {

    Integer studentId = getLoggedInStudentId();
    if (studentId == null) {
        return "redirect:/login";
    }

    List<ConsultationSession> sessions =
            consultationSessionDao.findAllAvailable();

    model.addAttribute("sessions", sessions);
    return "student/available-sessions";
}

/* =========================
   BOOK CONSULTATION SESSION
   ========================= */
@PostMapping("/sessions/{id}/book")
public String bookSession(
        @PathVariable("id") int sessionId,
        RedirectAttributes redirectAttributes) {

    Integer studentId = getLoggedInStudentId();
    if (studentId == null) {
        return "redirect:/login";
    }

    ConsultationSession session =
            consultationSessionDao.findById(sessionId);

    if (session == null || !"AVAILABLE".equals(session.getStatus())) {
        redirectAttributes.addFlashAttribute(
                "error", "Session is no longer available.");
        return "redirect:/student/sessions";
    }

    ConsultationBooking existing =
            consultationBookingDao.findBySessionId(sessionId);

    if (existing != null && "BOOKED".equals(existing.getStatus())) {
        redirectAttributes.addFlashAttribute(
                "error", "This session is already booked.");
        return "redirect:/student/sessions";
    }

    ConsultationBooking booking =
            consultationBookingDao.findBySessionId(sessionId);

    if (booking == null) {
        booking = new ConsultationBooking();
        booking.setSession(session);
    }

    booking.setStudentId(studentId);
    booking.setBookedAt(LocalDateTime.now());
    booking.setStatus("BOOKED");

    consultationBookingDao.save(booking);

    session.setStatus("BOOKED");
    consultationSessionDao.save(session);

    redirectAttributes.addFlashAttribute(
            "success", "Session booked successfully.");

    return "redirect:/student/sessions";
}

@GetMapping("/sessions/my-bookings")
public String myBookings(Model model) {

    Integer studentId = getLoggedInStudentId();
    if (studentId == null) {
        return "redirect:/login";
    }

    List<ConsultationBooking> bookings =
            consultationBookingDao.findByStudent(studentId);

    model.addAttribute("bookings", bookings);

    return "student/my-bookings";
}

@PostMapping("/sessions/cancel/{bookingId}/{sessionId}")
public String cancelBooking(
        @PathVariable int bookingId,
        @PathVariable int sessionId,
        RedirectAttributes ra) {

    Integer studentId = getLoggedInStudentId();
    if (studentId == null) return "redirect:/login";

    consultationBookingDao.cancelByStudent(bookingId, studentId);
    consultationSessionDao.markAvailable(sessionId);

    ra.addFlashAttribute("success", "Session cancelled successfully.");
    return "redirect:/student/sessions/my-bookings";
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