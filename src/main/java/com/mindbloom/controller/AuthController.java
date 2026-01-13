package com.mindbloom.controller;

import java.time.LocalDate;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // ✅ ADD
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.mindbloom.dao.PersonDao;
import com.mindbloom.model.Person;

@Controller
public class AuthController {

    @Autowired
    private PersonDao personDao;

    // =========================
    // SHOW LOGIN
    // =========================
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    // =========================
    // PROCESS LOGIN ✅ FIXED
    // =========================
    @PostMapping("/login")
    public String doLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Person person = personDao.findByEmail(email);

        if (person == null) {
            model.addAttribute("error", "Invalid email or password");
            return "auth/login";
        }

        // =========================
        // 🔐 PASSWORD CHECK (CRITICAL FIX)
        // =========================
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        boolean passwordMatches;

        // Support BOTH old plain-text passwords AND new hashed ones
        if (person.getPassword().startsWith("$2a$")) {
            // BCrypt hashed password
            passwordMatches = encoder.matches(password, person.getPassword());
        } else {
            // Plain-text password (old admin/counselor accounts)
            passwordMatches = person.getPassword().equals(password);
        }

        if (!passwordMatches) {
            model.addAttribute("error", "Invalid email or password");
            return "auth/login";
        }

        // =========================
        // LOGIN SUCCESS
        // =========================
        session.setAttribute("loggedUser", person);
        session.setAttribute("role", person.getRole());

        String role = person.getRole().trim().toUpperCase();

        switch (role) {
            case "STUDENT":
                return "redirect:/student/dashboard";
            case "ADMIN":
                return "redirect:/admin/dashboard";
            case "COUNSELOR":
                return "redirect:/counselor/dashboard";
            default:
                return "redirect:/login";
        }
    }

    // =========================
    // SHOW REGISTER
    // =========================
    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    // =========================
    // PROCESS REGISTER (still plain-text for now)
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
        person.setPassword(password); // ⚠ plain-text (OK for now)
        person.setPhone(phone);
        person.setDateOfBirth(dateOfBirth);
        person.setRole("STUDENT");

        personDao.save(person);

        return "redirect:/login";
    }

    // =========================
    // LOGOUT
    // =========================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
