package com.mindbloom.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.mindbloom.dao.PersonDao;
import com.mindbloom.model.Person;
import com.mindbloom.service.EmailService;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private PersonDao personDao;

    @Autowired
    private EmailService emailService;

    // =========================
    // EXISTING PAGES (UNCHANGED)
    // =========================

    @GetMapping("/dashboard")
    public String dashboard() {
        return "student/dashboard";
    }

    @GetMapping("/emergency")
    public String emergency() {
        return "student/emergency";
    }

    // =========================
    // FORGOT PASSWORD FLOW (ADDED)
    // =========================

    // STEP 1: show forgot password page
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "student/forgot-password";
    }

    // STEP 2: send verification code
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

    // STEP 3: verify code
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

    // STEP 4: reset password
   

@PostMapping("/reset-password")
public String resetPassword(
        @RequestParam("email") String email,
        @RequestParam("password") String password,
        @RequestParam("confirmPassword") String confirmPassword,
        Model model) {

    // backend safety check
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

    // HASH PASSWORD
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    String hashedPassword = encoder.encode(password);

    person.setPassword(hashedPassword);
    person.setResetCode(null);
    person.setResetCodeExpiry(null);
    personDao.save(person);

    return "redirect:/login";
}

   

}
