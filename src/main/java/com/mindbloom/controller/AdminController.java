package com.mindbloom.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mindbloom.dao.AssessmentResultDao;
import com.mindbloom.dao.ConsultationBookingDao;
import com.mindbloom.dao.EmergencyAlertDao;
import com.mindbloom.dao.PersonDao;
import com.mindbloom.dao.StudentResourceProgressDao;
import com.mindbloom.model.Person;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private PersonDao personDao;
	
	@Autowired
	private AssessmentResultDao assessmentResultDao;
	
	@Autowired
	private StudentResourceProgressDao studentResourceProgressDao;
	
	@Autowired
	private ConsultationBookingDao consultationBookingDao;
    @Autowired
    private EmergencyAlertDao emergencyAlertDao;

	@GetMapping("/dashboard")
public String adminDashboard(Model model) {

    List<Person> students = personDao.findByRole("STUDENT");
    int totalStudents = students.size();

    int totalCompletedAssessments = 0;
    for (Person student : students) {
        totalCompletedAssessments +=
                assessmentResultDao.findByStudentId(student.getId()).size();
    }

    long panicClicks = emergencyAlertDao.findAll().size();

    model.addAttribute("totalStudents", totalStudents);
    model.addAttribute("completedAssessments", totalCompletedAssessments);
    model.addAttribute("panicClicks", panicClicks);

    return "admin/dashboard";
}

    
    @GetMapping("/students/monitoring")
    public String studentsMonitoring(Model model) {
        List<Person> students = personDao.findByRole("STUDENT");
        List<Map<String, Object>> studentData = new ArrayList<>();
        
        for (Person student : students) {
            Map<String, Object> data = new HashMap<>();
            data.put("student", student);
            
            // Calculate average quiz score
            List<com.mindbloom.model.AssessmentResult> results = 
                assessmentResultDao.findByStudentId(student.getId());
            double avgScore = 0.0;
            if (!results.isEmpty()) {
                int totalScore = 0;
                for (com.mindbloom.model.AssessmentResult result : results) {
                    totalScore += result.getScore();
                }
                avgScore = (double) totalScore / results.size();
            }
            data.put("averageScore", avgScore);
            
            // Count resources accessed (completed)
            int resourcesAccessed = studentResourceProgressDao.countCompletedResources(student.getId());
            data.put("resourcesAccessed", resourcesAccessed);
            
            // Count approved bookings (BOOKED status)
            List<com.mindbloom.model.ConsultationBooking> bookings = 
                consultationBookingDao.findByStudent(student.getId());
            int approvedForms = bookings.size(); // All bookings are considered "approved" when status is BOOKED
            data.put("approvedForms", approvedForms);
            
            studentData.add(data);
        }
        
        model.addAttribute("studentData", studentData);
        return "admin/student-monitoring";
    }
    
    @GetMapping("/analytics")
    public String analytics(Model model) {
        List<Person> students = personDao.findByRole("STUDENT");
        
        // Prepare data for charts
        List<String> studentNames = new ArrayList<>();
        List<Double> averageScores = new ArrayList<>();
        List<Integer> resourcesCount = new ArrayList<>();
        List<Integer> approvedFormsCount = new ArrayList<>();
        
        for (Person student : students) {
            studentNames.add(student.getName());
            
            // Average score
            List<com.mindbloom.model.AssessmentResult> results = 
                assessmentResultDao.findByStudentId(student.getId());
            double avgScore = 0.0;
            if (!results.isEmpty()) {
                int totalScore = 0;
                for (com.mindbloom.model.AssessmentResult result : results) {
                    totalScore += result.getScore();
                }
                avgScore = (double) totalScore / results.size();
            }
            averageScores.add(avgScore);
            
            // Resources accessed
            int resourcesAccessed = studentResourceProgressDao.countCompletedResources(student.getId());
            resourcesCount.add(resourcesAccessed);
            
            // Approved forms
            List<com.mindbloom.model.ConsultationBooking> bookings = 
                consultationBookingDao.findByStudent(student.getId());
            approvedFormsCount.add(bookings.size());
        }
        
        model.addAttribute("studentNames", studentNames);
        model.addAttribute("averageScores", averageScores);
        model.addAttribute("resourcesCount", resourcesCount);
        model.addAttribute("approvedFormsCount", approvedFormsCount);
        model.addAttribute("totalStudents", students.size());
        
        return "admin/analytics";
    }
}