package com.mindbloom.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.mindbloom.dao.AssessmentDao;
import com.mindbloom.dao.AssessmentQuestionDao;
import com.mindbloom.dao.MentalHealthResourceDao;
import com.mindbloom.model.Assessment;
import com.mindbloom.model.AssessmentQuestion;
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
            Model model) {

        List<MentalHealthResource> resources;

        if (search != null && !search.trim().isEmpty()) {
            resources = resourceDao.searchByTitle(search);
        } else {
            resources = resourceDao.findAll();
        }

        // Map: resourceId → hasAssessmentWithQuestions
        Map<Integer, Boolean> assessmentMap = new HashMap<>();

        for (MentalHealthResource res : resources) {
            Assessment assessment =
                    assessmentDao.findByResourceId(res.getId());

            if (assessment != null) {
                List<AssessmentQuestion> questions =
                        questionDao.findByAssessmentId(assessment.getId());
                assessmentMap.put(res.getId(), !questions.isEmpty());
            } else {
                assessmentMap.put(res.getId(), false);
            }
        }

        model.addAttribute("resources", resources);
        model.addAttribute("assessmentMap", assessmentMap);

        return "counselor/resources";
    }

    /* =========================
       ADD RESOURCE FORM
       ========================= */
    @GetMapping("/resources/add")
    public String showAddForm(Model model) {
        model.addAttribute("resource", new MentalHealthResource());
        return "counselor/resource-form";
    }

    /* =========================
       SAVE RESOURCE
       ========================= */
    @PostMapping("/resources/save")
    public String saveResource(
            @ModelAttribute("resource") MentalHealthResource resource,
            HttpSession session) {

        Person counselor = getLoggedInCounselor(session);

        // ADD
        if (resource.getId() == 0) {
            resource.setCreatedById(counselor.getId());
            resource.setCreatedByName(counselor.getName());
            resource.setCreatedAt(LocalDateTime.now());
        }
        // EDIT
        else {
            MentalHealthResource existing =
                    resourceDao.findById(resource.getId());

            resource.setCreatedAt(existing.getCreatedAt());
            resource.setCreatedById(existing.getCreatedById());
            resource.setCreatedByName(existing.getCreatedByName());
        }

        resourceDao.save(resource);
        return "redirect:/counselor/resources";
    }

    /* =========================
       EDIT RESOURCE
       ========================= */
    @GetMapping("/resources/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        model.addAttribute("resource", resourceDao.findById(id));
        return "counselor/resource-form";
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
       SAVE ASSESSMENT QUESTIONS
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

        // Clear old questions
        questionDao.deleteByAssessmentId(assessment.getId());

        // Always 4 questions
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
       HELPER
       ========================= */
    private Person getLoggedInCounselor(HttpSession session) {
        return (Person) session.getAttribute("loggedUser");
    }
}
