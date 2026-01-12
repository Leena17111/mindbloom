package com.mindbloom.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mindbloom.dao.AssessmentDao;
import com.mindbloom.dao.AssessmentQuestionDao;
import com.mindbloom.dao.ConsultationBookingDao;
import com.mindbloom.dao.ConsultationSessionDao;
import com.mindbloom.dao.MentalHealthResourceDao;
import com.mindbloom.model.Assessment;
import com.mindbloom.model.AssessmentQuestion;
import com.mindbloom.model.ConsultationSession;
import com.mindbloom.model.MentalHealthResource;
import com.mindbloom.model.Person;

@Controller
@RequestMapping("/counselor")
public class CounselorController {

    @Autowired
    private MentalHealthResourceDao resourceDao;

    @Autowired
    private AssessmentDao assessmentDao;

    @Autowired
    private AssessmentQuestionDao questionDao;

    @Autowired
    private ConsultationSessionDao consultationSessionDao;

    @Autowired
    private ConsultationBookingDao consultationBookingDao;

    /* =========================
       DASHBOARD
       ========================= */
    @GetMapping("/dashboard")
    public String counselorDashboard() {
        return "counselor/dashboard";
    }

    /* =========================
       LIST RESOURCES
       ========================= */
    @GetMapping("/resources")
    public String listResources(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            Model model) {

        List<MentalHealthResource> resources;
        if (category != null && !category.isEmpty()) {
        resources = resourceDao.findByCategory(category);
    } else if (search != null && !search.trim().isEmpty()) {
        resources = resourceDao.searchByTitle(search);
    } else {
        resources = resourceDao.findAll();
    }

    // Map: resourceId → assessment exists
    Map<Integer, Boolean> assessmentMap = new HashMap<>();

    for (MentalHealthResource res : resources) {
        Assessment assessment =
                assessmentDao.findByResourceId(res.getId());
        assessmentMap.put(res.getId(), assessment != null);
    }

    model.addAttribute("resources", resources);
    model.addAttribute("assessmentMap", assessmentMap);
    model.addAttribute("selectedCategory", category);

    return "counselor/resources"; }
    /* =========================
       ADD RESOURCE FORM
       ========================= */
    @GetMapping("/resources/add")
    public String showAddForm(Model model) {
        model.addAttribute("resource", new MentalHealthResource());
        return "counselor/resource-form";
    }

    /* =========================
       EDIT RESOURCE FORM
       ========================= */
    @GetMapping("/resources/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        model.addAttribute("resource", resourceDao.findById(id));
        return "counselor/resource-form";
    }

    /* =========================
       SAVE RESOURCE (ADD + EDIT)
       ========================= */
    @PostMapping("/resources/save")
    public String saveResource(
            @ModelAttribute("resource") MentalHealthResource resource,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

            Integer counselorId = getLoggedInCounselorId(session);

            if (counselorId == null) {
                return "redirect:/login";
            }

            Person counselor = (Person) session.getAttribute("loggedUser");
            resource.setCreatedByName(counselor.getName());

        /* ===== HANDLE VIDEO / ARTICLE ===== */
        if ("VIDEO".equals(resource.getType())) {

            String videoId = extractYoutubeVideoId(resource.getArticleUrl());

            if (videoId == null) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Invalid YouTube link."
                );

                return resource.getId() == 0
                        ? "redirect:/counselor/resources/add"
                        : "redirect:/counselor/resources/edit/" + resource.getId();
            }

            resource.setYoutubeVideoId(videoId);
            resource.setArticleUrl(null);

        } else if ("ARTICLE".equals(resource.getType())) {

            if (resource.getArticleUrl() == null || resource.getArticleUrl().isBlank()) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Please provide a valid article URL."
                );

                return resource.getId() == 0
                        ? "redirect:/counselor/resources/add"
                        : "redirect:/counselor/resources/edit/" + resource.getId();
            }

            resource.setYoutubeVideoId(null);
        }

        /* ===== ADD vs EDIT ===== */
        if (resource.getId() == 0) {
           resource.setCreatedById(counselorId);
            resource.setCreatedAt(LocalDateTime.now());
            resource.setCreatedAt(LocalDateTime.now());
        } else {
            MentalHealthResource existing =
                    resourceDao.findById(resource.getId());
            resource.setCreatedAt(existing.getCreatedAt());
            resource.setCreatedById(existing.getCreatedById());
            resource.setCreatedByName(existing.getCreatedByName());
        }

        resourceDao.save(resource);

        redirectAttributes.addFlashAttribute(
                "success",
                "Resource saved successfully."
        );

        return "redirect:/counselor/resources";
    }

    /* =========================
       DELETE RESOURCE
       ========================= */
    @GetMapping("/resources/delete/{id}")
    public String deleteResource(@PathVariable int id) {
        resourceDao.delete(id);
        return "redirect:/counselor/resources";
    }

    /* =========================
       ADD / MANAGE ASSESSMENT
       (method name = addAssessment)
       ========================= */
    @GetMapping("/resources/{id}/assessment/add")
    public String addAssessment(@PathVariable int id, Model model) {

        MentalHealthResource resource = resourceDao.findById(id);
        Assessment assessment = assessmentDao.findByResourceId(id);

        if (assessment != null) {
            List<AssessmentQuestion> questions =
                    questionDao.findByAssessmentId(assessment.getId());
            model.addAttribute("questions", questions);
        }

        model.addAttribute("resource", resource);
        return "counselor/assessment-questions";
    }

    /* =========================
       SAVE ASSESSMENT
       ========================= */
    @PostMapping("/resources/{id}/assessment/save")
    public String saveAssessment(
            @PathVariable int id,
            @RequestParam Map<String, String> params) {

        Assessment assessment = assessmentDao.findByResourceId(id);

        if (assessment == null) {
            assessment = new Assessment();
            assessment.setResourceId(id);
            assessmentDao.save(assessment);
        }

        questionDao.deleteByAssessmentId(assessment.getId());

        for (int i = 1; i <= 4; i++) {
            AssessmentQuestion q = new AssessmentQuestion();
            q.setAssessmentId(assessment.getId());
            q.setQuestionOrder(i);
            q.setQuestionText(params.get("question" + i));
            q.setOptionA(params.get("option" + i + "A"));
            q.setOptionB(params.get("option" + i + "B"));
            q.setOptionC(params.get("option" + i + "C"));
            q.setOptionD(params.get("option" + i + "D"));
            q.setCorrectOption(params.get("correct" + i));
            questionDao.save(q);
        }

        return "redirect:/counselor/resources";
    }

    /* =========================
       HELPER METHODS
       ========================= */
   private Integer getLoggedInCounselorId(HttpSession session) {

    Person user = (Person) session.getAttribute("loggedUser");

    if (user == null) {
        return null;
    }

    return user.getId();
}

    private String extractYoutubeVideoId(String url) {
        if (url == null || url.isBlank()) return null;

        if (url.contains("youtu.be/")) {
            return url.substring(url.lastIndexOf("/") + 1).split("\\?")[0];
        }

        if (url.contains("watch?v=")) {
            return url.substring(url.indexOf("v=") + 2).split("&")[0];
        }

        return null;
    }

    /* =========================
   CREATE SESSION PAGE
   ========================= */
@GetMapping("/sessions/create")
public String showCreateSessionPage(Model model) {
    model.addAttribute("newSession", new ConsultationSession());
    return "counselor/create-session";
}


/* =========================
   CREATE CONSULTATION SESSION
   ========================= */
@PostMapping("/sessions/create")
public String createSession(
        @RequestParam("sessionDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate sessionDate,

        @RequestParam("startTime")
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        LocalTime startTime,

        @RequestParam("endTime")
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        LocalTime endTime,

        HttpSession session,
        RedirectAttributes ra) {

    Integer counselorId = getLoggedInCounselorId(session);
    if (counselorId == null) return "redirect:/login";

    if (!endTime.isAfter(startTime)) {
        ra.addFlashAttribute("error", "End time must be after start time");
        return "redirect:/counselor/sessions/create";
    }

    ConsultationSession cs = new ConsultationSession();
    cs.setCounselorId(counselorId);
    cs.setSessionDate(sessionDate);
    cs.setStartTime(startTime);
    cs.setEndTime(endTime);
    cs.setStatus("AVAILABLE");
    cs.setCreatedAt(LocalDateTime.now());

    consultationSessionDao.save(cs);

    ra.addFlashAttribute("success", "Session created successfully ✅");
    return "redirect:/counselor/dashboard";
}

/* =========================
   MANAGE SESSIONS PAGE
   ========================= */
@GetMapping("/sessions/manage")
public String manageSessions(Model model, HttpSession session) {

    Integer counselorId = getLoggedInCounselorId(session);
    if (counselorId == null) return "redirect:/counselor/dashboard";

    model.addAttribute(
        "mySessions",
        consultationSessionDao.findByCounselor(counselorId)
    );

    return "counselor/manage-sessions";
}
// =========================
// VIEW BOOKED SESSIONS
// =========================
@GetMapping("/sessions/booked")
public String viewBookedSessions(HttpSession session, Model model) {

    Person counselor = (Person) session.getAttribute("loggedUser");
    if (counselor == null) {
        return "redirect:/login";
    }

    
    List<Object[]> bookedSessions =
            consultationBookingDao.findBookedSessionsWithStudentByCounselor(
                    counselor.getId()
            );

    model.addAttribute("bookedSessions", bookedSessions);
    return "counselor/booked-sessions";
}


/* =========================
   UPDATE SESSION (EDIT)
   ========================= */

@GetMapping("/sessions/{id}/edit")
public String editSession(
        @PathVariable int id,
        HttpSession httpSession,
        Model model) {

    Person counselor = (Person) httpSession.getAttribute("loggedUser");
    if (counselor == null) {
        return "redirect:/login";
    }

    ConsultationSession session = consultationSessionDao.findById(id);
    if (session == null) {
        return "redirect:/counselor/sessions/manage";
    }

    if (session.getCounselorId() != counselor.getId()) {
        return "redirect:/login";
    }

    if (!"AVAILABLE".equals(session.getStatus())) {
        return "redirect:/counselor/sessions/manage";
    }

    // ✅ FIXED NAME
    model.addAttribute("consultationSession", session);

    return "counselor/edit-session";
}



@PostMapping("/sessions/{id}/edit")
public String updateSession(
        @PathVariable int id,
        @RequestParam("sessionDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate sessionDate,

        @RequestParam("startTime")
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        LocalTime startTime,

        @RequestParam("endTime")
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        LocalTime endTime,

        HttpSession httpSession,
        RedirectAttributes ra) {

    Person counselor = (Person) httpSession.getAttribute("loggedUser");
    if (counselor == null) {
        return "redirect:/login";
    }

    ConsultationSession cs = consultationSessionDao.findById(id);
    if (cs == null) {
        return "redirect:/counselor/sessions/manage";
    }

    if (!"AVAILABLE".equals(cs.getStatus())) {
        return "redirect:/counselor/sessions/manage";
    }

    if (!endTime.isAfter(startTime)) {
        ra.addFlashAttribute("error", "End time must be after start time");
        return "redirect:/counselor/sessions/" + id + "/edit"; // ✅ CORRECT
    }

    cs.setSessionDate(sessionDate);
    cs.setStartTime(startTime);
    cs.setEndTime(endTime);

    consultationSessionDao.save(cs);

    return "redirect:/counselor/sessions/manage";
}


/* =========================
   CANCEL SESSION (AVAILABLE ONLY)
   =========================
   Counselor can cancel ONLY if:
   - session is AVAILABLE
   - session belongs to counselor
*/
@PostMapping("/sessions/{id}/cancel")
public String cancelSession(
        @PathVariable int id,
        HttpSession httpSession,
        RedirectAttributes ra) {

    Person counselor = (Person) httpSession.getAttribute("loggedUser");
    if (counselor == null) {
        return "redirect:/login";
    }

    ConsultationSession cs =
            consultationSessionDao.findById(id);

    // ❌ session not found
    if (cs == null) {
        return "redirect:/counselor/sessions/manage";
    }

    // ❌ counselor does NOT own this session
    if (cs.getCounselorId() != counselor.getId()) {
        return "redirect:/login";
    }

    //  cannot cancel if already booked or cancelled
    if (!"AVAILABLE".equals(cs.getStatus())) {
        return "redirect:/counselor/sessions/manage";
    }

    // ✅ safe cancel
    cs.setStatus("CANCELLED");
    consultationSessionDao.save(cs);

    ra.addFlashAttribute("success", "Session cancelled");
    return "redirect:/counselor/sessions/manage";
}



}
