package com.mindbloom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student")
public class StudentController {

    // =========================
    // Emergency / Panic Button
    // =========================
    @GetMapping("/emergency")
    public String emergencyPage() {
        return "student/emergency";
    }

    @GetMapping("/dashboard")
    public String studentDashboard() {
        return "student/dashboard";
    }
}
