package com.mindbloom.controller;

import java.time.LocalDate;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.mindbloom.dao.PersonDao;
import com.mindbloom.model.Person;

@Controller
public class AuthController {

    @Autowired
    private PersonDao personDao;

    // 🔐 BCrypt encoder (Bean provided by SecurityConfig)
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // =========================
    // SHOW LOGIN PAGE
    // =========================
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    // =========================
    // SHOW REGISTER PAGE
    // =========================
    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    // =========================
    // PROCESS REGISTER (BCrypt)
    // =========================
    @PostMapping("/register")
    public String doRegister(
            @RequestParam("name") String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateOfBirth,
            Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "auth/register";
        }

        if (personDao.findByEmail(email) != null) {
            model.addAttribute("error", "Email is already registered");
            return "auth/register";
        }

        Person person = new Person();
        person.setName(fullName);
        person.setEmail(email);

        // 🔐 HASH PASSWORD (CORRECT & REQUIRED)
        person.setPassword(passwordEncoder.encode(password));

        person.setPhone(phone);
        person.setDateOfBirth(dateOfBirth);
        person.setRole("STUDENT");

        personDao.save(person);

        return "redirect:/login";
    }

    // =========================
    // LOGOUT (optional, Security also handles it)
    // =========================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
