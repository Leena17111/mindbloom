package com.mindbloom.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/dashboard")
    public String counselorDashboard() {
        return "counselor/dashboard";
    }

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

        Map<Integer, Boolean> assessmentMap = new HashMap<>();
        for (MentalHealthResource res : resources) {
            Assessment assessment = assessmentDao.findByResourceId(res.getId());
            assessmentMap.put(res.getId(), assessment != null);
        }

        model.addAttribute("resources", resources);
        model.addAttribute("assessmentMap", assessmentMap);
        model.addAttribute("selectedCategory", category);

        return "counselor/resources";
    }

    @GetMapping("/resources/add")
    public String showAddForm(Model model) {
        model.addAttribute("resource", new MentalHealthResource());
        return "counselor/resource-form";
    }

    @GetMapping("/resources/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        model.addAttribute("resource", resourceDao.findById(id));
        return "counselor/resource-form";
    }

    @PostMapping("/resources/save")
    public String saveResource(
            @ModelAttribute("resource") MentalHealthResource resource,
            RedirectAttributes redirectAttributes) {

        Integer counselorId = getLoggedInCounselorId();
        if (counselorId == null) return "redirect:/login";

        Person counselor = getLoggedInCounselor();

        resource.setCreatedByName(counselor.getName());

        if ("VIDEO".equals(resource.getType())) {

            String videoId = extractYoutubeVideoId(resource.getArticleUrl());

            if (videoId == null) {
                redirectAttributes.addFlashAttribute("error", "Invalid YouTube link.");
                return resource.getId() == 0
                        ? "redirect:/counselor/resources/add"
                        : "redirect:/counselor/resources/edit/" + resource.getId();
            }

            resource.setYoutubeVideoId(videoId);
            resource.setArticleUrl(null);

        } else if ("ARTICLE".equals(resource.getType())) {

            if (resource.getArticleUrl() == null || resource.getArticleUrl().isBlank()) {
                redirectAttributes.addFlashAttribute("error", "Please provide a valid article URL.");
                return resource.getId() == 0
                        ? "redirect:/counselor/resources/add"
                        : "redirect:/counselor/resources/edit/" + resource.getId();
            }

            resource.setYoutubeVideoId(null);
        }

        if (resource.getId() == 0) {
            resource.setCreatedById(counselorId);
            resource.setCreatedAt(LocalDateTime.now());
        } else {
            MentalHealthResource existing = resourceDao.findById(resource.getId());
            resource.setCreatedAt(existing.getCreatedAt());
            resource.setCreatedById(existing.getCreatedById());
            resource.setCreatedByName(existing.getCreatedByName());
        }

        resourceDao.save(resource);

        redirectAttributes.addFlashAttribute("success", "Resource saved successfully.");
        return "redirect:/counselor/resources";
    }

    @GetMapping("/resources/delete/{id}")
    public String deleteResource(@PathVariable int id) {
        resourceDao.delete(id);
        return "redirect:/counselor/resources";
    }

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

    @GetMapping("/sessions/create")
    public String showCreateSessionPage(Model model) {
        model.addAttribute("newSession", new ConsultationSession());
        return "counselor/create-session";
    }

    @PostMapping("/sessions/create")
    public String createSession(
            @RequestParam("sessionDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDate,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            RedirectAttributes ra) {

        Integer counselorId = getLoggedInCounselorId();
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

    @GetMapping("/sessions/manage")
    public String manageSessions(Model model) {

        Integer counselorId = getLoggedInCounselorId();
        if (counselorId == null) return "redirect:/counselor/dashboard";

        model.addAttribute(
                "mySessions",
                consultationSessionDao.findByCounselor(counselorId)
        );

        return "counselor/manage-sessions";
    }

    @GetMapping("/sessions/booked")
    public String viewBookedSessions(Model model) {

        Person counselor = getLoggedInCounselor();
        if (counselor == null) return "redirect:/login";

        List<Object[]> bookedSessions =
                consultationBookingDao.findBookedSessionsWithStudentByCounselor(
                        counselor.getId()
                );

        model.addAttribute("bookedSessions", bookedSessions);
        return "counselor/booked-sessions";
    }

    @GetMapping("/sessions/{id}/edit")
    public String editSession(@PathVariable int id, Model model) {

        Person counselor = getLoggedInCounselor();
        if (counselor == null) return "redirect:/login";

        ConsultationSession session = consultationSessionDao.findById(id);
        if (session == null) return "redirect:/counselor/sessions/manage";

        if (session.getCounselorId() != counselor.getId()) return "redirect:/login";
        if (!"AVAILABLE".equals(session.getStatus())) return "redirect:/counselor/sessions/manage";

        model.addAttribute("consultationSession", session);
        return "counselor/edit-session";
    }

    @PostMapping("/sessions/{id}/edit")
    public String updateSession(
            @PathVariable int id,
            @RequestParam("sessionDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDate,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            RedirectAttributes ra) {

        Person counselor = getLoggedInCounselor();
        if (counselor == null) return "redirect:/login";

        ConsultationSession cs = consultationSessionDao.findById(id);
        if (cs == null) return "redirect:/counselor/sessions/manage";
        if (!"AVAILABLE".equals(cs.getStatus())) return "redirect:/counselor/sessions/manage";

        if (!endTime.isAfter(startTime)) {
            ra.addFlashAttribute("error", "End time must be after start time");
            return "redirect:/counselor/sessions/" + id + "/edit";
        }

        cs.setSessionDate(sessionDate);
        cs.setStartTime(startTime);
        cs.setEndTime(endTime);

        consultationSessionDao.save(cs);
        return "redirect:/counselor/sessions/manage";
    }

    @PostMapping("/sessions/{id}/cancel")
    public String cancelSession(@PathVariable int id, RedirectAttributes ra) {

        Person counselor = getLoggedInCounselor();
        if (counselor == null) return "redirect:/login";

        ConsultationSession cs = consultationSessionDao.findById(id);
        if (cs == null) return "redirect:/counselor/sessions/manage";
        if (cs.getCounselorId() != counselor.getId()) return "redirect:/login";
        if (!"AVAILABLE".equals(cs.getStatus())) return "redirect:/counselor/sessions/manage";

        cs.setStatus("CANCELLED");
        consultationSessionDao.save(cs);

        ra.addFlashAttribute("success", "Session cancelled");
        return "redirect:/counselor/sessions/manage";
    }

    private Integer getLoggedInCounselorId() {
        Person counselor = getLoggedInCounselor();
        return counselor == null ? null : counselor.getId();
    }

    private Person getLoggedInCounselor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return (Person) auth.getPrincipal();
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
}