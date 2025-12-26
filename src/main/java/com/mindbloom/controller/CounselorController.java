package com.mindbloom.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mindbloom.dao.AssessmentDao;
import com.mindbloom.dao.AssessmentQuestionDao;
import com.mindbloom.dao.MentalHealthResourceDao;
import com.mindbloom.model.Assessment;
import com.mindbloom.model.AssessmentQuestion;
import com.mindbloom.model.MentalHealthResource;

@Controller
@RequestMapping("/counselor")
public class CounselorController {

    @Autowired
    private MentalHealthResourceDao resourceDao;

    @Autowired
    private AssessmentDao assessmentDao;

    @Autowired
    private AssessmentQuestionDao questionDao;


    /* =================================================
       TEMPORARY AUTH SIMULATION
       Replace when Spring Security is implemented
       ================================================= */

    // TODO: Replace with logged-in counselor ID from Spring Security
    private Long getLoggedInCounselorId() {
        return 1L; // TEMP: pretend counselor with ID = 1 is logged in
    }

    // TODO: Replace with logged-in counselor name from Spring Security
    private String getLoggedInCounselorName() {
        return "Dr. Azmina Ahmed"; // TEMP: fake logged-in counselor
    }

    /* ===========================
   LIST RESOURCES
   =========================== */
    @GetMapping("/resources")
    public String listResources(
            @RequestParam(required = false) String search,
            Model model) {

        // Get resources (with or without search)
        List<MentalHealthResource> resources;

        if (search != null && !search.trim().isEmpty()) {
            resources = resourceDao.searchByTitle(search);
        } else {
            resources = resourceDao.findAll();
        }

        // Create a map to track which resources have assessments WITH QUESTIONS
        Map<Long, Boolean> assessmentMap = new HashMap<>();

        for (MentalHealthResource res : resources) {
            Assessment assessment = assessmentDao.findByResourceId(res.getId());
            if (assessment != null) {
                // Check if assessment has questions 
                List<AssessmentQuestion> questions = questionDao.findByAssessmentId(assessment.getId());
                assessmentMap.put(res.getId(), !questions.isEmpty());
            } else {
                assessmentMap.put(res.getId(), false);
            }
        }

        // Step 3: Send data to the view
        model.addAttribute("resources", resources);
        model.addAttribute("assessmentMap", assessmentMap);

        return "counselor/resources";
    }


    /* ===========================
       SHOW ADD FORM
       =========================== */
    @GetMapping("/resources/add")
    public String showAddForm(Model model) {
        model.addAttribute("resource", new MentalHealthResource());
        return "counselor/resource-form";
    }

    /* ===========================
       SAVE RESOURCE (INSERT OR UPDATE)
       =========================== */
    @PostMapping("/resources/save")
    public String saveResource(@ModelAttribute("resource") MentalHealthResource resource) {

        // ADD
        if (resource.getId() == null) {
            resource.setCreatedById(getLoggedInCounselorId());
            resource.setCreatedByName(getLoggedInCounselorName());
            resource.setCreatedAt(LocalDateTime.now()); 
        } 
        // EDIT
        else {
            MentalHealthResource existing = resourceDao.findById(resource.getId());
            resource.setCreatedAt(existing.getCreatedAt()); 
            resource.setCreatedById(existing.getCreatedById());
            resource.setCreatedByName(existing.getCreatedByName());
        }

        resourceDao.save(resource);
        return "redirect:/counselor/resources";
    }



    /* ===========================
       SHOW EDIT FORM
       =========================== */
    @GetMapping("/resources/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("resource", resourceDao.findById(id));
        return "counselor/resource-form";
    }

    /* ===========================
       DELETE RESOURCE
       =========================== */
    @GetMapping("/resources/delete/{id}")
    public String deleteResource(@PathVariable Long id) {
        resourceDao.delete(id);
        return "redirect:/counselor/resources";
    }

    /* ===========================
       ADD / MANAGE ASSESSMENT
   =========================== */
    @GetMapping("/resources/{id}/assessment/add")
    public String addAssessment(@PathVariable Long id, Model model) {

    // Fetch the selected mental health resource
    MentalHealthResource resource = resourceDao.findById(id);

    // Check if an assessment already exists for this resource
    Assessment assessment = assessmentDao.findByResourceId(id);
    
    // If assessment exists, load existing questions for editing
    if (assessment != null) {
        List<AssessmentQuestion> questions = questionDao.findByAssessmentId(assessment.getId());
        model.addAttribute("questions", questions);
    }

    // Pass resource data to the view (used for title/context display)
    model.addAttribute("resource", resource);

    // Show assessment questions form (don't create assessment yet!)
    return "counselor/assessment-questions";
}

    /* ===========================
        SAVE ASSESSMENT QUESTIONS
    =========================== */
    @PostMapping("/resources/{id}/assessment/save")
    public String saveAssessment(
        @PathVariable Long id,
        @RequestParam Map<String, String> params) {

    // Retrieve or create the assessment for this resource
    Assessment assessment = assessmentDao.findByResourceId(id);
    
    // If no assessment exists, create it now (only when form is submitted!)
    if (assessment == null) {
        assessment = new Assessment();
        assessment.setResourceId(id);
        assessmentDao.save(assessment);
    }

    // Remove old questions to avoid duplicates (for Manage Assessment)
    questionDao.deleteByAssessmentId(assessment.getId());

    // Loop through exactly 4 questions
    for (int i = 1; i <= 4; i++) {

        AssessmentQuestion question = new AssessmentQuestion();

        // Link question to assessment
        question.setAssessmentId(assessment.getId());

        // Maintain question order (1 to 4)
        question.setQuestionOrder(i);

        // Set question text
        question.setQuestionText(params.get("question" + i));

        // Set MCQ options
        question.setOptionA(params.get("option" + i + "A"));
        question.setOptionB(params.get("option" + i + "B"));
        question.setOptionC(params.get("option" + i + "C"));
        question.setOptionD(params.get("option" + i + "D"));

        // Set correct option (A, B, C, or D)
        question.setCorrectOption(params.get("correct" + i));

        // Save question
        questionDao.save(question);
    }

    // After saving, return to resources list
    return "redirect:/counselor/resources";
}


}
