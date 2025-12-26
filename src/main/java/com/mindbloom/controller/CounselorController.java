package com.mindbloom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/counselor")
public class CounselorController {

    @GetMapping("/dashboard")
    public String counselorDashboard() {
        return "counselor/dashboard";
    }
}
